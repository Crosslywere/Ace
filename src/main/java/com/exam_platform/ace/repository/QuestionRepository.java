package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Question.Id> {

    Optional<Question> findById_PaperId_ExamIdAndId_PaperId_NameAndId_Number(Long examId, String name, Integer number);

}
