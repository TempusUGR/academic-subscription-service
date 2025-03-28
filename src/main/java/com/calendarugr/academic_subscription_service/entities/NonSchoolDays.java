package com.calendarugr.academic_subscription_service.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "non_school_days")
public class NonSchoolDays {

    @Id
    private String id;

    @NotBlank(message = "Faculty name cannot be blank")
    @Size(max = 255, message = "Faculty name must not exceed 255 characters")
    private String faculty;

    private List<String> grades;

    @NotEmpty(message = "Days cannot be empty")
    private List<DayInfo> days;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DayInfo {
        @NotBlank(message = "Title for day cannot be blank")
        @Size(max = 200, message = "Title for day must not exceed 200 characters")
        private String title;

        @NotBlank(message = "Day cannot be blank")
        @Size(max = 20, message = "Day must not exceed 20 characters")
        private LocalDate date;

    }
}