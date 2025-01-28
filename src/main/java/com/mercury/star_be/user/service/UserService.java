package com.mercury.star_be.user.service;

import com.mercury.star_be.user.dto.request.UserRequest;
import com.mercury.star_be.user.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
}