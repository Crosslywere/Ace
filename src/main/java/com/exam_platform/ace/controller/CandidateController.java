package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CandidateController {

	private final ExamService examService;

	@GetMapping("/exam")
	public String index() {
		return "redirect:/exam/select";
	}

	@GetMapping("/exam/select")
	public String examSelect(Model model) {
		model.addAttribute("exams", examService.getExamsByState(Exam.State.ONGOING));
		return "examSelect";
	}

	@PostMapping("/exam/login")
	public String examLogin(@RequestParam Long id, Model model) {
		Exam exam = examService.getExamById(id);
		if (exam == null || exam.getState() != Exam.State.ONGOING) {
			return "redirect:/exam/select";
		}
		throw new UnsupportedOperationException("Not yet created the \"examLogin\" page");
	}
}
