package com.mercury.star_be.user.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.mercury.star_be.studygroup.dto.request.GroupLeaveRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.Oauth2Response;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);
    UserResponse saveUser(Oauth2Response oauth2Response, String username);
    Map<String, Object> getUserInfo(Authentication auth);
    void updateUserInfo(Authentication auth, String nickname , MultipartFile profileImg) throws IOException;
    void deleteUserInfo(GroupLeaveRequest request, HttpServletRequest httpServletReq, Authentication auth);
    boolean reissue(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws ServletException, IOException;
    // void updateUserInfo(Authentication , UserResponse );
    User findById(Long userId);
}