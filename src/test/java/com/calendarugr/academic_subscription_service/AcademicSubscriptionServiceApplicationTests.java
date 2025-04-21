package com.calendarugr.academic_subscription_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;
import java.time.LocalTime;


// TO RUN THIS TEST YOU NEED TO LOCALLY RUN EUREKA, API GATEWAY AND SCHEDULE CONSUMER SERVICES
// WITHOUT USER AND MAIL YOU ARE NOT GOING TO GET THE FULL FUNCTIONALITY
// AND YOU NEED TO HAVE THE DATABASE WITH THE DATA

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademicSubscriptionServiceApplicationTests {

    private final String BASE_URL = "/academic-subscription";

    private static String apiKey;

	public AcademicSubscriptionServiceApplicationTests() {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("API_KEY", dotenv.get("API_KEY"));
		apiKey = dotenv.get("API_KEY");
	}
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // This tests require the schedule information to be already in the database.

    @Test
    void testConflictWithExistingClassMiddle() throws Exception {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 22));
        conflictingClass.setInitHour(LocalTime.of(15, 40)); // Conflicto con 15:30 - 17:30
        conflictingClass.setFinishHour(LocalTime.of(17, 20));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("E.T.S. de Ingenierías Informática y de Telecomunicación");
        conflictingClass.setTitle("Clase conflictiva");
        conflictingClass.setType("GROUP");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/create-group-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingClass))
                .header("X-Api-Key", apiKey)
                .header("X-User-Id", "123456")
                .header("X-User-Role", "ROLE_TEACHER"))
            .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testBadRequestNoFaculty() throws Exception {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 22));
        conflictingClass.setInitHour(LocalTime.of(15, 40)); // Conflicto con 15:30 - 17:30
        conflictingClass.setFinishHour(LocalTime.of(17, 20));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("");
        conflictingClass.setTitle("Clase conflictiva");
        conflictingClass.setType("GROUP");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/create-group-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingClass))
                .header("X-Api-Key", apiKey)
                .header("X-User-Id", "123456")
                .header("X-User-Role", "ROLE_TEACHER"))
            .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testConflictWithExistingClassInit() throws Exception {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(14, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(16, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("E.T.S. de Ingenierías Informática y de Telecomunicación");
        conflictingClass.setTitle("Clase conflictiva");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/create-group-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingClass))
                .header("X-Api-Key", apiKey)
                .header("X-User-Id", "123456")
                .header("X-User-Role", "ROLE_TEACHER"))
            .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testConflictWithExistingClassFinish() throws Exception {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(16, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(19, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("E.T.S. de Ingenierías Informática y de Telecomunicación");
        conflictingClass.setTitle("Clase conflictiva");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/create-group-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingClass))
                .header("X-Api-Key", apiKey)
                .header("X-User-Id", "123456")
                .header("X-User-Role", "ROLE_TEACHER"))
            .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testConflictWithExistingClassFull() throws Exception {
        // Clase extra que genera conflicto con la clase existente en la base de datos
        ExtraClassDTO conflictingClass = new ExtraClassDTO();
        conflictingClass.setClassroom("06");
        conflictingClass.setDay("Viernes");
        conflictingClass.setDate(LocalDate.of(2024, 11, 25));
        conflictingClass.setInitHour(LocalTime.of(14, 0)); // Conflicto con 17:30 - 18:30
        conflictingClass.setFinishHour(LocalTime.of(19, 0));
        conflictingClass.setGroupName("A");
        conflictingClass.setSubjectName("Ingeniería de Servidores");
        conflictingClass.setTeacher("Héctor Emilio Pomares Cintas");
        conflictingClass.setGradeName("Grado en Ingeniería Informática");
        conflictingClass.setFacultyName("E.T.S. de Ingenierías Informática y de Telecomunicación");
        conflictingClass.setTitle("Clase conflictiva");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/create-group-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conflictingClass))
                .header("X-Api-Key", apiKey)
                .header("X-User-Id", "123456")
                .header("X-User-Role", "ROLE_TEACHER"))
            .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }
    
}