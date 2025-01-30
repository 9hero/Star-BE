package com.mercury.star_be.studygroup.dto.response;

import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class NoticeUpdateResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createAt;


}
