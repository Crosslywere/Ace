package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.entity.Question;
import com.exam_platform.ace.model.PageRoute;
import com.exam_platform.ace.service.CandidateService;
import com.exam_platform.ace.service.ExamService;
import com.exam_platform.ace.service.QuestionService;
import com.exam_platform.ace.util.AttributeBuilder;
import com.exam_platform.ace.util.ExamExporter;
import com.exam_platform.ace.util.ExamImporter;
import com.exam_platform.ace.util.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.sql.Time;
import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

	private final ExamService examService;

	private final QuestionService questionService;

	private final CandidateService candidateService;

	private final ExamImporter examImporter;

	private final int MAX_SIZE = 15;

	public DashboardController(@Autowired ExamService examService, @Autowired QuestionService questionService, @Autowired CandidateService candidateService, @Autowired ExamImporter examImporter) {
		this.examService = examService;
		this.questionService = questionService;
		this.candidateService = candidateService;
		this.examImporter = examImporter;
	}

	@GetMapping
	public String index(HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("exam");
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
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("exam");
			session.removeAttribute("config");
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
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("exam");
			session.removeAttribute("config");
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
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("exam");
			session.removeAttribute("config");
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
	public String searchExams(@RequestParam String search, Model model) {
		if (search.isBlank()) {
			return "redirect:/scheduled";
		}
		var results = examService.getExamsByTitle(search);
		model.addAllAttributes(AttributeBuilder.buildForTable("Searched Exams Results", PageRoute.SEARCH, results, 0, 0));
		return "examTable";
	}
	//endregion
	//region Candidate Management Table
	@GetMapping("/manage/{id}")
	public String candidatesManagement(@PathVariable("id") Long examId, Model model, HttpServletRequest request) {
		return candidatesManagement(examId, 1, model, request);
	}
	@GetMapping("/manage/{id}/{pageNumber}")
	public String candidatesManagement(@PathVariable("id") Long examId, @PathVariable("pageNumber") int pageNumber, Model model, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam != null && exam.getState() == Exam.State.ONGOING) {
			var candidates = candidateService.getCandidatesByExam(exam, PageRequest.of(pageNumber - 1, MAX_SIZE));
			int maxPages = candidates.size() / MAX_SIZE;
			model.addAllAttributes(AttributeBuilder.buildForTable("Manage " + exam.getTitle(), PageRoute.ONGOING, exam, candidates, maxPages, pageNumber));
			return "candidateTable";
		}
		return "redirect:/ongoing";
	}
	//region Candidates Search Table
	@GetMapping("/manage/{id}/search")
	public String candidatesManagementSearch(@PathVariable("id") Long examId, @RequestParam String search, Model model, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam != null && exam.getState() == Exam.State.ONGOING && !search.isBlank()) {
			List<Candidate> candidates = candidateService.getCandidatesByExamAndUsername(exam, search);
			model.addAllAttributes(AttributeBuilder.buildForTable("Search Results", PageRoute.ONGOING, exam, candidates, 0, 0));
			return "candidateTable";
		} else if (search.isBlank()) {
			return "redirect:/manage/" + examId;
		}
		return "redirect:/ongoing";
	}
	//endregion
	//endregion
//endregion

