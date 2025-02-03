package com.mercury.star_be.user.controller;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.service.UserBlockService;
import com.mercury.star_be.user.service.UserService;
import com.mercury.star_be.user.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserBlockService userBlockService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @RequestMapping("/api/users")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ApiResponse.success(userResponse);
    }

    @GetMapping("/api/check-auth")
    public ResponseEntity<String> checkAuth() {
        System.out.println("api/check-auth");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken) ?
                ResponseEntity.status(200).body("Authenticated") :
                ResponseEntity.status(401).body("Not Authenticated");
    }

    @GetMapping("/api/user-info")
    public ResponseEntity<Map<String, Object>> getuserInfo(Authentication auth) {
        return jwtUtil.getAuthenticatedUser(auth) != null ?
                ResponseEntity.status(200).body(userService.getUserInfo(auth)) :
                ResponseEntity.status(401).body(null);
    }


    @PostMapping("/api/user-info")
    public ResponseEntity<String> updateUserInfo(
            Authentication auth,
            @RequestParam("nickname") String nickname,  // nickname 파라미터
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg) throws UnsupportedEncodingException {  // 이미지 파일 파라미터
        System.out.println("Received nickname: " + nickname);
        if (profileImg != null) {
            System.out.println("Received profile image: " + profileImg.getOriginalFilename());
        } else {
            System.out.println("No profile image received.");
        }

        if (jwtUtil.getAuthenticatedUser(auth) != null) {
            userService.updateUserInfo(auth, nickname, profileImg);
            return ResponseEntity.status(200).body("Success");
        }
        return ResponseEntity.status(401).body(null);
    }



    @DeleteMapping("/api/user-info")
    public ResponseEntity<String> deleteUserInfo(Authentication auth) {
        if( jwtUtil.getAuthenticatedUser(auth) != null ){
            userService.deleteUserInfo(auth);
            return ResponseEntity.status(200).body("Success");
        }
        return ResponseEntity.status(401).body(null);
    }


    /*
     * 사용자 차단
     */
    @PostMapping("/api/users/blocks")
    public ApiResponse<Void> blockUser(@RequestBody UserBlockRequest userBlockRequest, Authentication auth) {
        UserResponse user = jwtUtil.getAuthenticatedUser(auth);
        userBlockService.blockUser(user.getId(), userBlockRequest);
        return ApiResponse.success();
    }
}
