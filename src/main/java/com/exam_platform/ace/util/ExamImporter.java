package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
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

	private void extractPapers(Exam exam, String text) {
		Scanner scanner = new Scanner(text);
		String line = "";
		Paper paper = new Paper();
		boolean handled = true;
		boolean retry = false;
		while (scanner.hasNextLine()) {
			if (handled) {
				handled = false;
				line = scanner.nextLine().trim();
			}
			if (line.isBlank()) {
				handled = true;
				continue;
			}
			if (line.matches("^[A-Za-z0-9\\s]+:$")) {
				handled = true;
				if (paper.getQuestions() != null && !paper.getQuestions().isEmpty()) {
					paper.setQuestionsPerCandidate(paper.getQuestions().size());
					exam.addPaper(paper);
				}
				paper = Paper.builder()
						.id(Paper.Id.builder()
								.name(line.replaceAll(":", " ").trim())
								.build())
						.build();
				continue;
			}
			if (line.matches("^\\d+\\.\\s\\S[\\S\\s]+")) {
				handled = true;
				String[] parts = line.split("\\.\\s", 2);
				Question question = Question.builder()
						.id(Question.Id.builder()
								.number(Integer.parseInt(parts[0].trim()))
								.build())
						.query(parts[1].trim())
						.build();
				while (scanner.hasNextLine()) {
					if (handled) {
						handled = false;
						line = scanner.nextLine().trim();
					} else {
						retry = true;
						paper.addQuestion(question);
						break;
					}
					if (line.matches("^[A-Ea-e]\\.\\s[\\S\\s]+")) {
						handled = true;
						if (question.getOptions() == null) {
							question.setOptions(new ArrayList<>());
						}
						parts = line.split("\\.\\s", 2);
						question.getOptions().add(parts[1]);
					} else if (line.matches("^[Aa][Nn][Ss]:\\s*[A-Ea-e]")) {
						handled = true;
						byte answerIndex = (byte) (line.replaceAll("^[Aa][Nn][Ss]:", "").trim()
								.toLowerCase().charAt(0) - 'a');
						question.setAnswerIndex(answerIndex);
					}
					if (!scanner.hasNextLine()) {
						paper.addQuestion(question);
					}
				}
			}
			if (retry) {
				retry = false;
				continue;
			}
			handled = true;
		}
		if (!paper.getQuestions().isEmpty()) {
			paper.setQuestionsPerCandidate(paper.getQuestions().size());
			exam.addPaper(paper);
		}
		scanner.close();
	}

	public void extractPaperData(Exam exam, MultipartFile papersDoc) {
		if (papersDoc.isEmpty()) {
			return;
		}
		try {
			extractPapers(exam, extractText(papersDoc.getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void extractCandidates(Exam exam, String text) {
		Scanner scanner = new Scanner(text);
		String line;
		boolean header = true;
		ConfigIndexer indexer = new ConfigIndexer(exam);
		int row = 1;
		while (scanner.hasNextLine()) {
			if (!header && indexer.getUsernameIndex() == -1) {
				scanner.close();
				throw new RuntimeException("No username was specified in the CSV file.\nPlease indicate the column to use as the username by putting '#UN ' in front of it.");
			}
			if (!header && exam.isPasswordRequired() && indexer.getPasswordIndex() == -1) {
				scanner.close();
				throw new RuntimeException("No password was specified in the CSV file in spite of exam requiring a password.\nPlease indicate the column to use as the password by putting '#PW ' in front of it.");
			}
			line = scanner.nextLine();
			if (line.isBlank()) {
				continue;
			}
			if (header) {
				header = false;
				List<String> columns = parseCSVLine(line);
				if (columns.isEmpty()) {
					header = true;
					continue;
				}
				// Indexing the columns in the header
				for (int i = 0; i < columns.size(); i++) {
					String column = columns.get(i);
					if (column.toUpperCase().trim().startsWith("#UN ") && indexer.getUsernameIndex() == -1) {
						indexer.setUsernameIndex(i);
						column = column.substring(4).trim();
						exam.setUsernameDesc(column);
					}
					if (column.toUpperCase().trim().startsWith("#PW ") && indexer.getPasswordIndex() == -1 && (!exam.getCandidates().isEmpty() && exam.getCandidates().getFirst().getPassword() != null)) {
						indexer.setPasswordIndex(i);
						column = column.substring(4).trim();
						exam.setPasswordDesc(column);
						exam.setPasswordRequired(true);
					}
					if (column.toLowerCase().contains("mail")) {
						indexer.setEmailIndex(i);
						indexer.setEmail(true);
						indexer.setEmailDesc(column);
					} else
					if (column.toLowerCase().contains("phone") || column.toLowerCase().contains("tel")) {
						indexer.setPhoneIndex(i);
						indexer.setPhoneNumber(true);
						indexer.setPhoneNumberDesc(column);
					} else
					if (column.toLowerCase().contains("address")) {
						indexer.setAddressIndex(i);
						indexer.setAddress(true);
						indexer.setAddressDesc(column);
					} else
					if (column.toLowerCase().contains("state")) {
						indexer.setStateIndex(i);
						indexer.setState(true);
						indexer.setStateDesc(column);
					} else
					if (column.toLowerCase().contains("name")) {
						if (column.toLowerCase().contains("first")) {
							indexer.setFirstnameIndex(i);
							indexer.setFirstname(true);
							indexer.setFirstnameDesc(column);
						} else if (column.toLowerCase().contains("last")) {
							indexer.setLastnameIndex(i);
							indexer.setLastname(true);
							indexer.setLastnameDesc(column);
						} else if (column.toLowerCase().contains("other")) {
							indexer.setOtherNamesIndex(i);
							indexer.setOtherNames(true);
							indexer.setOtherNamesDesc(column);
						}
					} else
					if (column.toLowerCase().contains("paper")) {
						indexer.setPapersIndex(i);
					}
				}
			}
			else {
				Candidate candidate = new Candidate();
				List<String> columns = parseCSVLine(line);
				if (columns.size() > indexer.getUsernameIndex()) {
					String username = columns.get(indexer.getUsernameIndex());
					if (!username.isBlank()) {
						candidate.setId(Candidate.Id.builder().username(username).build());
					} else {
						scanner.close();
						throw new RuntimeException("Username not specified in row " + row);
					}
				}
				else {
					scanner.close();
					throw new RuntimeException("Username not specified in row " + row);
				}
				if (columns.size() > indexer.getPapersIndex()) {
					String papersLine = columns.get(indexer.getPapersIndex());
					List<String> papers = parseCSVLine(papersLine);
					for (var paper : papers) {
						if (candidate.getPapers().stream().noneMatch(paper::equalsIgnoreCase) && !paper.isBlank()) {
							Paper p = exam.getPapers().stream().filter(ePaper -> ePaper.getId().getName().equalsIgnoreCase(paper.trim())).findFirst().orElse(null);
							if (p == null) {
								continue;
							}
							candidate.getPapers().add(paper);
						}
					}
				}
				else {
					scanner.close();
					throw new RuntimeException("Papers not specified in row " + row);
				}
				if (columns.size() > indexer.getPasswordIndex() && exam.isPasswordRequired()) {
					String password = columns.get(indexer.getPasswordIndex());
					candidate.setPassword(password);
				}
				if (columns.size() > indexer.getEmailIndex() && indexer.getEmailIndex() >= 0) {
					String email = columns.get(indexer.getEmailIndex());
					candidate.setEmail(email);
				}
				if (columns.size() > indexer.getPhoneIndex() && indexer.getPhoneIndex() >= 0) {
					String phoneNumber = columns.get(indexer.getPhoneIndex());
					candidate.setPhoneNumber(phoneNumber);
				}
				if (columns.size() > indexer.getAddressIndex() && indexer.getAddressIndex() >= 0) {
					String address = columns.get(indexer.getAddressIndex());
					candidate.setAddress(address);
				}
				if (columns.size() > indexer.getStateIndex() && indexer.getStateIndex() >= 0) {
					String state = columns.get(indexer.getStateIndex());
					candidate.setState(state);
				}
				if (columns.size() > indexer.getFirstnameIndex() && indexer.getFirstnameIndex() >= 0) {
					String firstname = columns.get(indexer.getFirstnameIndex());
					candidate.setFirstname(firstname);
				}
				if (columns.size() > indexer.getLastnameIndex() && indexer.getLastnameIndex() >= 0) {
					String lastname = columns.get(indexer.getLastnameIndex());
					candidate.setLastname(lastname);
				}
				if (columns.size() > indexer.getOtherNamesIndex() && indexer.getOtherNamesIndex() >= 0) {
					String otherNames = columns.get(indexer.getOtherNamesIndex());
					candidate.setOtherNames(otherNames);
				}
				exam.addCandidate(candidate);
				System.out.println(candidate);
			}
			row ++;
		}
		exam.setCandidateConfig(indexer);
		scanner.close();
	}

	private List<String> parseCSVLine(String line) {
		List<String> columns = new ArrayList<>();
		StringBuilder colBuilder = new StringBuilder();
		boolean quoted = false;
		for (byte ch : line.getBytes()) {
			if (ch == ',' && !quoted) {
				columns.add(colBuilder.toString().trim());
				colBuilder = new StringBuilder();
				continue;
			}
			if (ch == '"') {
				quoted = !quoted;
				continue;
			}
			colBuilder.append(ch == (byte) 147 || ch == (byte) 148 ? '"' : (char) ch);
		}
		columns.add(colBuilder.toString());
		return columns;
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ConfigIndexer extends Exam.CandidateConfig {
		@Builder.Default
		private int usernameIndex = -1;
		@Builder.Default
		private int passwordIndex = -1;
		@Builder.Default
		private int emailIndex = -1;
		@Builder.Default
		private int phoneIndex = -1;
		@Builder.Default
		private int addressIndex = -1;
		@Builder.Default
		private int stateIndex = -1;
		@Builder.Default
		private int firstnameIndex = -1;
		@Builder.Default
		private int lastnameIndex = -1;
		@Builder.Default
		private int otherNamesIndex = -1;
		@Builder.Default
		private int papersIndex = -1;

		public ConfigIndexer(Exam exam) {
			super(
					exam.getCandidateConfig().isEmail(), exam.getCandidateConfig().getEmailDesc(),
					exam.getCandidateConfig().isPhoneNumber(), exam.getCandidateConfig().getPhoneNumberDesc(),
					exam.getCandidateConfig().isAddress(), exam.getCandidateConfig().getAddressDesc(),
					exam.getCandidateConfig().isState(), exam.getCandidateConfig().getStateDesc(),
					exam.getCandidateConfig().isFirstname(), exam.getCandidateConfig().getFirstnameDesc(),
					exam.getCandidateConfig().isLastname(), exam.getCandidateConfig().getLastnameDesc(),
					exam.getCandidateConfig().isOtherNames(), exam.getCandidateConfig().getOtherNamesDesc()
			);
		}
	}

	public void extractCandidateData(Exam exam, MultipartFile candidatesDoc) {
		if (candidatesDoc.isEmpty()) {
			System.out.println("Candidate's Document is empty!");
			return;
		}
		try {
			String text = extractText(candidatesDoc.getInputStream());
			System.out.println("Text Extracted\n" + text);
			extractCandidates(exam, text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
