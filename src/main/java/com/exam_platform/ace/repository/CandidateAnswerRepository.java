package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.CandidateAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateAnswerRepository extends JpaRepository<CandidateAnswer, CandidateAnswer.Id> {
}
