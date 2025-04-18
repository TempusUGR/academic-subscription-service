package com.calendarugr.academic_subscription_service.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.calendarugr.academic_subscription_service.dtos.FacultyDTO;
import com.calendarugr.academic_subscription_service.dtos.IdDTO;
import com.calendarugr.academic_subscription_service.entities.Subscription;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

        List<Subscription> findByStudentId(Integer student_id);

        List<Subscription> findByStudentIdAndGradeName(Integer studentInteger, String grade);

        Subscription findByStudentIdAndGradeNameAndSubjectNameAndGroupName(Integer studentInteger, String grade,
                        String subject, String group);

        @Query(value = "{ 'studentId' : ?0 }", fields = "{ 'facultyName' : 1, '_id' : 0 }")
        List<FacultyDTO> findFacultyNameByStudentId(Integer studentId);

        @Query(value = "{ 'gradeName': ?0, 'subjectName': ?1, 'groupName': ?2 }", fields = "{ 'studentId': 1, '_id': 0 }")
        List<IdDTO> findStudentsIdByGradeNameAndSubjectNameAndGroupName(String gradeName, String subjectName,
                        String groupName);
}
