package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExamExporter {

	@EqualsAndHashCode(callSuper = false)
	@NoArgsConstructor
	public static class ExportConfig extends Exam.CandidateConfig {
		private boolean username = false;
		private String usernameDesc;
		private boolean scoreAsPercentile = true;
		private String totalDesc = "Total";
		private int cutOffPercentile = 0;
		public ExportConfig(Exam.CandidateConfig base) {
			super(
					base.isEmail(), base.getEmailDesc(),
					base.isPhoneNumber(), base.getPhoneNumberDesc(),
					base.isAddress(), base.getAddressDesc(),
					base.isState(), base.getStateDesc(),
					base.isFirstname(), base.getFirstnameDesc(),
					base.isLastname(), base.getLastnameDesc(),
					base.isOtherNames(), base.getOtherNamesDesc(),
					base.isRegistrationNumber(), base.getRegistrationNumberDesc()
			);
		}
		public String getHeader() {
			StringBuilder sb = new StringBuilder();
			if (username)
				sb.append(usernameDesc).append(",");
			if (isRegistrationNumber())
				sb.append(getRegistrationNumberDesc()).append(",");
			if (isLastname())
				sb.append(getLastnameDesc()).append(",");
			if (isFirstname())
				sb.append(getFirstnameDesc()).append(",");
			if (isOtherNames())
				sb.append(getOtherNamesDesc()).append(",");
			if (isEmail())
				sb.append(getEmailDesc()).append(",");
			if (isPhoneNumber())
				sb.append(getPhoneNumberDesc()).append(",");
			if (isAddress())
				sb.append(getAddressDesc()).append(",");
			if (isState())
				sb.append(getStateDesc()).append(",");
			sb.append(totalDesc).append("\n");
			return sb.toString();
		}

		public boolean isUsername() {
			return username;
		}

		public void setUsername(boolean username) {
			this.username = username;
		}

		public String getUsernameDesc() {
			return usernameDesc;
		}

		public void setUsernameDesc(String usernameDesc) {
			this.usernameDesc = usernameDesc;
		}

		public boolean isScoreAsPercentile() {
			return scoreAsPercentile;
		}

		public void setScoreAsPercentile(boolean scoreAsPercentile) {
			this.scoreAsPercentile = scoreAsPercentile;
		}

		public String getTotalDesc() {
			return totalDesc;
		}

		public void setTotalDesc(String totalDesc) {
			this.totalDesc = totalDesc;
		}

		public int getCutOffPercentile() {
			return cutOffPercentile;
		}

		public void setCutOffPercentile(int cutOffPercentile) {
			this.cutOffPercentile = cutOffPercentile;
		}
	}

	private ExportConfig config;

	public ExportConfig getConfig() {
		return config;
	}

	public void setConfig(ExportConfig config) {
		this.config = config;
	}

	public void setConfig(Exam exam) {
		config = new ExportConfig(exam.getCandidateConfig());
		config.setUsernameDesc(exam.getUsernameDesc());
	}

	public String exportToCSV(Exam exam) {
		StringBuilder body = new StringBuilder();
		var candidates = exam.getCandidates().stream()
				.filter(candidate -> {
					if (config.getCutOffPercentile() > 0) {
						float percentile = Float.parseFloat(candidate.getFormattedScore(config.isScoreAsPercentile()));
						return percentile >= ((float) config.getCutOffPercentile()) - 0.1f;
					}
					return true;
				})
				.sorted()
				.toList();
		for (Candidate candidate : candidates) {
			if (config.isUsername())
				body.append('"').append(candidate.getId().getUsername()).append('"').append(",");
			if (config.isRegistrationNumber())
				body.append(candidate.getRegistrationNumber()).append(",");
			if (config.isLastname())
				body.append(candidate.getLastname()).append(",");
			if (config.isFirstname())
				body.append(candidate.getFirstname()).append(",");
			if (config.isOtherNames())
				body.append('"').append(candidate.getOtherNames()).append('"').append(",");
			if (config.isEmail())
				body.append('"').append(candidate.getEmail()).append('"').append(",");
			if (config.isPhoneNumber())
				body.append('"').append(candidate.getPhoneNumber()).append('"').append(",");
			if (config.isAddress())
				body.append('"').append(candidate.getAddress()).append('"').append(",");
			if (config.isState())
				body.append(candidate.getState()).append(",");
			body.append(candidate.getFormattedScore(config.isScoreAsPercentile())).append("%\n");
		}
		return config.getHeader() + body;
	}
}
