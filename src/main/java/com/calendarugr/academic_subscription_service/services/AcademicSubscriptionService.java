package com.calendarugr.academic_subscription_service.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.ExtraClasses;
import com.calendarugr.academic_subscription_service.entities.Subscription;
import com.calendarugr.academic_subscription_service.repositories.ExtraClassesRepository;
import com.calendarugr.academic_subscription_service.repositories.SubscriptionRepository;

// UNCOMMENT TO USE ZIPKIN
//import io.micrometer.observation.Observation;
//import io.micrometer.observation.ObservationRegistry;

@Service
public class AcademicSubscriptionService {

    @Value("${server.port}")
    private String port;

    // UNCOMMENT TO USE ZIPKIN
    // @Autowired
    // private ObservationRegistry observationRegistry;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ExtraClassesRepository extraClassesRepository;

    String url = "http://schedule-consumer-service/schedule-consumer";

    public List<ClassDTO> getClasses(String studentId) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(studentId);
        // First we need to get the subscriptions of the user
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        if (subscriptions.isEmpty()) {
            return List.of();
        }

        // From List<Subscription> to List<SubscriptionDTO>
        List<SubscriptionDTO> subscriptionsDTO = subscriptions.stream().map(subscription -> {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setFaculty(subscription.getFacultyName());
            subscriptionDTO.setGrade(subscription.getGradeName());
            subscriptionDTO.setSubject(subscription.getSubjectName());
            subscriptionDTO.setGroup(subscription.getGroupName());
            return subscriptionDTO;
        }).toList();

        // UNCOMMENT TO USE ZIPKIN 
        // Observation observation = Observation.createNotStarted("getClasses", observationRegistry);

        // return observation.observe(() -> {
        //     // Call the schedule-consumer service to get the classes
        //     List<ClassDTO> classes = webClientBuilder.build()
        //         .post()
        //         .uri(url + "/classes-per-subscriptions")
        //         .bodyValue(subscriptionsDTO)
        //         .retrieve()
        //         .bodyToFlux(ClassDTO.class)
        //         .collectList()
        //         .block();
    
        //     return classes;
        // }); 
        
        
        // COMMENT TO NOT TO USE ZIPKIN -------------------------------
        List<ClassDTO> classes = webClientBuilder.build()
            .post()
            .uri(url + "/classes-per-subscriptions")
            .bodyValue(subscriptionsDTO)
            .retrieve()
            .bodyToFlux(ClassDTO.class)
            .collectList()
            .block();

