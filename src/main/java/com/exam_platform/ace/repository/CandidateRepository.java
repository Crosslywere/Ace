package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Candidate.Id> {

	List<Candidate> findById_ExamId(Long examId);

	List<Candidate> findByExam(Exam exam, Pageable pageable);

	List<Candidate> findByExamAndId_UsernameContainingIgnoreCase(Exam exam, String username);

	Optional<Candidate> findById_ExamIdAndId_UsernameIgnoreCase(Long examId, String username);

}
