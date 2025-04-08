package com.calendarugr.academic_subscription_service.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.calendarugr.academic_subscription_service.entities.ExtraClasses;

@Repository
public interface ExtraClassesRepository extends MongoRepository<ExtraClasses, String> {

    ExtraClasses findByGradeNameAndSubjectNameAndGroupName(String grade_name, String subject_name, String group_name);
    ExtraClasses findByFacultyName(String faculty_name);

    // Validate classes with conflicts in the hours, not only the same start and end time
    @Query("""
        {
            "facultyName": ?0,
            "date": ?1,
            "classroom": ?2,
            "initHour": { "$lt": ?4 },
            "finishHour": { "$gt": ?3 }
        }
    """)
    List<ExtraClasses> findConflictingClassesOnGroupEvent(String faculty_name, LocalDate date, String classroom, LocalDateTime init_hour, LocalDateTime finish_hour);

    @Query("""
        {
            "facultyName": ?0,
            "date": ?1,
            "initHour": { "$lt": ?3 },
            "finishHour": { "$gt": ?2 }
        }
    """)
    List<ExtraClasses> findConflictingClassesOnFacultyEvent(String faculty_name, LocalDate date, LocalDateTime init_hour, LocalDateTime finish_hour);

}
