package com.calendarugr.academic_subscription_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final String BASE_URL = "http://localhost:8090/academic-subscription/create-group-event";
    
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void testConflictWithExistingClass() {
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
                .uri(BASE_URL)
                .body(Mono.just(conflictingClass), ExtraClassDTO.class)
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
    void testNoConflictWithExistingClass() {
        // Clase extra que no genera conflicto con la clase existente
        ExtraClassDTO nonConflictingClass = new ExtraClassDTO();
        nonConflictingClass.setClassroom("06");
        nonConflictingClass.setDay("Viernes");
        nonConflictingClass.setDate(LocalDate.of(2024, 11, 25));
        nonConflictingClass.setInitHour(LocalTime.of(15, 30)); // No conflicto con 17:30 - 18:30
        nonConflictingClass.setFinishHour(LocalTime.of(16, 30));
        nonConflictingClass.setGroupName("A");
        nonConflictingClass.setSubjectName("Ingeniería de Servidores");
        nonConflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        nonConflictingClass.setGradeName("Grado en Ingeniería Informática");
        nonConflictingClass.setFacultyName("");
        nonConflictingClass.setTitle("Clase no conflictiva");

        HttpStatusCode statusCode = webClientBuilder.build()
            .post()
            .uri(BASE_URL)
            .body(Mono.just(nonConflictingClass), ExtraClassDTO.class)
            .retrieve()
            .toBodilessEntity()
            .map(response -> response.getStatusCode())
            .block();

        // Verificar que la clase se inserta correctamente
        assertEquals(HttpStatusCode.valueOf(201), statusCode);
    }

    @Test
    void testExactConflictWithExistingClass() {
        // Clase extra que coincide exactamente con la clase existente
        ExtraClassDTO exactConflictClass = new ExtraClassDTO();
        exactConflictClass.setClassroom("06");
        exactConflictClass.setDay("Viernes");
        exactConflictClass.setDate(LocalDate.of(2024, 11, 25));
        exactConflictClass.setInitHour(LocalTime.of(17, 30)); // Exactamente igual a 17:30 - 18:30
        exactConflictClass.setFinishHour(LocalTime.of(18, 30));
        exactConflictClass.setGroupName("A");
        exactConflictClass.setSubjectName("Ingeniería de Servidores");
        exactConflictClass.setTeacher("Héctor Emilio Pomares Cintas");
        exactConflictClass.setGradeName("Grado en Ingeniería Informática");
        exactConflictClass.setFacultyName("");
        exactConflictClass.setTitle("Clase conflictiva exacta");

        HttpStatusCode statusCode = webClientBuilder.build()
            .post()
            .uri(BASE_URL)
            .body(Mono.just(exactConflictClass), ExtraClassDTO.class)
            .retrieve()
            .toBodilessEntity()
            .map(response -> response.getStatusCode())
            .block();

        // Verificar que se detecta el conflicto exacto
        assertEquals(HttpStatusCode.valueOf(409), statusCode);
    }
}