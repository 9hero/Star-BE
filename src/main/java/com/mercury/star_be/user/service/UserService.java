package com.mercury.star_be.user.service;

import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.Oauth2Response;
import com.mercury.star_be.user.dto.response.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);
    UserResponse saveUser(Oauth2Response oauth2Response, String username);
    Map<String, Object> getUserInfo(Authentication auth);
    void updateUserInfo(Authentication auth, String nickname , MultipartFile profileImg) throws UnsupportedEncodingException;
    void deleteUserInfo(Authentication auth);

    // void updateUserInfo(Authentication , UserResponse );
}