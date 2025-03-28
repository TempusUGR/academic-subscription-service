package com.calendarugr.academic_subscription_service.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;

    @NotNull(message = "Student ID cannot be null")
    private Integer studentId;

    @NotNull(message = "Grade name cannot be null")
    @Size(max = 255, message = "Grade name must not exceed 255 characters")
    private String gradeName;

    @Size(max = 255, message = "Subject name must not exceed 255 characters")
    private String subjectName;

    @Size(max = 50, message = "Group name must not exceed 50 characters")
    private String groupName;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
