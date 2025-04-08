package com.calendarugr.academic_subscription_service.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.calendarugr.academic_subscription_service.entities.Subscription;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    List<Subscription> findByStudentId(Integer student_id);

    List<Subscription> findByStudentIdAndGradeName(Integer studentInteger, String grade);

    Subscription findByStudentIdAndGradeNameAndSubjectNameAndGroupName(Integer studentInteger, String grade,
            String subject, String group);

}
