package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Paper.Id> {

    Optional<Paper> findById_ExamIdAndId_Name(Long examId, String name);

}
