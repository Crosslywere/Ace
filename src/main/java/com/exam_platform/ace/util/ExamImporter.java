package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.entity.Question;
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
		boolean hadCandidates = !exam.getCandidates().isEmpty();
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
				// Indexing the columns in the header
				for (int i = 0; i < columns.size(); i++) {
					String column = columns.get(i);
					if (column.trim().toUpperCase().startsWith("#UN ")) {
						indexer.setUsernameIndex(i);
						column = column.substring(4).trim();
						if (!hadCandidates) {
							exam.setUsernameDesc(column);
						}
					}
					if (column.trim().toUpperCase().startsWith("#PW ")) {
						indexer.setPasswordIndex(i);
						column = column.substring(4).trim();
						if (!hadCandidates) {
							exam.setPasswordDesc(column);
							exam.setPasswordRequired(true);
						}
					}
					if (column.toLowerCase().contains("mail")) {
						indexer.setEmailIndex(i);
						if (!hadCandidates) {
							indexer.setEmail(true);
							indexer.setEmailDesc(column);
						}
					} else
					if (column.toLowerCase().contains("phone") || column.toLowerCase().contains("tel")) {
						indexer.setPhoneIndex(i);
						if (!hadCandidates) {
							indexer.setPhoneNumber(true);
							indexer.setPhoneNumberDesc(column);
						}
					} else
					if (column.toLowerCase().contains("address")) {
						indexer.setAddressIndex(i);
						if (!hadCandidates) {
							indexer.setAddress(true);
							indexer.setAddressDesc(column);
						}
					} else
					if (column.toLowerCase().contains("state")) {
						indexer.setStateIndex(i);
						if (!hadCandidates) {
							indexer.setState(true);
							indexer.setStateDesc(column);
						}
					} else
					if (column.toLowerCase().contains("name")) {
						if (column.toLowerCase().contains("first")) {
							indexer.setFirstnameIndex(i);
							if (!hadCandidates) {
								indexer.setFirstname(true);
								indexer.setFirstnameDesc(column);
							}
						} else if (column.toLowerCase().contains("last")) {
							indexer.setLastnameIndex(i);
							if (!hadCandidates) {
								indexer.setLastname(true);
								indexer.setLastnameDesc(column);
							}
						} else if (column.toLowerCase().contains("other")) {
							indexer.setOtherNamesIndex(i);
							if (!hadCandidates) {
								indexer.setOtherNames(true);
								indexer.setOtherNamesDesc(column);
							}
						}
					} else
					if (column.toLowerCase().contains("reg") || column.toLowerCase().contains("s/n") || column.toLowerCase().contains("serial")) {
						indexer.setRegistrationNumberIndex(i);
						if (!hadCandidates) {
							indexer.setRegistrationNumber(true);
							indexer.setRegistrationNumberDesc(column);
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
					for (var paperNames : papers) {
						if (candidate.getPapers().stream().noneMatch(paperNames::equalsIgnoreCase) && !paperNames.isBlank()) {
							Paper paper = exam.getPapers().stream().filter(ePaper -> ePaper.getId().getName().equalsIgnoreCase(paperNames.trim())).findFirst().orElse(null);
							if (paper == null) {
								continue;
							}
							candidate.getPapers().add(paper.getId().getName());
						}
					}
					for (var paperName : exam.getPapers().stream().filter(Paper::isMandatory).map(paper -> paper.getId().getName()).toList()) {
						if (candidate.getPapers().stream().noneMatch(paper -> paper.equalsIgnoreCase(paperName))) {
							candidate.getPapers().add(paperName);
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
				if (columns.size() > indexer.getRegistrationNumberIndex() && indexer.getRegistrationNumberIndex() >= 0) {
					String registrationNumber = columns.get(indexer.getRegistrationNumberIndex());
					candidate.setRegistrationNumber(registrationNumber);
				}
				exam.addCandidate(candidate);
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

	public static class ConfigIndexer extends Exam.CandidateConfig {
		private int usernameIndex = -1;
		private int passwordIndex = -1;
		private int emailIndex = -1;
		private int phoneIndex = -1;
		private int addressIndex = -1;
		private int stateIndex = -1;
		private int firstnameIndex = -1;
		private int lastnameIndex = -1;
		private int otherNamesIndex = -1;
		private int papersIndex = -1;
		private int registrationNumberIndex = -1;

		public ConfigIndexer(Exam exam) {
			super(
					exam.getCandidateConfig().isEmail(), exam.getCandidateConfig().getEmailDesc(),
					exam.getCandidateConfig().isPhoneNumber(), exam.getCandidateConfig().getPhoneNumberDesc(),
					exam.getCandidateConfig().isAddress(), exam.getCandidateConfig().getAddressDesc(),
					exam.getCandidateConfig().isState(), exam.getCandidateConfig().getStateDesc(),
					exam.getCandidateConfig().isFirstname(), exam.getCandidateConfig().getFirstnameDesc(),
					exam.getCandidateConfig().isLastname(), exam.getCandidateConfig().getLastnameDesc(),
					exam.getCandidateConfig().isOtherNames(), exam.getCandidateConfig().getOtherNamesDesc(),
					exam.getCandidateConfig().isRegistrationNumber(), exam.getCandidateConfig().getRegistrationNumberDesc()
			);
		}

		public ConfigIndexer() {
		}

		public ConfigIndexer(boolean email, String emailDesc, boolean phoneNumber, String phoneNumberDesc, boolean address, String addressDesc, boolean state, String stateDesc, boolean firstname, String firstnameDesc, boolean lastname, String lastnameDesc, boolean otherNames, String otherNamesDesc, boolean registrationNumber, String registrationNumberDesc, int usernameIndex, int passwordIndex, int emailIndex, int phoneIndex, int addressIndex, int stateIndex, int firstnameIndex, int lastnameIndex, int otherNamesIndex, int papersIndex, int registrationNumberIndex) {
			super(email, emailDesc, phoneNumber, phoneNumberDesc, address, addressDesc, state, stateDesc, firstname, firstnameDesc, lastname, lastnameDesc, otherNames, otherNamesDesc, registrationNumber, registrationNumberDesc);
			this.usernameIndex = usernameIndex;
			this.passwordIndex = passwordIndex;
			this.emailIndex = emailIndex;
			this.phoneIndex = phoneIndex;
			this.addressIndex = addressIndex;
			this.stateIndex = stateIndex;
			this.firstnameIndex = firstnameIndex;
			this.lastnameIndex = lastnameIndex;
			this.otherNamesIndex = otherNamesIndex;
			this.papersIndex = papersIndex;
			this.registrationNumberIndex = registrationNumberIndex;
		}

		public int getUsernameIndex() {
			return usernameIndex;
		}

		public void setUsernameIndex(int usernameIndex) {
			this.usernameIndex = usernameIndex;
		}

		public int getPasswordIndex() {
			return passwordIndex;
		}

		public void setPasswordIndex(int passwordIndex) {
			this.passwordIndex = passwordIndex;
		}

		public int getEmailIndex() {
			return emailIndex;
		}

		public void setEmailIndex(int emailIndex) {
			this.emailIndex = emailIndex;
		}

		public int getPhoneIndex() {
			return phoneIndex;
		}

		public void setPhoneIndex(int phoneIndex) {
			this.phoneIndex = phoneIndex;
		}

		public int getAddressIndex() {
			return addressIndex;
		}

		public void setAddressIndex(int addressIndex) {
			this.addressIndex = addressIndex;
		}

		public int getStateIndex() {
			return stateIndex;
		}

		public void setStateIndex(int stateIndex) {
			this.stateIndex = stateIndex;
		}

		public int getFirstnameIndex() {
			return firstnameIndex;
		}

		public void setFirstnameIndex(int firstnameIndex) {
			this.firstnameIndex = firstnameIndex;
		}

		public int getLastnameIndex() {
			return lastnameIndex;
		}

		public void setLastnameIndex(int lastnameIndex) {
			this.lastnameIndex = lastnameIndex;
		}

		public int getOtherNamesIndex() {
			return otherNamesIndex;
		}

		public void setOtherNamesIndex(int otherNamesIndex) {
			this.otherNamesIndex = otherNamesIndex;
		}

		public int getPapersIndex() {
			return papersIndex;
		}

		public void setPapersIndex(int papersIndex) {
			this.papersIndex = papersIndex;
		}

		public int getRegistrationNumberIndex() {
			return registrationNumberIndex;
		}

		public void setRegistrationNumberIndex(int registrationNumberIndex) {
			this.registrationNumberIndex = registrationNumberIndex;
		}
	}

	public void extractCandidateData(Exam exam, MultipartFile candidatesDoc) {
		if (candidatesDoc.isEmpty()) {
			return;
		}
		try {
			extractCandidates(exam, extractText(candidatesDoc.getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
