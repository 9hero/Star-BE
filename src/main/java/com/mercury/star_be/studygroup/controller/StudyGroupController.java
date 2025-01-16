package com.mercury.star_be.studygroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {
    @GetMapping("/api/testData")
    public ResponseEntity<String> testData() {
        return ResponseEntity.ok("Hello front!! im backend Data~");
    }
}
