package com.calendarugr.academic_subscription_service.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.calendarugr.academic_subscription_service.dtos.FacultyGroupEventsDTO;
import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.Subscription;
import com.calendarugr.academic_subscription_service.services.AcademicSubscriptionServiceAlwaysData;
import com.calendarugr.academic_subscription_service.services.AcademicSubscriptionServiceReverseProxy;

@RestController
@RequestMapping("/academic-subscription")
public class AcademicSubscriptionController {

    @Autowired 
    private AcademicSubscriptionServiceReverseProxy academicSubscriptionService;

    @GetMapping("/classes") // We get X-User-Id from the request headers
    public ResponseEntity<?> getClasses(@RequestHeader("X-User-Id") String userId) {
        List<ClassDTO> classes = academicSubscriptionService.getClasses(userId);
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No se encontraron clases para el usuario");
        }
        return ResponseEntity.ok(classes);        
    }

    @GetMapping("/entire-calendar")
    public ResponseEntity<?> getEntireCalendar(@RequestHeader("X-User-Id") String userId) {
        HashMap<String,List<?>> classes = academicSubscriptionService.getEntireCalendar(userId);
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No se encontraron clases para el usuario");
        }
        return ResponseEntity.ok(classes);        
    }

    @GetMapping("/subscriptions") 
    public ResponseEntity<?> getSubscription(@RequestHeader("X-User-Id") String userId) {
        List<SubscriptionDTO> subscriptions = academicSubscriptionService.getSubscriptions(userId);
        if (subscriptions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No se encontraron suscripciones para el usuario");
        }
        return ResponseEntity.ok(subscriptions);       
    }

    @PostMapping("/subscription") // We get X-User-Id from the request headers
    public ResponseEntity<?> subscribe(@RequestHeader("X-User-Id") String userId, @RequestBody SubscriptionDTO subscription) {
        Optional<Subscription> sub = academicSubscriptionService.subscribe(userId, subscription);
        if (sub.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La suscripción ya existe o no es válida");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(sub.get());    
    }

    @PostMapping("/subscription-batching") // We get X-User-Id from the request headers
    public ResponseEntity<?> subscribeBatching(@RequestHeader("X-User-Id") String userId, @RequestBody List<SubscriptionDTO> subscriptions) {
        List<SubscriptionDTO> subs = academicSubscriptionService.subscribeBatching(userId, subscriptions);
        if (subs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Hubo un error al crear las suscripciones,comprueba si tienen buen formato o ya existen");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(subs);    
    }

    @GetMapping("/ics")
    public ResponseEntity<?> downloadCalendar(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "true") boolean completeCalendar) {
    
        try {

            byte[] icsFile = academicSubscriptionService.generateIcs(userId, completeCalendar);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=TempusUGR.ics");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/calendar");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(icsFile.length));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(icsFile); 

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Hubo un error construyendo el .ics: " + e.getMessage());
        }
    }

    @GetMapping("/calendar/{identifier}")
    public ResponseEntity<?> getIcsWithoutCredentials(@PathVariable String identifier) {
        try {
            byte[] icsFile = academicSubscriptionService.getIcsWithoutCredentials(identifier);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=TempusUGR.ics");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/calendar");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(icsFile.length));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(icsFile); 

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Hubo un error construyendo el .ics: " + e.getMessage());
        }
    }

    @GetMapping("/sync-url")
    public ResponseEntity<?> getSyncUrl(@RequestHeader("X-User-Id") String userId) throws IOException {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El id de usuario no puede estar vacío");
        }

        String syncUrl = academicSubscriptionService.getSyncUrl(userId);
        if (syncUrl == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No se encontraron clases para el usuario");
        }
        return ResponseEntity.ok(syncUrl);        
    }

    @DeleteMapping("/subscription-grade")
    public ResponseEntity<?> removeGrade(@RequestHeader("X-User-Id") String userId, @RequestParam String grade){
        boolean removed = academicSubscriptionService.removeSubscriptionsByGrade(userId, grade);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron suscripciones para el usuario");
        }
        return ResponseEntity.ok("Suscripciones eliminadas correctamente");
    }

    @DeleteMapping("/subscription")
    public ResponseEntity<?> removeSubscription(@RequestHeader("X-User-Id") String userId, @RequestParam String grade, @RequestParam String subject, @RequestParam String group){
        boolean removed = academicSubscriptionService.removeSubscription(userId, grade, subject, group);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron suscripciones para el usuario");
        }
        return ResponseEntity.ok("Suscripciones eliminadas correctamente");
    }

    @GetMapping("/group-event") // The group events that a user has created
    public ResponseEntity<?> getGroupEvents(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole) {
        
        if (!userRole.equals("ROLE_TEACHER") && !userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para ver eventos de grupo");
        }

        List<ExtraClassDTO> extraClasses = academicSubscriptionService.getGroupEvents(userId);
        return ResponseEntity.ok(extraClasses);        
    }

    @PostMapping("/group-event")
    public ResponseEntity<?> createGroupEvent(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole, @RequestBody ExtraClassDTO extraClassDTO) {
        
        if (!userRole.equals("ROLE_TEACHER") && !userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para crear eventos de grupo");
        }

        if (extraClassDTO.getDate() == null || extraClassDTO.getDay() == null || extraClassDTO.getInitHour() == null || extraClassDTO.getFinishHour() == null 
                || extraClassDTO.getFacultyName() == null || extraClassDTO.getTitle() == null || extraClassDTO.getTeacher() == null 
            || extraClassDTO.getGroupName() == null || extraClassDTO.getSubjectName() == null || extraClassDTO.getGradeName() == null || extraClassDTO.getClassroom() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos para crear el evento");
        }

        // If classroom, date, day, init hour, finish hour or faculty name are ""
        if (extraClassDTO.getClassroom().isEmpty() || extraClassDTO.getDate() == null || extraClassDTO.getDay().isEmpty() || extraClassDTO.getInitHour() == null || extraClassDTO.getFinishHour() == null 
                || extraClassDTO.getFacultyName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos para crear el evento");
        }

        ExtraClassDTO extraClass = academicSubscriptionService.createGroupEvent(userId, extraClassDTO);
        if (extraClass == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Hubo un error al crear el evento, ya existe o coincide con otra clase");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(extraClass);
    }

    @DeleteMapping("/group-event")
    public ResponseEntity<?> removeGroupEvent(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole, @RequestParam String eventId) {
        
        if (!userRole.equals("ROLE_TEACHER") && !userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar eventos de grupo");
        }

        boolean removed = academicSubscriptionService.removeGroupEvent(userId, eventId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron eventos para el usuario");
        }
        return ResponseEntity.ok("Evento eliminado correctamente");
    }

    @GetMapping("/faculty-group-event") // The faculty and group events that a user has created
    public ResponseEntity<?> getFacultyGroupEvents(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole) {
        
        if (!userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para ver eventos de grupo");
        }

        FacultyGroupEventsDTO extraClasses = academicSubscriptionService.getFacultyGroupEvents(userId);
        return ResponseEntity.ok(extraClasses);        
    }

    @PostMapping("/faculty-event")
    public ResponseEntity<?> createFacultyEvent(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole, @RequestBody ExtraClassDTO extraClassDTO) {
        
        if (!userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para crear eventos de facultad");
        }

        if (extraClassDTO.getDate() == null || extraClassDTO.getDay() == null || extraClassDTO.getInitHour() == null || extraClassDTO.getFinishHour() == null 
                || extraClassDTO.getFacultyName() == null || extraClassDTO.getTitle() == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos para crear el evento");
        }

        if ( extraClassDTO.getDate() == null || extraClassDTO.getDay().isEmpty() || extraClassDTO.getInitHour() == null || extraClassDTO.getFinishHour() == null 
                || extraClassDTO.getFacultyName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos para crear el evento");
        }

        ExtraClassDTO extraClass = academicSubscriptionService.createFacultyEvent(userId, extraClassDTO);
        if (extraClass == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Hubo un error al crear el evento, ya existe o coincide con otra clase");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(extraClass);
    }

    @DeleteMapping("/faculty-event")
    public ResponseEntity<?> removeFacultyEvent(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-User-Role") String userRole, @RequestParam String eventId) {
        
        if (!userRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para eliminar eventos de facultad");
        }

        boolean removed = academicSubscriptionService.removeFacultyEvent(userId, eventId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron eventos para el usuario");
        }
        return ResponseEntity.ok("Evento eliminado correctamente");
    }
    
}
