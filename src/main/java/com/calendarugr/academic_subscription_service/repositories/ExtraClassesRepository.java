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
            "finishHour": { "$gt": ?3 },
            "type": "GROUP"
        }
    """)
    List<ExtraClasses> findConflictingClassesOnGroupEvent(String faculty_name, LocalDate date, String classroom, LocalDateTime init_hour, LocalDateTime finish_hour);
    
    @Query("""
        {
            "facultyName": ?0,
            "date": ?1,
            "initHour": { "$eq": ?3 },
            "finishHour": { "$eq": ?2 },
            "title": ?4,
            "type": "FACULTY"
        }
    """)
    List<ExtraClasses> findConflictingClassesOnFacultyEvent(String facultyName, LocalDate date, LocalDateTime initHour, LocalDateTime finishHour, String title);

    List<ExtraClasses> findByTypeAndGradeNameAndSubjectNameAndGroupName(String type, String gradeName, String subjectName,
            String groupName);

    List<ExtraClasses> findByTypeAndFacultyNameIn(String type, List<String> uniqueFaculties);
    List<ExtraClasses> findByIdUserAndType(String idUser, String type);

}
