package com.calendarugr.academic_subscription_service.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ClassDTO{

    private String classroom;

    private String day;

    private LocalDate initDate;

    private LocalDate finishDate;

    private LocalTime initHour;

    private LocalTime finishHour;

    private String group;

    private String subject;

    private String teachers;

    private String grade;

    private String subjectUrl;

}
