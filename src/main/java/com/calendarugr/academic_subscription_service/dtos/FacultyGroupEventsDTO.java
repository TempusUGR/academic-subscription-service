package com.calendarugr.academic_subscription_service.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class FacultyGroupEventsDTO {
    List<ExtraClassDTO> groupEvents;
    List<ExtraClassDTO> facultyEvents;
}
