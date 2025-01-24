package com.mercury.star_be.user.service;

import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
}
