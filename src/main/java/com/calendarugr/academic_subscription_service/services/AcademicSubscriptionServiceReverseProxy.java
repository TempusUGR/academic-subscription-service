package com.calendarugr.academic_subscription_service.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;

import com.calendarugr.academic_subscription_service.config.RabbitMQConfig;
import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.calendarugr.academic_subscription_service.dtos.FacultyDTO;
import com.calendarugr.academic_subscription_service.dtos.FacultyGroupEventsDTO;
import com.calendarugr.academic_subscription_service.dtos.IdDTO;
import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.ExtraClasses;
import com.calendarugr.academic_subscription_service.entities.Subscription;
import com.calendarugr.academic_subscription_service.mappers.ExtraClassMapper;
import com.calendarugr.academic_subscription_service.mappers.SubscriptionMapper;
import com.calendarugr.academic_subscription_service.repositories.ExtraClassesRepository;
import com.calendarugr.academic_subscription_service.repositories.SubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class AcademicSubscriptionServiceReverseProxy implements IAcademicSubscriptionService {

    @Value("${server.port}")
    private String port;

    private Logger logger = LoggerFactory.getLogger(AcademicSubscriptionServiceReverseProxy.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ExtraClassesRepository extraClassesRepository;

    String scheduleConsumerurl = "http://schedule-consumer-service/schedule-consumer";
    String userUrl = "http://user-service/user";

    private static final String SECRET_KEY = System.getProperty("SECRET_KEY");
    // This is the secret key used to encrypt and decrypt the userId

    private void sendNotificationEmail(List<String> emails, ExtraClassDTO extraClassDTO) {

        Map<String, Object> message = new HashMap<>();
        message.put("emails", emails);
        message.put("gradeName", extraClassDTO.getGradeName());
        message.put("subjectName", extraClassDTO.getSubjectName());
        message.put("groupName", extraClassDTO.getGroupName());
        message.put("date", extraClassDTO.getDate().getDayOfMonth() + "-" + extraClassDTO.getDate().getMonthValue()
                + "-" + extraClassDTO.getDate().getYear());
        message.put("initHour", extraClassDTO.getInitHour().getHour() + ":" + extraClassDTO.getInitHour().getMinute());
        message.put("finishHour",
                extraClassDTO.getFinishHour().getHour() + ":" + extraClassDTO.getFinishHour().getMinute());
        message.put("classroom", extraClassDTO.getClassroom());
        message.put("title", extraClassDTO.getTitle());
        message.put("type", extraClassDTO.getType());
        message.put("facultyName", extraClassDTO.getFacultyName());
        message.put("teacher", extraClassDTO.getTeacher());
        message.put("day", extraClassDTO.getDay());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(message);
            Message msg = MessageBuilder
                    .withBody(jsonMessage.getBytes())
                    .setContentType("application/json")
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.MAIL_EXCHANGE, RabbitMQConfig.MAIL_ROUTING_KEY, msg);
        } catch (JsonProcessingException e) {
            logger.error("Error al convertir el mensaje a JSON: " + e.getMessage());
        }
    }

    private String getIdentifierFromUserId(String userId) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES"); // Using AES encryption : Little explanation
            // AES (Advanced Encryption Standard) is a symmetric encryption algorithm that
            // uses the same key for both encryption and decryption. It is widely used for
            // securing data due to its efficiency and security.
            // The key must be 16, 24, or 32 bytes long for AES-128, AES-192, or AES-256
            // encryption, respectively. In this case, we are using a 16-byte key.
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(userId.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting userId", e);
        }
    }

    private String getUserIdFromIdentifier(String identifier) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getUrlDecoder().decode(identifier));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting identifier", e);
        }
    }

    public List<ClassDTO> getClasses(String studentId) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(studentId);
        // First we need to get the subscriptions of the user
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        if (subscriptions.isEmpty()) {
            return List.of();
        }

        // From List<Subscription> to List<SubscriptionDTO>
        List<SubscriptionDTO> subscriptionsDTO = SubscriptionMapper.toDTOList(subscriptions);

        List<ClassDTO> classes = webClientBuilder.build()
                .post()
                .uri(scheduleConsumerurl + "/classes-per-subscriptions")
                .bodyValue(subscriptionsDTO)
                .retrieve()
                .bodyToFlux(ClassDTO.class)
                .collectList()
                .block();

        return classes;
    }

    public HashMap<String, List<?>> getEntireCalendar(String userId) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(userId);

        // First we extract all the faculty events of the user
        // In order to achieve this, we get the faculties of the user
        List<FacultyDTO> faculties = subscriptionRepository.findFacultyNameByStudentId(studentInteger);
        List<String> facultiesString = faculties.stream().map(FacultyDTO::getFacultyName).distinct().toList();

        // Now we get the events of the user
        List<ExtraClasses> extraClasses = extraClassesRepository.findByTypeAndFacultyNameIn("FACULTY", facultiesString);
        List<ExtraClassDTO> facultyEventsDTO = ExtraClassMapper.toDTOList(extraClasses);

        // Then we are going to get all the group events
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        if (subscriptions.isEmpty()) {
            return new HashMap<>();
        }

        List<ExtraClasses> groupEvents = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            List<ExtraClasses> extraClassesGroup = extraClassesRepository
                    .findByTypeAndGradeNameAndSubjectNameAndGroupName(
                            "GROUP",
                            subscription.getGradeName(),
                            subscription.getSubjectName(),
                            subscription.getGroupName());

            groupEvents.addAll(extraClassesGroup);
        }

        List<ExtraClassDTO> groupEventsDTO = ExtraClassMapper.toDTOList(groupEvents);

        // Last thing, we get the official classes of the user
        // Subscriptions to dto
        List<SubscriptionDTO> subscriptionsDTO = SubscriptionMapper.toDTOList(subscriptions);

        // Call the schedule-consumer service to get the classes
        List<ClassDTO> classes = webClientBuilder.build()
                .post()
                .uri(scheduleConsumerurl + "/classes-per-subscriptions")
                .bodyValue(subscriptionsDTO)
                .retrieve()
                .bodyToFlux(ClassDTO.class)
                .collectList()
                .block();

        // Now we create the calendar
        HashMap<String, List<?>> calendar = new HashMap<>();
        calendar.put("facultyEvents", facultyEventsDTO);
        calendar.put("groupEvents", groupEventsDTO);
        calendar.put("classes", classes);
        return calendar;

    }

    public byte[] generateIcs(String userId, boolean completeCalendar) throws Exception {

        IcsGenerator icsGenerator = new IcsGenerator();
        if (completeCalendar) {
            // Obtener el calendario completo
            HashMap<String, List<?>> classes = getEntireCalendar(userId);
            if (classes.isEmpty()) {
                throw new Exception("No se encontraron clases para el usuario");
            }
            // Generar el archivo .ics para el calendario completo
            return icsGenerator.generateCompleteICalendar(classes);
        } else {
            // Obtener solo las clases
            List<ClassDTO> classes = getClasses(userId);
            if (classes.isEmpty()) {
                throw new Exception("No se encontraron clases para el usuario");
            }
            // Generar el archivo .ics para las clases
            return icsGenerator.generateICalendar(classes);
        }
    }

    public byte[] getIcsWithoutCredentials(String identifier) throws Exception {

        String userId = getUserIdFromIdentifier(identifier);
        if (userId == null) {
            return null;
        }

        // TODO: If generateIcs y successful, we return the file and save in /calendar.
        // Else if the file exists in /calendar and return it.
        // Else we return null.

        return this.generateIcs(userId, true);
    }

    public String getSyncUrl(String userId) throws IOException {

        String identifier = this.getIdentifierFromUserId(userId);
        //return "https://tempus.ugr.es/calendarugr/v1/academic-subscription/calendar/" + identifier;
        return "https://minixatc.ugr.es/calendarugr/v1/academic-subscription/calendar/" + identifier;

    }

    @Transactional
    public Optional<Subscription> subscribe(String userId, SubscriptionDTO subscription) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(userId);

        // Check if the subscription already exists
        Subscription subscriptionAux = subscriptionRepository.findByStudentIdAndGradeNameAndSubjectNameAndGroupName(
                studentInteger,
                subscription.getGrade(),
                subscription.getSubject(),
                subscription.getGroup());

        if (!(subscriptionAux == null)) {
            logger.info("Subscription already exists");
            return Optional.empty();
        }

        // Now we need to call the schedule-consumer service to validate the
        // subscription

        Boolean isValid = webClientBuilder.build()
                .post()
                .uri(scheduleConsumerurl + "/subscription-validation")
                .bodyValue(subscription)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (!isValid) {
            logger.info("Subscription is not valid");
            return Optional.empty();
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setStudentId(studentInteger);
        newSubscription.setFacultyName(subscription.getFaculty());
        newSubscription.setGradeName(subscription.getGrade());
        newSubscription.setSubjectName(subscription.getSubject());
        newSubscription.setGroupName(subscription.getGroup());
        newSubscription.setCreatedAt(java.time.LocalDateTime.now());
        newSubscription.setUpdatedAt(java.time.LocalDateTime.now());

        // Save the subscription
        Subscription subscriptionSaved = Optional.of(subscriptionRepository.save(newSubscription)).orElse(null);

        if (subscriptionSaved == null) {
            logger.info("Error saving subscription");
            return Optional.empty();
        }

        return Optional.of(subscriptionSaved);
    }

    @Transactional
    public List<SubscriptionDTO> subscribeBatching(String userId, List<SubscriptionDTO> subscriptions) {

        if (subscriptions.isEmpty()) {
            return List.of();
        }

        for (SubscriptionDTO subscription : subscriptions) {
            // Pass the String to Integer
            Integer studentInteger = Integer.parseInt(userId);

            if (subscription.getGroup() == null || subscription.getGroup().isEmpty()) {
                return List.of();
            }
            if (subscription.getGrade() == null || subscription.getGrade().isEmpty()) {
                return List.of();
            }
            if (subscription.getSubject() == null || subscription.getSubject().isEmpty()) {
                return List.of();
            }
            if (subscription.getFaculty() == null || subscription.getFaculty().isEmpty()) {
                return List.of();
            }

            // Check if the subscription already exists
            Subscription subscriptionAux = subscriptionRepository.findByStudentIdAndGradeNameAndSubjectNameAndGroupName(
                    studentInteger,
                    subscription.getGrade(), subscription.getSubject(), subscription.getGroup());

            if (!(subscriptionAux == null)) {
                return List.of();
            }

            // Now we need to call the schedule-consumer service to validate the
            // subscription
            Boolean isValid = webClientBuilder.build()
                    .post()
                    .uri(scheduleConsumerurl + "/subscription-validation")
                    .bodyValue(subscription)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (!isValid) {
                return List.of();
            }

            Subscription newSubscription = new Subscription();
            newSubscription.setStudentId(studentInteger);
            newSubscription.setFacultyName(subscription.getFaculty());
            newSubscription.setGradeName(subscription.getGrade());
            newSubscription.setSubjectName(subscription.getSubject());
            newSubscription.setGroupName(subscription.getGroup());
            newSubscription.setCreatedAt(java.time.LocalDateTime.now());
            newSubscription.setUpdatedAt(java.time.LocalDateTime.now());

            subscriptionRepository.save(newSubscription);
        }

        return subscriptions;
    }

    @Transactional
    public boolean removeSubscriptionsByGrade(String userId, String grade) {
        // Pass the String to INteger
        Integer studentInteger = Integer.parseInt(userId);

        // Check if there is subscriptions for the user in the grade
        List<Subscription> subscriptions = subscriptionRepository.findByStudentIdAndGradeName(studentInteger, grade);

        if (subscriptions.isEmpty()) {
            return false;
        }

        // Remove the subscriptions
        subscriptionRepository.deleteAll(subscriptions);

        return true;
    }

    @Transactional
    public boolean removeSubscription(String userId, String grade, String subject, String group) {
        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(userId);

        // Check if there is a subscription for the user in the grade, subject and group
        Subscription subscription = subscriptionRepository
                .findByStudentIdAndGradeNameAndSubjectNameAndGroupName(studentInteger, grade, subject, group);

        if (subscription == null) {
            logger.info("Subscription not found");
            return false;
        }

        // Remove the subscription
        subscriptionRepository.delete(subscription);

        return true;
    }

    public ExtraClassDTO createGroupEvent(String userId, ExtraClassDTO extraClassDTO) {
        // Pass the String top Integer
        Integer userInteger = Integer.parseInt(userId);

        LocalDate date = extraClassDTO.getDate();

        // Default date to save with the time
        LocalDate defaultDate = LocalDate.of(2000, 1, 1);

        LocalDateTime initHour = LocalDateTime.of(defaultDate, extraClassDTO.getInitHour());
        LocalDateTime finishHour = LocalDateTime.of(defaultDate, extraClassDTO.getFinishHour());

        // Check if the extraClass does not create conflicts with another extraClass
        
        /* Actually we want this kind of events to be able to overlap with other (Ex exams, special class events ...)
        List<ExtraClasses> extraClasses = extraClassesRepository.findConflictingClassesOnGroupEvent(
                extraClassDTO.getFacultyName(),
                date,
                extraClassDTO.getClassroom(),
                initHour,
                finishHour);

        if (!extraClasses.isEmpty()) {
            logger.info("ExtraClass already exists");
            return null;
        }*/

        extraClassDTO.setType("GROUP");

        // Check if the extraClass does not creat conflicts with the regular classes

        /* We also could want to allow this kind of events to overlap with other official classes
        Boolean isValid = webClientBuilder.build()
                .post()
                .uri(scheduleConsumerurl + "/extraclass-validation")
                .bodyValue(extraClassDTO)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (!isValid) {
            logger.info("ExtraClass is not valid");
            return null;
        }*/

        ExtraClasses extraClass = new ExtraClasses();
        extraClass.setIdUser(userInteger.toString());
        extraClass.setFacultyName(extraClassDTO.getFacultyName());
        extraClass.setGradeName(extraClassDTO.getGradeName());
        extraClass.setSubjectName(extraClassDTO.getSubjectName());
        extraClass.setGroupName(extraClassDTO.getGroupName());
        extraClass.setDay(extraClassDTO.getDay());
        extraClass.setDate(date);
        extraClass.setInitHour(initHour);
        extraClass.setFinishHour(finishHour);
        extraClass.setTeacher(extraClassDTO.getTeacher());
        extraClass.setClassroom(extraClassDTO.getClassroom());
        extraClass.setTitle(extraClassDTO.getTitle());
        extraClass.setType("GROUP");
        extraClass.setCreatedAt(java.time.LocalDateTime.now());
        extraClass.setUpdatedAt(java.time.LocalDateTime.now());

        try {
            extraClassesRepository.save(extraClass);
        } catch (Exception e) {
            logger.error("Error al guardar la extraClass: " + e.getMessage());
            return null;
        }

        extraClassDTO.setId_user(userId);
        extraClassDTO.setType("GROUP");

        // Before returning the extraClassDTO we are going to get all the emails of the
        // Students in the subject group
        // and with the notifications on, to send them emails with the information

        // We get a list with the unique ids of users with subscription to the subject
        // group
        List<IdDTO> ids = subscriptionRepository.findStudentsIdByGradeNameAndSubjectNameAndGroupName(
                extraClassDTO.getGradeName(),
                extraClassDTO.getSubjectName(),
                extraClassDTO.getGroupName());

        List<Long> idsLong = ids.stream().map(IdDTO::getStudentId).distinct().toList();

        // Now we need to get the emails of the users with the notifications on
        List<String> emails = new ArrayList<>();
        try {
            // Call the user-service to get the emails of the users
            List<?> rawEmails = webClientBuilder.build()
                    .post()
                    .uri(userUrl + "/email-list")
                    .bodyValue(idsLong)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .block();

            // This is because we get [ [ ... ] ], and we want to get [ ... ]
            if (!rawEmails.isEmpty() && rawEmails.get(0) instanceof String
                    && ((String) rawEmails.get(0)).startsWith("[") && ((String) rawEmails.get(0)).endsWith("]")) {
                ObjectMapper objectMapper = new ObjectMapper();
                emails = objectMapper.readValue((String) rawEmails.get(0), new TypeReference<List<String>>() {
                });
            } else {
                emails = rawEmails.stream()
                        .map(Object::toString)
                        .toList();
            }

        } catch (Exception e) {
            logger.error("Error al obtener los correos de los usuarios: " + e.getMessage());
        }

        // Now we need to send the emails to the users

        try {
            sendNotificationEmail(emails, extraClassDTO);
        } catch (Exception e) {
            logger.error("Error al enviar el correo: " + e.getMessage());
        }

        return extraClassDTO;
    }

    public boolean removeGroupEvent(String userId, String eventId) {

        // Check if there is a extraClass for the user in the eventId
        ExtraClasses extraClass = extraClassesRepository.findById(eventId).orElse(null);

        if (extraClass == null || !extraClass.getIdUser().equals(userId) || !extraClass.getType().equals("GROUP")) {
            return false;
        }

        // Remove the extraClass
        extraClassesRepository.delete(extraClass);

        return true;
    }

    public ExtraClassDTO createFacultyEvent(String userId, ExtraClassDTO extraClassDTO) {
        Integer userInteger = Integer.parseInt(userId);

        LocalDate date = extraClassDTO.getDate();
        // Default date to save with the time
        LocalDate defaultDate = LocalDate.of(2000, 1, 1);

        LocalDateTime initHour = LocalDateTime.of(defaultDate, extraClassDTO.getInitHour());
        LocalDateTime finishHour = LocalDateTime.of(defaultDate, extraClassDTO.getFinishHour());

        // Check if there is no extraClass with type FACULTY the same day, date,
        // initHour and finishHour and facultyName

        /* Maybe there is a use case where we want to allow this kind of events to overlap with other faculty events
        List<ExtraClasses> extraClasses = extraClassesRepository.findConflictingClassesOnFacultyEvent(
                extraClassDTO.getFacultyName(),
                date,
                initHour,
                finishHour,
                extraClassDTO.getTitle());

        if (!extraClasses.isEmpty()) {
            logger.info("ExtraClass already exists");
            return null;
        }*/

        ExtraClasses extraClass = new ExtraClasses();
        extraClass.setIdUser(userInteger.toString());
        extraClass.setFacultyName(extraClassDTO.getFacultyName());
        extraClass.setGradeName(null);
        extraClass.setSubjectName(null);
        extraClass.setGroupName(null);
        extraClass.setDay(extraClassDTO.getDay());
        extraClass.setDate(date);
        extraClass.setInitHour(initHour);
        extraClass.setFinishHour(finishHour);
        extraClass.setTeacher(null);
        extraClass.setClassroom(null);
        extraClass.setTitle(extraClassDTO.getTitle());
        extraClass.setType("FACULTY");
        extraClass.setCreatedAt(java.time.LocalDateTime.now());
        extraClass.setUpdatedAt(java.time.LocalDateTime.now());

        try {
            extraClassesRepository.save(extraClass);
        } catch (Exception e) {
            return null;
        }

        extraClassDTO.setId_user(userId);
        extraClassDTO.setType("FACULTY");

        return extraClassDTO;
    }

    public boolean removeFacultyEvent(String userId, String eventId) {
        ExtraClasses extraClass = extraClassesRepository.findById(eventId).orElse(null);

        if (extraClass == null || !extraClass.getIdUser().equals(userId) || !extraClass.getType().equals("FACULTY")) {
            return false;
        }

        // Remove the extraClass
        extraClassesRepository.delete(extraClass);
        return true;
    }

    public List<SubscriptionDTO> getSubscriptions(String userId) {
        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(userId);

        // Get the subscriptions of the user
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        if (subscriptions.isEmpty()) {
            return List.of();
        }

        // From List<Subscription> to List<SubscriptionDTO>
        List<SubscriptionDTO> subscriptionsDTO = SubscriptionMapper.toDTOList(subscriptions);

        return subscriptionsDTO;
    }

    public List<ExtraClassDTO> getGroupEvents(String userId) {
        // From extraclasses, get the classes with type GROUP with the userId
        List<ExtraClasses> extraClasses = extraClassesRepository.findByIdUserAndType(userId, "GROUP");
        if (extraClasses.isEmpty()) {
            return List.of();
        }
        // From List<ExtraClasses> to List<ExtraClassDTO>
        List<ExtraClassDTO> extraClassesDTO = ExtraClassMapper.toDTOList(extraClasses);
        return extraClassesDTO;
    }

    public FacultyGroupEventsDTO getFacultyGroupEvents(String userId) {
        FacultyGroupEventsDTO facultyGroupEventsDTO = new FacultyGroupEventsDTO();

        // From extraclasses, get the classes with type GROUP with the userId
        List<ExtraClasses> extraClasses = extraClassesRepository.findByIdUserAndType(userId, "GROUP");

        // From List<ExtraClasses> to List<ExtraClassDTO>
        List<ExtraClassDTO> extraClassesDTO = ExtraClassMapper.toDTOList(extraClasses);
        facultyGroupEventsDTO.setGroupEvents(extraClassesDTO);

        // From extraclasses, get the classes with type FACULTY with the userId
        List<ExtraClasses> extraClassesFaculty = extraClassesRepository.findByIdUserAndType(userId, "FACULTY");

        // From List<ExtraClasses> to List<ExtraClassDTO>
        List<ExtraClassDTO> extraClassesFacultyDTO = ExtraClassMapper.toDTOList(extraClassesFaculty);
        facultyGroupEventsDTO.setFacultyEvents(extraClassesFacultyDTO);

        return facultyGroupEventsDTO;
    }

}
