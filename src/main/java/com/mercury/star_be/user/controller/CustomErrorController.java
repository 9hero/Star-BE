package com.mercury.star_be.user.controller;

import com.mercury.star_be.global.error.CustomAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public void handleError(HttpServletRequest request) {
        // 포워딩 시 저장한 예외를 가져옵니다.
        Object exceptionObj = request.getAttribute("javax.servlet.error.exception");

        if (exceptionObj instanceof CustomAuthenticationException ex) {
            throw new CustomAuthenticationException(ex.getErrorCode());
        } else if (exceptionObj instanceof RuntimeException ex) {
            throw ex;
        } else {
            // 예외 정보가 없는 경우 기본 에러 처리
            throw new RuntimeException("Unknown error occurred");
        }
    }
}
