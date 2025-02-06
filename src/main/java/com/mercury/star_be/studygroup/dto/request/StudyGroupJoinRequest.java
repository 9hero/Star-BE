package com.mercury.star_be.studygroup.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupJoinRequest {
    // 그룹이 비밀번호로 보호되어 있다면 클라이언트에서 보낼 비밀번호 (필수는 아님)
    private String password;
}
