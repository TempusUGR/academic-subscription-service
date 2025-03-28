package com.calendarugr.academic_subscription_service.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.calendarugr.academic_subscription_service.entities.NonSchoolDays;

@Repository
public interface NonSchoolDaysRepository extends MongoRepository<NonSchoolDays, String> {

    NonSchoolDays findByFaculty(String faculty);
    List<NonSchoolDays> findByGradesContainingIgnoreCase(String grade);

}
