package com.mercury.star_be.user.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.UserErrorCode;
import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.GoogleResponse;
import com.mercury.star_be.user.dto.response.NaverResponse;
import com.mercury.star_be.user.dto.response.Oauth2Response;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.dto.response.kakaoResponse;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import com.mercury.star_be.user.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends DefaultOAuth2UserService implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        User user = User.builder()
                .email(userRequest.getEmail())
                .nickname(userRequest.getNickname())
                .provider(userRequest.getProvider())
                .image(userRequest.getImage())

                .build();

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("CustomOAuth2UserService: " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Oauth2Response oauth2Response = null;

        if (registrationId.equals("naver")) {
            oauth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oauth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oauth2Response = new kakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }
        String oauthId = oauth2Response.getProvider() + "_" + oauth2Response.getProviderId();

        // DB save
        UserResponse userResponseDto = saveUser(oauth2Response, oauthId);
        return (OAuth2User) userResponseDto;
    }

    /**
     * 이미 존재하는 경우 update,
     * 존재하지 않는 경우 save
     */
    @Override
    public UserResponse saveUser(Oauth2Response oauth2Response, String oauthId) {
        // DB 조회
        User existData = userRepository.findByoauthId(oauthId);
        if (existData == null) {
            User user = User.builder()
                    .email(oauth2Response.getEmail())
                    .nickname(oauth2Response.getName())
                    .provider(oauth2Response.getProvider())
                    .image(oauth2Response.getImage())
                    .oauthId(oauthId)
                    .build();
            User savedUser = userRepository.save(user);
            return new UserResponse(savedUser);
        }

        return new UserResponse(existData);
    }


    @Override
    public Map<String, Object> getUserInfo(Authentication auth) {
        // 인증된 사용자 정보 가져오기 (JWT로부터 사용자 ID만 가져오고, DB에서 최신 정보 조회)
        UserResponse userResponse = jwtUtil.getAuthenticatedUser(auth);

        // DB에서 최신 유저 정보 조회
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 유저 정보 Map에 담기
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", user.getEmail());
        userInfo.put("provider", user.getProvider());
        userInfo.put("created_at", user.getCreatedAt());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("profileImgUrl", user.getImage());
        return userInfo;
    }

    @Override
    public void updateUserInfo(Authentication auth, String nickname, MultipartFile profileImg) throws UnsupportedEncodingException {

        UserResponse authenticatedUser = jwtUtil.getAuthenticatedUser(auth);
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2. 닉네임 업데이트
        user.setNickname(nickname);
        // 3. 프로필 이미지가 있는 경우 파일 저장 후 URL 저장
        if (profileImg != null && !profileImg.isEmpty()) {
            String imageUrl = saveImage(profileImg);
            user.setImage(imageUrl);
        }

        // DB에 업데이트된 유저 저장
        userRepository.save(user);
    }



    public String saveImage(MultipartFile file) throws UnsupportedEncodingException {
        // 파일 저장 로직 구현
        // 예: 로컬 저장소에 저장 후 URL 생성
        String uploadDirectory = Paths.get("src/main/resources/static/fileupload/").toAbsolutePath().toString(); // 절대 경로
        String fileName = UUID.randomUUID().toString() + "_" + URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8);

        Path path = Paths.get(uploadDirectory + "/" + fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패");
        }
        // 저장된 URL 반환
        return "http://34.22.98.26:8080/fileupload/" + fileName;
    }


    @Override
    public void deleteUserInfo(Authentication auth) {
        UserResponse authenticatedUser = jwtUtil.getAuthenticatedUser(auth);
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // DB에 업데이트된 유저 저장
        userRepository.delete(user);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_EXIST));
    }
}
