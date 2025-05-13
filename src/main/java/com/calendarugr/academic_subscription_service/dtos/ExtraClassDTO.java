package com.calendarugr.academic_subscription_service.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ExtraClassDTO {

    private String id_user; 

    private String facultyName;

    private String gradeName;

    private String subjectName;

    private String groupName;

    private String day;

    private LocalDate date;

    private LocalTime initHour; 

    private LocalTime finishHour; 

    private String teacher;

    private String classroom;

    private String title;

    private String type;

    private String eventId;
    
}
