package com.calendarugr.academic_subscription_service.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.calendarugr.academic_subscription_service.entities.ExtraClasses;

@Repository
public interface ExtraClassesRepository extends MongoRepository<ExtraClasses, String> {

    ExtraClasses findByGradeNameAndSubjectNameAndGroupName(String grade_name, String subject_name, String group_name);
    ExtraClasses findByFacultyName(String faculty_name);

}
