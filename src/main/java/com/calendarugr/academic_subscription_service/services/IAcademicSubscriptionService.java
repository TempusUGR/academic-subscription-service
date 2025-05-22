package com.calendarugr.academic_subscription_service.services;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.ExtraClassDTO;
import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.Subscription;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface IAcademicSubscriptionService {

    List<ClassDTO> getClasses(String studentId);

    HashMap<String, List<?>> getEntireCalendar(String userId);

    public List<SubscriptionDTO> getSubscriptions(String userId);

    byte[] generateIcs(String userId, boolean completeCalendar) throws Exception;

    byte[] getIcsWithoutCredentials(String identifier) throws Exception;

    String getSyncUrl(String userId) throws IOException;

    Optional<Subscription> subscribe(String userId, SubscriptionDTO subscription);

    List<SubscriptionDTO> subscribeBatching(String userId, List<SubscriptionDTO> subscriptions);

    boolean removeSubscriptionsByGrade(String userId, String grade);

    boolean removeSubscription(String userId, String grade, String subject, String group);

    ExtraClassDTO createGroupEvent(String userId, ExtraClassDTO extraClassDTO);

    boolean removeGroupEvent(String userId, String eventId);

    ExtraClassDTO createFacultyEvent(String userId, ExtraClassDTO extraClassDTO);

    boolean removeFacultyEvent(String userId, String eventId);

}