package com.mercury.star_be.studygroup.dto.request;

import lombok.Getter;

@Getter
public class StudyGroupCreateRequest {

	
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
