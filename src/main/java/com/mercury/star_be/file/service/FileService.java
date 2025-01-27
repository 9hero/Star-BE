package com.mercury.star_be.file.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.mercury.star_be.file.dto.response.ImageUploadResponse;

public interface FileService {

	ImageUploadResponse uploadImage(MultipartFile file) throws IOException;
}