        return classes;
        // ------------------------------------------------------------
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
            subscription.getGroup()
        );

        if (!(subscriptionAux == null)) {
            return Optional.empty();
        }

        // Now we need to call the schedule-consumer service to validate the subscription

        Boolean  isValid = webClientBuilder.build()
            .post()
            .uri(url + "/validate-subscription")
            .bodyValue(subscription)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();

        if (!isValid) {
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

        return Optional.of(subscriptionRepository.save(newSubscription));

    }

    @Transactional
    public List<SubscriptionDTO> subscribeBatching(String userId, List<SubscriptionDTO> subscriptions) {
        
        if (subscriptions.isEmpty()) {
            return List.of();
        }
        
        for (SubscriptionDTO subscription : subscriptions) {
            // Pass the String to Integer
            Integer studentInteger = Integer.parseInt(userId);

            // Check if the subscription already exists
            Subscription subscriptionAux = subscriptionRepository.findByStudentIdAndGradeNameAndSubjectNameAndGroupName(studentInteger, 
                subscription.getGrade(), subscription.getSubject(), subscription.getGroup());

            if (!(subscriptionAux == null)) {
                return List.of();
            }
                
            // Now we need to call the schedule-consumer service to validate the subscription
            Boolean  isValid = webClientBuilder.build()
                .post()
                .uri(url + "/validate-subscription")
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
        Subscription subscription = subscriptionRepository.findByStudentIdAndGradeNameAndSubjectNameAndGroupName(studentInteger, grade, subject, group);

        System.out.println("Subscription: " + subscription);

        if (subscription.equals(null)) {
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
        LocalDate defaultDate = LocalDate.of(2000,1,1);

        LocalDateTime initHour = LocalDateTime.of(defaultDate, extraClassDTO.getInitHour());
        LocalDateTime finishHour = LocalDateTime.of(defaultDate, extraClassDTO.getFinishHour());

        // Check if the extraClass does not create conflicts with another extraClass
        List<ExtraClasses> extraClasses = extraClassesRepository.findConflictingClassesOnGroupEvent(
            extraClassDTO.getFacultyName(),
            date,
            extraClassDTO.getClassroom(),
            initHour,
            finishHour
        );

        System.out.println("Extra Classes: " + extraClasses);
        System.out.println("Init Hour: " + initHour);
        System.out.println("Finish Hour: " + finishHour);

        if (!extraClasses.isEmpty()) {
            return null;
        }

        // Check if the extraClass does not creat conflicts with the regular classes
        Boolean isValid = webClientBuilder.build()
            .post()
            .uri(url + "/validate-extra-class")
            .bodyValue(extraClassDTO)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();

        if (!isValid) {
            return null;
        }

        ExtraClasses extraClass = new ExtraClasses();
        extraClass.setId_user(userInteger.toString());
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

        try{
            extraClassesRepository.save(extraClass);
        }catch (Exception e) {
            return null;
        }

        extraClassDTO.setId_user(userId);
        extraClassDTO.setType("GROUP");

        return extraClassDTO;
    }

    public boolean removeGroupEvent(String userId, String eventId) {

        // Check if there is a extraClass for the user in the eventId
        ExtraClasses extraClass = extraClassesRepository.findById(eventId).orElse(null);

        if (extraClass == null || !extraClass.getId_user().equals(userId) || !extraClass.getType().equals("GROUP")) {
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
        LocalDate defaultDate = LocalDate.of(2000,1,1);

        LocalDateTime initHour = LocalDateTime.of(defaultDate, extraClassDTO.getInitHour());
        LocalDateTime finishHour = LocalDateTime.of(defaultDate, extraClassDTO.getFinishHour());

        // Check if the extraClass does not create conflicts with another extraClass

        List<ExtraClasses> extraClasses = extraClassesRepository.findConflictingClassesOnFacultyEvent(
            extraClassDTO.getFacultyName(),
            date,
            initHour,
            finishHour
        );

        System.out.println("Extra Classes: " + extraClasses);
        System.out.println("Init Hour: " + initHour);
        System.out.println("Finish Hour: " + finishHour);

        if (!extraClasses.isEmpty()) {
            return null;
        }

        // Check if the extraClass does not creat conflicts with the regular classes
        Boolean isValid = webClientBuilder.build()
            .post()
            .uri(url + "/validate-extra-class")
            .bodyValue(extraClassDTO)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();
            
        System.out.println("Is Valid: " + isValid);
        if (!isValid) {
            return null;
        }

        ExtraClasses extraClass = new ExtraClasses();
        extraClass.setId_user(userInteger.toString());
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

        try{
            extraClassesRepository.save(extraClass);
        }catch (Exception e) {
            return null;
        }

        extraClassDTO.setId_user(userId);
        extraClassDTO.setType("FACULTY");

        return extraClassDTO;
    }

    public boolean removeFacultyEvent(String userId, String eventId) {
        ExtraClasses extraClass = extraClassesRepository.findById(eventId).orElse(null);
        System.out.println("Extra Class: " + extraClass);

        if (extraClass == null || !extraClass.getId_user().equals(userId) || !extraClass.getType().equals("FACULTY")) {
            return false;
        }

        // Remove the extraClass
        extraClassesRepository.delete(extraClass);
        return true;
    }

}