//region Form Views
	//region Create an exam
	@GetMapping("/create")
	public String create(Model model, HttpSession session, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var exam = (Exam)session.getAttribute("exam");
		if (exam == null) {
			exam = new Exam();
			session.setAttribute("exam", exam);
			session.removeAttribute("config");
		}
		model.addAllAttributes(AttributeBuilder.buildForForm("Create Exam", PageRoute.CREATE, exam));
		return "examForm";
	}
	@GetMapping("/create-r")
	public String create(HttpSession session, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		session.removeAttribute("exam");
		return "redirect:/create";
	}
	@PostMapping("/create")
	public String create(@RequestParam String open, @RequestParam String close, @RequestParam MultipartFile document, Exam exam, Model model, HttpSession session) {
		setTimesFromString(exam, open.trim(), close.trim());
		if (exam.getPapers().isEmpty()) {
			examImporter.extractPaperData(exam, document);
		} else {
			examImporter.extractCandidateData(exam, document);
		}
		exam.getPapers().forEach(paper -> Collections.sort(paper.getQuestions()));
		session.setAttribute("exam", exam);
		model.addAllAttributes(AttributeBuilder.buildForForm("Create Exam", PageRoute.CREATE, exam));
		return "examForm";
	}
	@PostMapping("/create-f")
	public String create(@RequestParam String open, @RequestParam String close, Exam exam, HttpSession session) {
		setTimesFromString(exam, open.trim(), close.trim());
		session.removeAttribute("exam");
		exam.prepForSave();
		examService.createExam(exam);
		return "redirect:/scheduled";
	}
	//endregion
	//region Updating an exam
	@GetMapping("/update/{id}")
	public String update(@PathVariable("id") Long examId, Model model, HttpSession session, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		session.removeAttribute("config");
		Exam exam = examService.getExamById(examId);
		if (exam == null || exam.getState() == Exam.State.RECORDED) {
			return "redirect:/recorded";
		}
		exam.getPapers().forEach(paper -> Collections.sort(paper.getQuestions()));
		model.addAllAttributes(AttributeBuilder.buildForForm("Modify Exam", PageRoute.valueOf(exam.getState().name()), exam));
		if (session.getAttribute("exam") == null)
			session.setAttribute("exam", exam);
		return "examForm";
	}
	@PostMapping("/update/{id}")
	public String updateWithDocument(@PathVariable("id") Long examId, @RequestParam String open, @RequestParam String close, @RequestParam MultipartFile document, Exam exam, Model model, HttpSession session) {
		setTimesFromString(exam, open.trim(), close.trim());
		if (exam.getPapers().isEmpty()) {
			examImporter.extractPaperData(exam, document);
		} else {
			examImporter.extractCandidateData(exam, document);
		}
		Exam unchanged = (Exam) session.getAttribute("exam");
		if (unchanged == null) {
			return "redirect:/scheduled";
		}
		exam.setId(examId);
		exam.setState(unchanged.getState());
		exam.prepForUpdate();
		session.setAttribute("exam", exam);
		model.addAllAttributes(AttributeBuilder.buildForForm("Modify Exam", PageRoute.valueOf(exam.getState().name()), exam));
		return "examForm";
	}
	@PostMapping("/update/{id}/{paper}/{number}")
	public String updateQuestionImage(@PathVariable("id") Long examId, @PathVariable("paper") String paper, @PathVariable("number") Integer number, @RequestParam String open, @RequestParam String close, Exam exam, HttpSession session) {
		setTimesFromString(exam, open.trim(), close.trim());
		exam.setId(examId);
		exam.prepForUpdate();
		examService.updateExam(exam);
		Question capture = new Question();
		exam.getPapers().stream().filter(p -> p.getId().getName().equals(paper)).findFirst().flatMap(
				pPaper -> pPaper.getQuestions().stream().filter(q -> q.getId().getNumber().equals(number)).findFirst()).ifPresent(
						qQuestion -> capture.setImageDocument(qQuestion.getImageDocument())
		);
		MultipartFile document = capture.getImageDocument();
		if (document == null || document.isEmpty()) {
			return "redirect:/update/" + examId;
		}
		questionService.addImageToQuestionById(Question.Id.builder().paperId(Paper.Id.builder().examId(examId).name(paper).build()).number(number).build(), document);
		return "redirect:/update/" + examId;
	}
	@PostMapping("/delete/{id}/{paper}/{number}")
	public String deleteImageInQuestion(@PathVariable("id") Long examId, @PathVariable("paper") String paper, @PathVariable("number") Integer number) {
		questionService.deleteImageInQuestionById(Question.Id.builder().paperId(Paper.Id.builder().examId(examId).name(paper).build()).number(number).build());
		return "redirect:/update/" + examId;
	}
	@PostMapping("/remove/{id}")
	public String deletePart(@RequestParam String open, @RequestParam String close, @PathVariable("id") Long examId, Exam exam) {
		setTimesFromString(exam, open.trim(), close.trim());
		exam.setId(examId);
		exam.prepForUpdate();
		if (!exam.getCandidates().isEmpty()) {
			examService.deleteAllCandidates(examId);
		} else {
			examService.deleteAllPapers(examId);
		}
		return "redirect:/update/" + examId;
	}
	@PostMapping("/update-f/scheduled/{id}")
	public String update(@PathVariable("id") Long examId, @RequestParam String open, @RequestParam String close, Exam exam, HttpSession session) {
		Exam unchanged = (Exam) session.getAttribute("exam");
		if (unchanged != null) {
			session.removeAttribute("exam");
			setTimesFromString(exam, open.trim(), close.trim());
			exam.setId(examId);
			exam.setState(unchanged.getState());
			exam.prepForUpdate();
			examService.updateExam(exam);
		}
		return "redirect:/scheduled";
	}
	@PostMapping("/update-f/ongoing/{id}")
	public String updateOngoingExam(@RequestParam String close, @PathVariable("id") Long examId, Exam exam, HttpSession session) {
		Exam unchanged = (Exam) session.getAttribute("exam");
		if (unchanged != null) {
			session.removeAttribute("exam");
			setCloseTimeFromString(exam, close.trim());
			exam.setId(examId);
			exam.setPasswordRequired(unchanged.isPasswordRequired());
			exam.setUsernameDesc(unchanged.getUsernameDesc());
			exam.setOpenTime(unchanged.getOpenTime());
			exam.setPasswordDesc(unchanged.getPasswordDesc());
			exam.setState(Exam.State.ONGOING);
			exam.setPapers(unchanged.getPapers());
			exam.setCandidates(candidateService.getCandidatesByExamId(examId));
			exam.prepForUpdate();
			examService.updateExam(exam);
		}
		return "redirect:/ongoing";
	}
	//endregion
	//region Export Exams
	@GetMapping("/export/{id}")
	public String exportExam(@PathVariable("id") Long examId, Model model, HttpSession session, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		var exam = examService.getExamById(examId);
		if (exam == null || exam.getState() != Exam.State.RECORDED) {
			return "redirect:/scheduled";
		}
		var config = (ExamExporter.ExportConfig) session.getAttribute("config");
		ExamExporter exporter = new ExamExporter(config);
		if (exporter.getConfig() == null)
			exporter.setConfig(exam);
		model.addAttribute("config", exporter.getConfig());
		model.addAttribute("exam", exam);
		model.addAttribute("currentRoute", PageRoute.RECORDED);
		return "examExportForm";
	}
	@PostMapping("/configure/{id}")
	public String configure(@PathVariable("id") Long examId, ExamExporter.ExportConfig config, HttpSession session) {
		session.setAttribute("config", config);
		return "redirect:/export/" + examId;
	}
	@ResponseBody
	@GetMapping("/download")
	public FileSystemResource download(@RequestParam Long id, HttpServletRequest request, HttpSession session) {
		if (RequestValidator.isNotLocalhost(request)) {
			return null;
		}
		Exam exam = examService.getExamById(id);
		var config = (ExamExporter.ExportConfig) session.getAttribute("config");
		if (config == null || exam == null || exam.getState() != Exam.State.RECORDED) {
			return null;
		}
		ExamExporter exporter = new ExamExporter(config);
		String content = exporter.exportToCSV(exam);
		File file = new File("result.csv");
		try {
			if (file.createNewFile()) {
				file.deleteOnExit();
			}
			FileWriter fw = new FileWriter(file, false);
			fw.write(content);
			fw.close();
			return new FileSystemResource(file);
		} catch (Exception exception) {
			//noinspection CallToPrintStackTrace
			exception.printStackTrace();
			System.out.println("File exception!");
		}
		return null;
	}
	//endregion
