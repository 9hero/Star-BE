package com.mercury.star_be.global.common;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

	private boolean success = true;
	private T data;

	public ApiResponse(T data) {
		this.data = data;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(data);
	}

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>();
	}
}
