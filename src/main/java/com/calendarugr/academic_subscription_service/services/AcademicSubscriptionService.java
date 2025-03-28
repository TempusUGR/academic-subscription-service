package com.calendarugr.academic_subscription_service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.calendarugr.academic_subscription_service.dtos.ClassDTO;
import com.calendarugr.academic_subscription_service.dtos.SubscriptionDTO;
import com.calendarugr.academic_subscription_service.entities.Subscription;
import com.calendarugr.academic_subscription_service.repositories.SubscriptionRepository;

@Service
public class AcademicSubscriptionService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private final String scheduleConsumerUrl = "http://localhost:8083/schedule-consumer";

    public List<ClassDTO> getClasses(String studentId) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(studentId);
        // First we need to get the subscriptions of the user
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        // From List<Subscription> to List<SubscriptionDTO>
        List<SubscriptionDTO> subscriptionsDTO = subscriptions.stream().map(subscription -> {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setGrade(subscription.getGradeName());
            subscriptionDTO.setSubject(subscription.getSubjectName());
            subscriptionDTO.setGroup(subscription.getGroupName());
            return subscriptionDTO;
        }).toList();

        // Now we need to call the schedule-consumer service to get the classes
        List<ClassDTO> classes = webClientBuilder.build()
            .post()
            .uri(scheduleConsumerUrl + "/classes-per-subscriptions")
            .bodyValue(subscriptionsDTO)
            .retrieve()
            .bodyToFlux(ClassDTO.class)
            .collectList()
            .block();

        return classes;

    }

    public Optional<Subscription> subscribe(String userId, SubscriptionDTO subscription) {

        // Pass the String to Integer
        Integer studentInteger = Integer.parseInt(userId);

        // Check if the subscription already exists
        List<Subscription> subscriptions = subscriptionRepository.findByStudentId(studentInteger);

        for (Subscription sub : subscriptions) {
            if (sub.getGradeName().equals(subscription.getGrade()) && sub.getSubjectName().equals(subscription.getSubject()) && sub.getGroupName().equals(subscription.getGroup())) {
                return Optional.empty();
            }
        }
        
        // Now we need to call the schedule-consumer service to validate the subscription

        Boolean  isValid = webClientBuilder.build()
            .post()
            .uri(scheduleConsumerUrl + "/validate-subscription")
            .bodyValue(subscription)
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();

        if (!isValid) {
            return Optional.empty();
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setStudentId(studentInteger);
        newSubscription.setGradeName(subscription.getGrade());
        newSubscription.setSubjectName(subscription.getSubject());
        newSubscription.setGroupName(subscription.getGroup());
        newSubscription.setCreatedAt(java.time.LocalDateTime.now());
        newSubscription.setUpdatedAt(java.time.LocalDateTime.now());

        return Optional.of(subscriptionRepository.save(newSubscription));

    }

    @Transactional
    public List<SubscriptionDTO> subscribeBatching(String userId, List<SubscriptionDTO> subscriptions) {
        
        if (subscriptions.isEmpty()) {
            return List.of();
        }
        
        for (SubscriptionDTO subscription : subscriptions) {
            // Pass the String to Integer
            Integer studentInteger = Integer.parseInt(userId);

            // Check if the subscription already exists
            List<Subscription> existingSubscriptions = subscriptionRepository.findByStudentId(studentInteger);

            for (Subscription sub : existingSubscriptions) {
                if (sub.getGradeName().equals(subscription.getGrade()) && sub.getSubjectName().equals(subscription.getSubject()) && sub.getGroupName().equals(subscription.getGroup())) {
                    return List.of();
                }
            }
            
            // Now we need to call the schedule-consumer service to validate the subscription

            Boolean  isValid = webClientBuilder.build()
                .post()
                .uri(scheduleConsumerUrl + "/validate-subscription")
                .bodyValue(subscription)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            if (!isValid) {
                return List.of();
            }

            Subscription newSubscription = new Subscription();
            newSubscription.setStudentId(studentInteger);
            newSubscription.setGradeName(subscription.getGrade());
            newSubscription.setSubjectName(subscription.getSubject());
            newSubscription.setGroupName(subscription.getGroup());
            newSubscription.setCreatedAt(java.time.LocalDateTime.now());
            newSubscription.setUpdatedAt(java.time.LocalDateTime.now());

            subscriptionRepository.save(newSubscription);
        }

        return subscriptions;
    }

}
