package com.mercury.star_be.user.controller;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.global.error.CustomAuthenticationException;
import com.mercury.star_be.global.error.code.AuthenticationErrorCode;
import com.mercury.star_be.user.Handler.CustomSuccessHandler;
import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.request.UserUnblockRequest;
import com.mercury.star_be.user.dto.response.BlockUserListResponse;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.repository.RefreshRepository;
import com.mercury.star_be.user.repository.UserRepository;
import com.mercury.star_be.user.service.BlockUserService;
import com.mercury.star_be.user.service.UserService;
import com.mercury.star_be.user.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BlockUserService blockUserService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final CustomSuccessHandler customSuccessHandler;


    @PostMapping("/api/auth/reissue")
    public ResponseEntity<String> reissue(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws ServletException, IOException {
        try {
            if (userService.reissue(req, res, auth))
                return ResponseEntity.status(200).body("reissue Success");
            else {
                return ResponseEntity.status(401).body("reissue Fail");
            }
        } catch (Exception e) {
            throw new CustomAuthenticationException(AuthenticationErrorCode.NOTEXIST_ID_ACCESSTOKEN);
        }
    }

    @PostMapping
    @RequestMapping("/api/users")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ApiResponse.success(userResponse);
    }


    @GetMapping("/api/check-auth")
    public ResponseEntity<String> checkAuth(Authentication auth) {
        if (JwtUtil.getAuthenticatedUser(auth) != null) {
            return ResponseEntity.status(200).body("Authenticated");
        }
        throw new CustomAuthenticationException(AuthenticationErrorCode.MISSING_ACCESSTOKEN);
    }


    @GetMapping("/api/user-info")
    public ResponseEntity<Map<String, Object>> getuserInfo(Authentication auth) {
        if (JwtUtil.getAuthenticatedUser(auth) != null) {
            return ResponseEntity.status(200).body(userService.getUserInfo(auth));
        }
        throw new CustomAuthenticationException(AuthenticationErrorCode.MISSING_ACCESSTOKEN);
    }


    @PostMapping("/api/user-info")
    public ResponseEntity<String> updateUserInfo(
            Authentication auth,
            @RequestParam("nickname") String nickname,  // nickname 파라미터
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg) throws UnsupportedEncodingException {  // 이미지 파일 파라미터
        if (JwtUtil.getAuthenticatedUser(auth) != null) {
            userService.updateUserInfo(auth, nickname, profileImg);
            return ResponseEntity.status(200).body("Success");
        }
        throw new CustomAuthenticationException(AuthenticationErrorCode.MISSING_ACCESSTOKEN);
    }


    @DeleteMapping("/api/user-info")
    public ResponseEntity<String> deleteUserInfo(Authentication auth) {
        if (JwtUtil.getAuthenticatedUser(auth) != null) {
            userService.deleteUserInfo(auth);
            return ResponseEntity.status(200).body("Success");
        }
        throw new CustomAuthenticationException(AuthenticationErrorCode.MISSING_ACCESSTOKEN);
    }


    /**
     * 사용자 차단
     */
    @PostMapping("/api/users/blocks")
    public ApiResponse<Void> blockUser(@RequestBody UserBlockRequest userBlockRequest, Authentication auth) {
        UserResponse user = jwtUtil.getAuthenticatedUser(auth);
        blockUserService.blockUser(user.getId(), userBlockRequest);
        return ApiResponse.success();
    }

    /**
     * 사용자 차단 해제
     */
    @DeleteMapping("/api/users/blocks")
    public ApiResponse<Void> unblockUser(@RequestBody UserUnblockRequest userUnblockRequest, Authentication auth) {
        UserResponse user = jwtUtil.getAuthenticatedUser(auth);
        blockUserService.unblockUser(user.getId(), userUnblockRequest);
        return ApiResponse.success();
    }

    /**
     * 차단 사용자 목록 조회
     */
    @GetMapping("/api/users/blocks")
    public ApiResponse<List<BlockUserListResponse>> getBlockUserList(Authentication auth) {
        UserResponse user = jwtUtil.getAuthenticatedUser(auth);
        List<BlockUserListResponse> blockUserList = blockUserService.getBlockUserList(user.getId());
        return ApiResponse.success(blockUserList);
    }
}
