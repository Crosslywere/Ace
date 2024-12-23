package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.CandidateAnswer;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.service.CandidateAnswerService;
import com.exam_platform.ace.service.CandidateService;
import com.exam_platform.ace.service.ExamService;
import com.exam_platform.ace.service.PaperService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CandidateController {

	private final ExamService examService;

	private final PaperService paperService;

	private final CandidateService candidateService;

	private final CandidateAnswerService answerService;

	public CandidateController(@Autowired ExamService examService, @Autowired PaperService paperService, @Autowired CandidateService candidateService, @Autowired CandidateAnswerService answerService) {
		this.examService = examService;
		this.paperService = paperService;
		this.candidateService = candidateService;
		this.answerService = answerService;
	}

	@GetMapping("/exam")
	public String index() {
		return "redirect:/exam/select";
	}

	@GetMapping("/exam/select")
	public String examSelect(@NonNull Model model, @NonNull HttpSession session) {
		session.removeAttribute("exam");
		session.removeAttribute("exams");
		session.removeAttribute("pastTime");
		session.removeAttribute("candidate");
		session.removeAttribute("candidateId");
		session.removeAttribute("candidateAnswerId");
		model.addAttribute("exams", examService.getExamsByState(Exam.State.ONGOING));
		return "examSelect";
	}

	@PostMapping("/login")
	public String examLogin(@RequestParam Long id, HttpSession session) {
		Exam exam = examService.getExamById(id);
		if (exam == null || exam.getState() != Exam.State.ONGOING) {
			return "redirect:/exam/select";
		}
		session.setAttribute("exam", exam);
		return "examLogin";
	}

	@PostMapping("/exam")
	public String login(@RequestParam String username, @RequestParam String password, RedirectAttributes attributes, @NonNull HttpSession session) {
		Exam exam = (Exam) session.getAttribute("exam");
		if (exam == null) {
			return "redirect:/exam/select";
		}
		Candidate candidate = candidateService.loginCandidate(Candidate.Id.builder().username(username).examId(exam.getId()).build(), password);
		if (candidate == null) {
			attributes.addFlashAttribute("error", "Invalid credentials/Already logged in.");
			return "redirect:/exam/select";
		}
		session.removeAttribute("exams");
		// Sets the candidate as the current logged in candidate for this session until reset
		session.setAttribute("candidateId", candidate.getId());
		session.setAttribute("candidate", candidate);
		session.setAttribute("candidateAnswerId", CandidateAnswer.Id.builder().candidateId(candidate.getId()).paperName(candidate.getPapers().getFirst()).number(1).build());
		return "examInstructions";
	}

	@PostMapping("/exam/{paper}/{number}")
	public String examQuestion(@PathVariable("paper") String paperName, @PathVariable("number") Integer questionNumber, @Nullable @RequestParam Byte answer, Model model, @NonNull HttpSession session) {
		var candidateAnswerId = (CandidateAnswer.Id) session.getAttribute("candidateAnswerId");
		if (candidateAnswerId != null) {
			// do time calculation and set time for candidate
			Object pastTime = session.getAttribute("pastTime");
			if (pastTime != null) {
				long prevTime = Long.parseLong(pastTime.toString());
				long nowTime = System.currentTimeMillis();
				long timeDiff = nowTime - prevTime;
				float inSeconds = timeDiff / 1_000.0f;
				Candidate candidate = candidateService.getCandidateById(candidateAnswerId.getCandidateId());
				if (candidate == null || candidate.isSubmitted()) {
					return "redirect:/exam";
				}
				float timeUsed = candidate.getTimeUsed() + inSeconds;
				candidate.setTimeUsed(timeUsed);
				candidate.setLoggedIn(true);
				candidateService.updateCandidate(candidate);
				answerService.setAnswer(candidateAnswerId, answer);
			}
		}
		Candidate.Id candidateId = (Candidate.Id) session.getAttribute("candidateId");
		if (candidateId == null) {
			return "redirect:/exam";
		}
		Candidate candidate = candidateService.getCandidateById(candidateId);
		if (candidate == null) {
			return "redirect:/exam";
		}
		CandidateAnswer.Id answerId = CandidateAnswer.Id.builder().candidateId(candidateId).paperName(paperName).number(questionNumber).build();
		var ans = answerService.getCandidateAnswerById(answerId);
		if (ans == null) {
			return "redirect:/exam";
		}
		// For calculating time without being tied to the front end
		session.setAttribute("pastTime", System.currentTimeMillis());
		// For storing the answer when submitted from the frontend
		session.setAttribute("candidateAnswerId", answerId);
		// For candidate.getPaperAnswers(String) for pagination of questions
		session.setAttribute("candidate", candidate);
		// Calculating the next and previous question route
		int i = 0;
		if (questionNumber > 1) {
			model.addAttribute("prevQuestionRoute", paperName + "/" + (questionNumber - 1));
		} else if ((i = candidate.getPapers().indexOf(paperName)) > 0) {
			Paper paper = paperService.getPaperById(Paper.Id.builder().examId(candidateId.getExamId()).name(candidate.getPapers().get(i - 1)).build());
			if (paper != null) {
				model.addAttribute("prevQuestionRoute", paper.getId().getName() + "/" + paper.getQuestionsPerCandidate());
			}
		}
		if (ans.getQuestion().getPaper().getQuestionsPerCandidate() > questionNumber) {
			model.addAttribute("nextQuestionRoute", paperName + "/" + (questionNumber + 1));
		} else if ((i = candidate.getPapers().indexOf(paperName)) < candidate.getPapers().size() - 1) {
			model.addAttribute("nextQuestionRoute", candidate.getPapers().get(i + 1) + "/" + 1);
		}
		model.addAttribute("candidateAnswer", ans);
		return "examQuestion";
	}

	@PostMapping("/submit")
	public String submit(@Nullable @RequestParam Byte answer, @NonNull HttpSession session) {
		// Get the candidate (Check if the exam is meant to show results)
		CandidateAnswer.Id candidateAnswerId = (CandidateAnswer.Id) session.getAttribute("candidateAnswerId");
		if (candidateAnswerId != null) {
			Object pastTime = session.getAttribute("pastTime");
			if (pastTime != null) {
				long prevTime = Long.parseLong(pastTime.toString());
				long nowTime = System.currentTimeMillis();
				long timeDiff = nowTime - prevTime;
				float inSeconds = timeDiff / 1_000.0f;
				Candidate candidate = candidateService.getCandidateById(candidateAnswerId.getCandidateId());
				if (candidate == null || candidate.isSubmitted()) {
					return "redirect:/exam";
				}
				float timeUsed = candidate.getTimeUsed() + inSeconds;
				candidate.setTimeUsed(timeUsed);
				candidate.setLoggedIn(true);
				candidate.setSubmitted(true);
				candidateService.updateCandidate(candidate);
				answerService.setAnswer(candidateAnswerId, answer);
			}
		} else {
			return "redirect:/exam";
		}
		Candidate.Id candidateId = (Candidate.Id) session.getAttribute("candidateId");
		Candidate candidate = candidateService.getCandidateById(candidateId);
		if (candidate == null) {
			return "redirect:/exam/select";
		}
		// Set page attributes
		session.removeAttribute("pastTime");
		session.removeAttribute("candidateAnswerId");
		session.removeAttribute("candidateId");
		session.setAttribute("candidate", candidate);
		// Return page template
		return "examSubmit";
	}
}
