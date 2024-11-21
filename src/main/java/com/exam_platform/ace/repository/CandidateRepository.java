package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Candidate.Id> {
	List<Candidate> findByExam(Exam exam, Pageable pageable);
}
