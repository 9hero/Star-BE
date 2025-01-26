package com.mercury.star_be.studygroup.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NoticeUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 50, message = "공지사항의 제목은 2자 이상 100자 이하여야 합니다.")
    private String title;

    @NotBlank
    @Size(min = 2, max = 1000, message = "공지사항의 내용은 2자 이상 1000자 이하여야 합니다.")
    private String content;
}
