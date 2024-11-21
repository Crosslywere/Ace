package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Question.Id> {
}
