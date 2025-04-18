package com.calendarugr.academic_subscription_service.mappers;

import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.calendarugr.academic_subscription_service.entities.ExtraClasses;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ExtraClassMapper {

    // Convertir de ExtraClasses a ExtraClassDTO
    public static ExtraClassDTO toDTO(ExtraClasses extraClass) {
        ExtraClassDTO dto = new ExtraClassDTO();
        dto.setId_user(extraClass.getId_user());
        dto.setFacultyName(extraClass.getFacultyName());
        dto.setGradeName(extraClass.getGradeName());
        dto.setSubjectName(extraClass.getSubjectName());
        dto.setGroupName(extraClass.getGroupName());
        dto.setDay(extraClass.getDay());
        dto.setDate(extraClass.getDate());
        dto.setInitHour(extraClass.getInitHour().toLocalTime());
        dto.setFinishHour(extraClass.getFinishHour().toLocalTime());
        dto.setTeacher(extraClass.getTeacher());
        dto.setClassroom(extraClass.getClassroom());
        dto.setTitle(extraClass.getTitle());
        dto.setType(extraClass.getType());
        return dto;
    }

    // Convertir de ExtraClassDTO a ExtraClasses
    public static ExtraClasses toEntity(ExtraClassDTO dto) {
        ExtraClasses extraClass = new ExtraClasses();
        extraClass.setId_user(dto.getId_user());
        extraClass.setFacultyName(dto.getFacultyName());
        extraClass.setGradeName(dto.getGradeName());
        extraClass.setSubjectName(dto.getSubjectName());
        extraClass.setGroupName(dto.getGroupName());
        extraClass.setDay(dto.getDay());
        extraClass.setDate(dto.getDate());

        // Convertir LocalTime a LocalDateTime con una fecha predeterminada
        LocalDateTime defaultDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        extraClass.setInitHour(LocalDateTime.of(defaultDate.toLocalDate(), dto.getInitHour()));
        extraClass.setFinishHour(LocalDateTime.of(defaultDate.toLocalDate(), dto.getFinishHour()));

        extraClass.setTeacher(dto.getTeacher());
        extraClass.setClassroom(dto.getClassroom());
        extraClass.setTitle(dto.getTitle());
        extraClass.setType(dto.getType());
        extraClass.setCreatedAt(LocalDateTime.now());
        extraClass.setUpdatedAt(LocalDateTime.now());
        return extraClass;
    }

    // Convertir una lista de ExtraClasses a una lista de ExtraClassDTO
    public static List<ExtraClassDTO> toDTOList(List<ExtraClasses> extraClasses) {
        return extraClasses.stream()
                .map(ExtraClassMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Convertir una lista de ExtraClassDTO a una lista de ExtraClasses
    public static List<ExtraClasses> toEntityList(List<ExtraClassDTO> dtos) {
        return dtos.stream()
                .map(ExtraClassMapper::toEntity)
                .collect(Collectors.toList());
    }
}