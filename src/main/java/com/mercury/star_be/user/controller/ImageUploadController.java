package com.mercury.star_be.user.controller;

import com.mercury.star_be.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ImageUploadController {

    private final UserServiceImpl userService;

    @PostMapping("/api/images/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 이미지 저장 처리 로직 (예: 클라우드 또는 로컬 디렉토리)
            String imageUrl = userService.saveImage(file);
            // 2. 성공 시 URL 반환
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }

}

