package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.entity.Question;
import com.exam_platform.ace.model.PageRoute;
import com.exam_platform.ace.service.CandidateService;
import com.exam_platform.ace.service.ExamService;
import com.exam_platform.ace.util.AttributeBuilder;
import com.exam_platform.ace.util.ExamImporter;
import com.exam_platform.ace.util.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.management.Notification;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	private final ExamService examService;

	private final CandidateService candidateService;

	private final ExamImporter examImporter;

	@EventListener(ApplicationStartedEvent.class)
	public void testRepository() {
		Question question = Question.builder()
				.id(Question.Id.builder()
						.number(1)
						.build())
				.query("\"What are you doing in my swap?\" What famous character said this line?")
				.options(List.of("The Avatar", "Shrek", "Rick", "Ben 10", "Naruto"))
				.answerIndex((byte)1)
				.build();
		Paper paper = Paper.builder()
				.id(Paper.Id.builder()
						.name("Trivia")
						.build())
				.build();
		paper.addQuestion(question);
		Exam exam = Exam.builder()
				.title("Trivia")
				.scheduledDate(Date.valueOf(LocalDate.now()))
				.openTime(Time.valueOf(LocalTime.now()))
				.closeTime(Time.valueOf(LocalTime.now().plusHours(1)))
				.duration(10)
				.passwordRequired(false)
				.build();
	}

	@GetMapping()
	public String index(HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		return "redirect:/scheduled";
	}

//region Table Views
	//region Scheduled Table
	@GetMapping("/scheduled")
	public String scheduled(Model model, HttpServletRequest request) {
		return scheduled(1, model, request);
	}

	@GetMapping("/scheduled/{pageNumber}")
	public String scheduled(@PathVariable int pageNumber, Model model, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		var sorting = Sort.by("scheduledDate").ascending()
				.and(Sort.by("openTime").ascending()
						.and(Sort.by("title")));
		var exams = examService.getExamsByState(Exam.State.SCHEDULED, PageRequest.of(pageNumber - 1, 10, sorting));
		var maxPages = examService.getExamsByState(Exam.State.SCHEDULED).size() / 10;
		model.addAllAttributes(AttributeBuilder.buildForTable(
				"Scheduled Exams", PageRoute.SCHEDULED,
				exams, maxPages, pageNumber));
		return "examTable";
	}
	//endregion
	//region Ongoing Table
	@GetMapping("/ongoing")
	public String ongoing(Model model, HttpServletRequest request) {
		return ongoing(1, model, request);
	}

	@GetMapping("/ongoing/{pageNumber}")
	public String ongoing(@PathVariable int pageNumber, Model model, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		var sorting = Sort.by("closeTime").ascending();
		var exams = examService.getExamsByState(Exam.State.ONGOING, PageRequest.of(pageNumber - 1, 10, sorting));
		var maxPages = examService.getExamsByState(Exam.State.ONGOING).size() / 10;
		model.addAllAttributes(AttributeBuilder.buildForTable(
				"Scheduled Exams", PageRoute.ONGOING,
				exams, maxPages, pageNumber));
		return "examTable";
	}
	//endregion
	//region Recorded Table
	@GetMapping("/recorded")
	public String recorded(Model model, HttpServletRequest request) {
		return recorded(1, model, request);
	}

	@GetMapping("/recorded/{pageNumber}")
	public String recorded(@PathVariable int pageNumber, Model model, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		var sorting = Sort.by("closeTime").ascending();
		var exams = examService.getExamsByState(Exam.State.RECORDED, PageRequest.of(pageNumber - 1, 10, sorting));
		var maxPages = examService.getExamsByState(Exam.State.RECORDED).size() / 10;
		model.addAllAttributes(AttributeBuilder.buildForTable(
				"Scheduled Exams", PageRoute.RECORDED,
				exams, maxPages, pageNumber));
		return "examTable";
	}
	//endregion
	//region TODO Exam Search Table
	public String search(@RequestParam String search, Model model) {
		if (search.isBlank()) {
			return "redirect:/scheduled";
		}
		var results = examService.getExamsByTitle(search);
		model.addAllAttributes(AttributeBuilder.buildForTable("Search Results", PageRoute.MIXTURE, results, 0, 0));
		return "examTable";
	}
	//endregion
	//region TODO Candidate Management Table
	@GetMapping("/manage/{id}")
	public String candidates(@PathVariable("id") Long examId) {

		return "redirect:/scheduled";
	}
	//endregion
//endregion

//region Form Views
	//region Creating an exam
	@GetMapping("/create")
	public String create(Model model, HttpSession session, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		if (session.getAttribute("exam") == null) {
			session.setAttribute("exam", new Exam());
		}
		model.addAttribute("examType", Exam.State.SCHEDULED.name());
		model.addAttribute("currentRoute", PageRoute.CREATE.name());
		model.addAttribute("pageTitle", "Create Exam");
		model.addAllAttributes(AttributeBuilder.build("Create Exam", PageRoute.CREATE));
		return "examForm";
	}

	@PostMapping("/create")
	public String create(@RequestParam MultipartFile papers, @RequestParam MultipartFile candidates, Model model, HttpSession session, HttpServletRequest request) {
		Exam exam = (Exam)session.getAttribute("exam");
		if (exam == null)
			return create(model, session, request);
		if (papers != null && !papers.isEmpty()) {
			examImporter.extractPaperData(exam, papers);
		}
		if (candidates != null && !candidates.isEmpty()) {
			examImporter.extractCandidateData(exam, candidates);
		}
		session.setAttribute("exam", exam);
		return create(model, session, request);
	}

	@PostMapping("/created")
	public String create(HttpSession session, RedirectAttributes attributes) {
		Exam exam = (Exam)session.getAttribute("exam");
		session.removeAttribute("exam");
		exam = examService.createExam(exam);
		attributes.addFlashAttribute("notification",new Notification("success", exam, 0L, "Created exam successfully"));
		switch (exam.getState()) {
			case SCHEDULED -> {
				return "redirect:/scheduled";
			}
			case ONGOING -> {
				return "redirect:/ongoing";
			}
			case RECORDED -> {
				return "redirect:/recorded";
			}
			default -> {
				return "redirect:/";
			}
		}
	}
	//endregion
	//region TODO Modifying an exam
	@GetMapping("/modify/{id}")
	public String modify(@PathVariable("id") Long examId, HttpSession session, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam == null) {
			return "redirect:/";
		}
		return"";
	}
	//endregion
	//region TODO Exporting an exam
	//endregion
//endregion
}
