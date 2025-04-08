package com.calendarugr.academic_subscription_service.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor
@Document(collection = "extra_classes")
public class ExtraClasses {

    @Id
    private String id;

    private String id_user; // This refers to the user who created the extra class

    @Size(max = 255, message = "Faculty name must not exceed 255 characters")
    private String facultyName;

    @Size(max = 255, message = "Grade name must not exceed 255 characters")
    private String gradeName;

    @Size(max = 255, message = "Subject name must not exceed 255 characters")
    private String subjectName;

    @Size(max = 50, message = "Group name must not exceed 50 characters")
    private String groupName;

    @NotNull(message = "Day cannot be null")
    @Size(max = 20, message = "Day must not exceed 20 characters") 
    private String day;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @NotNull(message = "Start hour cannot be null")
    private LocalDateTime initHour; 

    @NotNull(message = "Finish hour cannot be null")
    private LocalDateTime finishHour; 

    @NotNull(message = "Teacher cannot be null")
    @Size(max = 100, message = "Teacher must not exceed 100 characters")
    private String teacher;

    @NotNull(message = "Classroom cannot be null")
    @Size(max = 50, message = "Classroom must not exceed 50 characters")
    private String classroom;

    @NotNull(message = "Title cannot be null")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Type cannot be null")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    @Pattern(regexp = "GROUP|FACULTY", message = "Type must be either 'GROUP' or 'FACULTY'")
    private String type;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}