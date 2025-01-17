package com.mercury.star_be.global.error;

import lombok.Getter;

@Getter
public class ErrorResponse {

	private boolean success = false;
	private String status;
	private String message;

	public ErrorResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}
}
