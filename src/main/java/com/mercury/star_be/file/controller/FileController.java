package com.mercury.star_be.file.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mercury.star_be.file.dto.response.ImageUploadResponse;
import com.mercury.star_be.file.service.FileService;
import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.FileErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FileController {

	private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
		"image/png", "image/jpeg", "image/gif", "image/webp"
	);

	private final FileService fileService;

	@PostMapping("/api/files/image")
	public ApiResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
		}

		String contentType = file.getContentType();
		if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
			throw new BusinessException(FileErrorCode.UNSUPPORTED_IMAGE_TYPE);
		}

		ImageUploadResponse imageUploadResponse = fileService.uploadImage(file);
		return ApiResponse.success(imageUploadResponse);
	}
}
