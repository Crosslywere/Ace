package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Exam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

	List<Exam> findByState(Exam.State state);

	List<Exam> findByState(Exam.State state, Pageable pageable);

	List<Exam> findByTitleContainingIgnoreCase(String title);

	long countByState(Exam.State state);
}
