package com.mercury.star_be.user.dto.response;

import com.mercury.star_be.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private String provider;
    private String image;
    private boolean isActive;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.provider = user.getProvider();
        this.image = user.getImage();
        this.isActive = user.isActive();
        this.createdAt = user.getCreatedAt();
    }
}