package com.mercury.star_be.user.dto.response;

import com.mercury.star_be.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse extends User implements OAuth2User {

    private Long id;
    private String email;
    private String nickname;
    private String provider;
    private String image;
    private boolean isActive;
    private LocalDateTime createdAt;
    private String oauthId;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.provider = user.getProvider();
        this.image = user.getImage();
        this.isActive = user.isActive();
        this.createdAt = user.getCreatedAt();
        this.oauthId = user.getOauthId();
    }



    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return "ROLE_USER";
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return this.getNickname();
    }

}