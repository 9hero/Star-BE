package com.mercury.star_be.file.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.mercury.star_be.file.dto.response.ImageUploadResponse;
import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.FileErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GcsFileServiceImpl implements FileService {

	@Value("${spring.cloud.gcp.storage.bucket}")
	private String bucketName;

	private static final String GCS_FILE_PREFIX = "https://storage.googleapis.com/";

	private final Storage storage;

	@Override
	public ImageUploadResponse uploadImage(MultipartFile file) {
		String uuid = UUID.randomUUID().toString();
		String contentType = file.getContentType();
		try {
			storage.create(
				BlobInfo.newBuilder(bucketName, uuid)
					.setContentType(contentType)
					.build(),
				file.getBytes()
			);
		} catch (IOException e) {
			throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
		}

		String url = GCS_FILE_PREFIX + bucketName + "/" + uuid;
		return new ImageUploadResponse(url);
	}
}
