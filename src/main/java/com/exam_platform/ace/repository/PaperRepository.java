package com.exam_platform.ace.repository;

import com.exam_platform.ace.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Paper.Id> {
}
