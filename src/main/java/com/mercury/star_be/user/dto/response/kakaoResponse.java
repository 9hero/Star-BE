package com.mercury.star_be.user.dto.response;

import java.util.Map;
import java.util.Optional;

public class kakaoResponse implements Oauth2Response {


    private final Map<String, Object> attribute;
    private final String id;

    public kakaoResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("kakao_account");
        this.id = String.valueOf(attribute.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return Optional.ofNullable(attribute)
                .map(attr -> (Map<String, Object>) attr.get("profile"))
                .map(profile -> profile.get("nickname"))
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public String getImage() {
        return Optional.ofNullable(attribute)
                .map(attr -> (Map<String, Object>) attr.get("profile"))
                .map(profile -> profile.get("profile_image_url"))
                .map(Object::toString)
                .orElse(null);
    }
}
