package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
@Table(name = "QUESTIONS")
public class Question implements Comparable<Question> {

	@EmbeddedId
	private Id id;

	@MapsId("paperId")
	@ManyToOne
	@JoinColumns({
			@JoinColumn(name = "EXAM_ID", referencedColumnName = "EXAM_ID"),
			@JoinColumn(name = "PAPER_NAME", referencedColumnName = "PAPER_NAME")
	})
	private Paper paper;

	@Column(name = "HAS_IMAGE")
	private boolean hasImage;

	@Column(name = "QUESTION", length = 1024)
	private String query;

	@Column(name = "OPTIONS")
	private List<String> options;

	@Column(name = "ANSWER_INDEX", length = 6)
	private Byte answerIndex;

	@Column(name = "IMAGE_DATA")
	private byte[] imageData = null;

	@Transient
	private MultipartFile imageDocument;

	public Question() {
	}

	public Question(Id id, Paper paper, boolean hasImage, String query, List<String> options, Byte answerIndex, byte[] imageData, MultipartFile imageDocument) {
		this.id = id;
		this.paper = paper;
		this.hasImage = hasImage;
		this.query = query;
		this.options = options;
		this.answerIndex = answerIndex;
		this.imageData = imageData;
		this.imageDocument = imageDocument;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public Paper getPaper() {
		return paper;
	}

	public void setPaper(Paper paper) {
		this.paper = paper;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public Byte getAnswerIndex() {
		return answerIndex;
	}

	public void setAnswerIndex(Byte answerIndex) {
		this.answerIndex = answerIndex;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public MultipartFile getImageDocument() {
		return imageDocument;
	}

	public void setImageDocument(MultipartFile imageDocument) {
		this.imageDocument = imageDocument;
	}

	public void deleteImage() throws Exception {
		imageData = null;
        hasImage = false;
	}

	public void saveImage(MultipartFile image) throws Exception {
		if (!image.isEmpty()) {
			imageData = image.getBytes();
			hasImage = true;
		}
	}

	public static QuestionBuilder builder() {
		return new QuestionBuilder();
	}

	public static class QuestionBuilder {

		private Id id;
		private Paper paper;
		private boolean hasImage;
		private String query;
		private List<String> options;
		private Byte answerIndex;
		private byte[] imageData;
		private MultipartFile imageDocument;

		public QuestionBuilder() {
			Question question = new Question();
			this.id = question.id;
			this.paper = question.paper;
			this.hasImage = question.hasImage;
			this.query = question.query;
			this.options = question.options;
		}

		public QuestionBuilder id(Id id) {
			this.id = id;
			return this;
		}

		public QuestionBuilder paper(Paper paper) {
			this.paper = paper;
			return this;
		}

		public QuestionBuilder hasImage(boolean hasImage) {
			this.hasImage = hasImage;
			return this;
		}

		public QuestionBuilder query(String query) {
			this.query = query;
			return this;
		}

		public QuestionBuilder options(List<String> options) {
			this.options = options;
			return this;
		}

		public QuestionBuilder answerIndex(Byte answerIndex) {
			this.answerIndex = answerIndex;
			return this;
		}

		public QuestionBuilder imageData(byte[] imageData) {
			this.imageData = imageData;
			return this;
		}

		public QuestionBuilder imageDocument(MultipartFile imageDocument) {
			this.imageDocument = imageDocument;
			return this;
		}

		public Question build() {
			return new Question(id, paper, hasImage, query, options, answerIndex, imageData, imageDocument);
		}
	}

	@Override
	public int compareTo(@NotNull Question o) {
		return Integer.compare(this.id.number, o.id.number);
	}

	@Embeddable
	public static class Id {

		private Paper.Id paperId;

		@Column(name = "QUESTION_NUMBER")
		private Integer number;

		public Id() {
		}

		public Id(Paper.Id paperId, Integer number) {
			this.paperId = paperId;
			this.number = number;
		}

		public Paper.Id getPaperId() {
			return paperId;
		}

		public void setPaperId(Paper.Id paperId) {
			this.paperId = paperId;
		}

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
		}

		public static IdBuilder builder() {
			return new IdBuilder();
		}

		public static class IdBuilder {

			private Paper.Id paperId;
			private Integer number;

			public IdBuilder() {
				Id id = new Id();
				this.paperId = id.paperId;
				this.number = id.number;
			}

			public IdBuilder paperId(Paper.Id id) {
				paperId = id;
				return this;
			}

			public IdBuilder number(Integer number) {
				this.number = number;
				return this;
			}

			public Id build() {
				return new Id(paperId, number);
			}
		}
	}
}