//endregion

//region Functionalities
	//region Delete Exam
	@GetMapping("/delete/{id}")
	public String deleteExam(@PathVariable("id") Long examId, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		Exam exam = examService.getExamById(examId);
		if (exam == null) {
			return "redirect:/scheduled";
		}
		examService.deleteById(examId);
		return switch (exam.getState()) {
			case SCHEDULED -> "redirect:/scheduled";
			case ONGOING -> "redirect:/ongoing";
			case RECORDED -> "redirect:/recorded";
		};
	}
	//endregion
	//region Stop Exam
	@GetMapping("/stop/{id}")
	public String stopExam(@PathVariable("id") Long examId, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/exam";
		}
		examService.stopExamById(examId);
		return "redirect:/recorded";
	}
	//endregion
	//region Candidate Management (ie allow re-entry, reset, etc)
	@GetMapping("/allow-reentry/{id}")
	public String allowReentryOfCandidate(@PathVariable("id") Long examId, @RequestParam String username, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/examId";
		}
		var candidateId = Candidate.Id.builder().examId(examId).username(username).build();
		candidateService.logoutCandidateById(candidateId);
		return "redirect:/manage/" + examId;
	}
	@GetMapping("/allow-reentry/{id}/all")
	public String allowReentryOfAllCandidates(@PathVariable("id") Long examId, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/examId";
		}
		return "redirect:/manage/" + examId;
	}
	@GetMapping("/reset/{id}")
	public String resetCandidate(@PathVariable("id") Long examId, @RequestParam String username, HttpServletRequest request) {
		if (RequestValidator.isNotLocalhost(request)) {
			return "redirect:/examId";
		}
		var candidateId = Candidate.Id.builder().examId(examId).username(username).build();
		candidateService.resetCandidateById(candidateId);
		return "redirect:/manage/" + examId;
	}
	//endregion
//endregion

	private static void setCloseTimeFromString(@NonNull Exam exam, @NonNull String closeTime) {
		if (closeTime.matches("^\\d+:\\d+$")) {
			closeTime += ":00";
		}
		exam.setCloseTime(Time.valueOf(closeTime));
	}

	private static void setTimesFromString(@NonNull Exam exam, @NonNull String openTime, @NonNull String closeTime) {
		if (openTime.matches("^\\d+:\\d+$")) {
			openTime += ":00";
		}
		exam.setOpenTime(Time.valueOf(openTime));
		setCloseTimeFromString(exam, closeTime);
	}
}
