package com.calendarugr.academic_subscription_service.mappers;

import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.Subscription;

import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionMapper {

    // Convertir de Subscription a SubscriptionDTO
    public static SubscriptionDTO toDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setFaculty(subscription.getFacultyName());
        dto.setGrade(subscription.getGradeName());
        dto.setSubject(subscription.getSubjectName());
        dto.setGroup(subscription.getGroupName());
        return dto;
    }

    // Convertir de SubscriptionDTO a Subscription
    public static Subscription toEntity(SubscriptionDTO dto, Integer studentId) {
        Subscription subscription = new Subscription();
        subscription.setStudentId(studentId);
        subscription.setFacultyName(dto.getFaculty());
        subscription.setGradeName(dto.getGrade());
        subscription.setSubjectName(dto.getSubject());
        subscription.setGroupName(dto.getGroup());
        subscription.setCreatedAt(java.time.LocalDateTime.now());
        subscription.setUpdatedAt(java.time.LocalDateTime.now());
        return subscription;
    }

    // Convertir una lista de Subscription a una lista de SubscriptionDTO
    public static List<SubscriptionDTO> toDTOList(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(SubscriptionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Convertir una lista de SubscriptionDTO a una lista de Subscription
    public static List<Subscription> toEntityList(List<SubscriptionDTO> dtos, Integer studentId) {
        return dtos.stream()
                .map(dto -> toEntity(dto, studentId))
                .collect(Collectors.toList());
    }
}