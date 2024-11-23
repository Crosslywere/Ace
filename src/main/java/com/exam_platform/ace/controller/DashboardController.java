package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Candidate;
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
import org.springframework.data.domain.Pageable;
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

	private final int MAX_SIZE = 15;

	@EventListener(ApplicationStartedEvent.class)
	public void testRepository() {
		var questionBuilder = Question.builder()
				.id(Question.Id.builder()
						.number(1)
						.build())
				.query("\"What are you doing in my swap?\" What famous character said this line?")
				.options(List.of("The Avatar", "Shrek", "Rick", "Ben 10", "Naruto"))
				.answerIndex((byte)1);
		var paperBuilder = Paper.builder().id(Paper.Id.builder()
						.name("Trivia")
						.build());
		var examBuilder = Exam.builder()
				.openTime(Time.valueOf(LocalTime.now()))
				.closeTime(Time.valueOf(LocalTime.now().plusHours(6)))
				.duration(10);
		var candidateBuilder = Candidate.builder();
		for (int i = 0; i < 3; i++) {
			Question q = questionBuilder.build();
			Paper p = paperBuilder.build();
			p.addQuestion(q);
			Exam e = examBuilder
					.title("Some Other Exam " + (i + 1))
					.scheduledDate(Date.valueOf(LocalDate.now()))
					.build();
			e.addPaper(p);
			for (int j = 0; j < 10; j++) {
				Candidate c = candidateBuilder
						.id(Candidate.Id.builder()
								.username(String.valueOf(j))
								.build())
						.build();
				e.addCandidate(c);
			}
			examService.createExam(e);
		}
		Question question1 = questionBuilder.build();
		Paper paper1 = paperBuilder.build();
		paper1.addQuestion(question1);
		Exam exam1 = examBuilder
				.title("Trivia 1(now)")
				.scheduledDate(Date.valueOf(LocalDate.now()))
				.build();
		exam1.addPaper(paper1);
		examService.createExam(exam1);

		Question question2 = questionBuilder.build();
		Paper paper2 = paperBuilder.build();
		paper2.addQuestion(question2);
		Exam exam2 = examBuilder
				.title("Trivia 2(past)")
				.scheduledDate(Date.valueOf(LocalDate.now().minusDays(1)))
				.build();
		exam2.addPaper(paper2);
		examService.createExam(exam2);

		Question question3 = questionBuilder.build();
		Paper paper3 = paperBuilder.build();
		paper3.addQuestion(question3);
		Exam exam3 = examBuilder
				.title("Trivia 3(tomorrow)")
				.scheduledDate(Date.valueOf(LocalDate.now().plusDays(1)))
				.build();
		exam3.addPaper(paper3);
		examService.createExam(exam3);
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
		var exams = examService.getExamsByState(Exam.State.SCHEDULED, PageRequest.of(pageNumber - 1, MAX_SIZE, sorting));
		var maxPages = (int)examService.countExamsByState(Exam.State.SCHEDULED) / MAX_SIZE;
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
		var exams = examService.getExamsByState(Exam.State.ONGOING, PageRequest.of(pageNumber - 1, MAX_SIZE, sorting));
		var maxPages = (int)examService.countExamsByState(Exam.State.ONGOING) / MAX_SIZE;
		model.addAllAttributes(AttributeBuilder.buildForTable(
				"Ongoing Exams", PageRoute.ONGOING,
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
		var exams = examService.getExamsByState(Exam.State.RECORDED, PageRequest.of(pageNumber - 1, MAX_SIZE, sorting));
		var maxPages = (int)examService.countExamsByState(Exam.State.RECORDED) / MAX_SIZE;
		model.addAllAttributes(AttributeBuilder.buildForTable(
				"Recorded Exams", PageRoute.RECORDED,
				exams, maxPages, pageNumber));
		return "examTable";
	}
	//endregion
	//region Exam Search Table
	@GetMapping("/search")
	public String search(@RequestParam String search, Model model) {
		if (search.isBlank() || search.length() < 4) {
			return "redirect:/scheduled";
		}
		var results = examService.getExamsByTitle(search);
		model.addAllAttributes(AttributeBuilder.buildForTable("Searched Exams Results", PageRoute.SEARCH, results, 0, 0));
		return "examTable";
	}
	//endregion
	//region TODO Candidate Management Table
	@GetMapping("/manage/{id}")
	public String candidates(@PathVariable("id") Long examId, Model model, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam != null && exam.getState() == Exam.State.ONGOING) {
			List<Candidate> candidates = candidateService.getCandidatesByExam(exam, Pageable.unpaged());
			model.addAllAttributes(AttributeBuilder.buildForTable("Manage " + exam.getTitle(), PageRoute.ONGOING, exam, candidates, 0, 0));
			return "candidateTable";
		}
		return "redirect:/ongoing";
	}
	//region Candidates Search Table
	@GetMapping("/manage/{id}/search")
	public String candidates(@PathVariable("id") Long examId, @RequestParam String search, Model model, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam != null && exam.getState() == Exam.State.ONGOING && !search.isBlank()) {
			List<Candidate> candidates = candidateService.getCandidatesByExamAndUsername(exam, search);
			model.addAllAttributes(AttributeBuilder.buildForTable("Search Results", PageRoute.SEARCH, exam, candidates, 0, 0));
			return "candidateTable";
		}
		return "redirect:/ongoing";
	}
	//endregion
	//endregion
