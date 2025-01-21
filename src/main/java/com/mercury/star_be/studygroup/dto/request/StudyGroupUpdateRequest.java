package com.mercury.star_be.studygroup.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupUpdateRequest {

	private String name;
	private String description;
	private String image;
	private int maxCapacity;
	private boolean hasPassword;
	private String password;
	private boolean isPublic;

	public boolean hasPassword() {
		return hasPassword;
	}
}
