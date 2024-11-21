package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.entity.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ExamImporter {

	private final Parser parser;
	private final ParseContext context;
	private final Metadata metadata;

	public ExamImporter() {
		Detector detector = new DefaultDetector();
		parser = new AutoDetectParser(detector);
		context = new ParseContext();
		context.set(Parser.class, parser);
		metadata = new Metadata();
	}

	private String extractText(InputStream stream) throws Exception {
		OutputStream os = new ByteArrayOutputStream();
		ContentHandler handler = new BodyContentHandler(os);
		parser.parse(stream, handler, metadata, context);
		return os.toString();
	}

	// Returns true when there is no leftover part of the scanner to be read
	private boolean extractPaperQuestions(Paper paper, Scanner scanner) {
		boolean handled = true;
		String line = "";
		while (scanner.hasNext()) {
			if (handled) {
				handled = false;
				line = scanner.nextLine();;
			}
			if (line.matches("")) {

			}
		}
		return true;
	}

	private List<Paper> extractPapers(String text) {
		Scanner scanner = new Scanner(text);
		String line = "";
		boolean handled = true;
		Paper paper = new Paper();
		List<Paper> papers = new ArrayList<>();
		while (scanner.hasNext()) {
			if (handled) {
				handled = false;
				line = scanner.nextLine().trim();
			}
			if (line.isBlank()) {
				handled = true;
				continue;
			}
			// Paper
			if (line.matches("^[A-Za-z\\s]+:$")) {
				if (!paper.getQuestions().isEmpty()) {
					paper.setQuestionsPerCandidate(paper.getQuestions().size());
					papers.add(paper);
				}
				paper = new Paper();
				paper.setId(Paper.Id.builder()
						.name(line.replaceAll(":$", "").trim())
						.build());
				paper.setQuestions(List.of());
				handled = true;
				// Question
				Question question = new Question();
				while (scanner.hasNext()) {
					if (handled) {
						handled = false;
						line = scanner.nextLine();
					}
					if (line.isBlank()) {
						handled = true;
						continue;
					}
					// Question found
					if (line.matches("^\\d+. \\S+[\\s\\S]+")) {
						if (question.getId() != null) {
							paper.addQuestion(question);
						}
						int dotIndex = line.indexOf('.');
						question.setQuery(line);
					}

				}
			}
		}
		return papers;
	}

	public void extractPaperData(@NonNull Exam exam, @NonNull MultipartFile paperDocument) {

	}

	public void extractCandidateData(@NonNull Exam exam, @NonNull MultipartFile candidatesDocument) {

	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@SuperBuilder
	public static class ConfigIndexer extends Exam.CandidateConfig {
		private int emailIndex;
		private int genderIndex;
		private int phoneNumberIndex;
		private int addressIndex;
		private int stateIndex;
		private int firstnameIndex;
		private int lastnameIndex;
		private int otherNamesIndex;
		private int usernameIndex;
		private int passwordIndex;
		private int papersIndex;
	}
}