//endregion

//region Form Views
	//region Creating an exam
	@GetMapping("/create")
	public String create(Model model, HttpSession session, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = (Exam) session.getAttribute("exam");
		if (exam == null || exam.getState() != null) {
			session.setAttribute("exam", new Exam());
		}
		model.addAllAttributes(AttributeBuilder.build("Create Exam", PageRoute.CREATE));
		return "examForm";
	}

	@PostMapping("/create")
	public String create(@RequestParam MultipartFile papersDoc, @RequestParam MultipartFile candidatesDoc, @RequestParam String oTime, @RequestParam String cTime, Exam exam, Model model, HttpSession session, HttpServletRequest request) {
		if (papersDoc != null && !papersDoc.isEmpty()) {
			examImporter.extractPaperData(exam, papersDoc);
		}
		if (candidatesDoc != null && !candidatesDoc.isEmpty()) {
			examImporter.extractCandidateData(exam, candidatesDoc);
		}
		setTimesFromString(exam, oTime.trim(), cTime.trim());
		session.setAttribute("exam", exam);
		model.addAllAttributes(AttributeBuilder.build("Create Exam", PageRoute.CREATE));
		return "examForm";
	}

	@PostMapping("/create-f")
	public String create(Exam exam, @RequestParam String oTime, @RequestParam String cTime, HttpSession session, RedirectAttributes attributes) {
		exam.setId(((Exam)session.getAttribute("exam")).getId());
		session.removeAttribute("exam");
		setTimesFromString(exam, oTime.trim(), cTime.trim());
		exam = examService.createExam(exam);
		attributes.addFlashAttribute("notification", new Notification("success", exam, 0L, "Created exam successfully"));
		return "redirect:/scheduled";
	}
	//endregion
	//region TODO Updating an exam
	@GetMapping("/update/{id}")
	public String modify(@PathVariable("id") Long examId, Model model, HttpSession session, HttpServletRequest request) {
		if (!RequestValidator.isLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam == null || exam.getState() == Exam.State.RECORDED) {
			return "redirect:/recorded";
		}
		model.addAllAttributes(AttributeBuilder.buildForForm("Modify Exam", PageRoute.valueOf(exam.getState().name())));
		session.setAttribute("exam", exam);
		return "examForm";
	}

	@PostMapping("/update/{id}")
	public String update(@RequestParam MultipartFile papersDoc, @RequestParam MultipartFile candidatesDoc, @RequestParam String oTime, @RequestParam String cTime, Exam exam, Model model, HttpSession session) {
		throw new UnsupportedOperationException("Not fully implemented /update/{id}");
//		return "examForm";
	}

	@PostMapping("/update-f/{id}")
	public String update(@PathVariable("id") Long examId, Exam exam, HttpSession session) {

		return "redirect:/";
	}
	//endregion
	//region TODO Exporting an exam
	//endregion
//endregion

//region Functionalities
	//region Delete Exam

	//endregion
	//region Stop Exam

	//endregion
//endregion

	private static void setTimesFromString(Exam exam, String openTime, String closeTime) {
		if (openTime.matches("^\\d+:\\d+$")) {
			openTime += ":00";
		}
		exam.setOpenTime(Time.valueOf(openTime));
		if (closeTime.matches("^\\d+:\\d+$")) {
			closeTime += ":00";
		}
		exam.setCloseTime(Time.valueOf(closeTime));
	}
}
