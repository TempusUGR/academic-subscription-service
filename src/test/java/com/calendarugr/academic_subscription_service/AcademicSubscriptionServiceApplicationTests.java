package com.calendarugr.academic_subscription_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AcademicSubscriptionServiceApplicationTests {

    private final String BASE_URL = "http://academic-subscription-service/academic-subscription";
    
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${api.key}") 
    private String apiKey;

    // This tests require the schedule information to be already in the database.

    @Test
    void testConflictWithExistingClassMiddle() {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(17, 40)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(18, 20));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("");
        conflictingClass.setTitle("Clase conflictiva");

        try {
            // Intentar insertar la clase conflictiva
            HttpStatusCode statusCode = webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/create-group-event" )
                .body(Mono.just(conflictingClass), ExtraClassDTO.class)
                .header("X-Api-Key", apiKey) 
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode())
                .block();

            // Verificar que se detecta el conflicto
            assertEquals(HttpStatusCode.valueOf(409), statusCode);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    void testConflictWithExistingClassInit() {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(17, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(18, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("");
        conflictingClass.setTitle("Clase conflictiva");

        try {
            // Intentar insertar la clase conflictiva
            HttpStatusCode statusCode = webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/create-group-event" )
                .body(Mono.just(conflictingClass), ExtraClassDTO.class)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode())
                .block();

            // Verificar que se detecta el conflicto
            assertEquals(HttpStatusCode.valueOf(409), statusCode);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    void testConflictWithExistingClassFinish() {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(18, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(19, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("");
        conflictingClass.setTitle("Clase conflictiva");

        try {
            // Intentar insertar la clase conflictiva
            HttpStatusCode statusCode = webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/create-group-event" )
                .body(Mono.just(conflictingClass), ExtraClassDTO.class)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode())
                .block();

            // Verificar que se detecta el conflicto
            assertEquals(HttpStatusCode.valueOf(409), statusCode);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    void testConflictWithExistingClassFull() {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(17, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(19, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("");
        conflictingClass.setTitle("Clase conflictiva");

        try {
            // Intentar insertar la clase conflictiva
            HttpStatusCode statusCode = webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/create-group-event" )
                .body(Mono.just(conflictingClass), ExtraClassDTO.class)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode())
                .block();

            // Verificar que se detecta el conflicto
            assertEquals(HttpStatusCode.valueOf(409), statusCode);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}