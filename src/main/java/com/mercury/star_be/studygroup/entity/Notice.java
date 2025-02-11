package com.mercury.star_be.studygroup.entity;

import com.mercury.star_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String title;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Column(length = 20)
    private String writerNickname;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false) // 그룹 아이디로 외래키 설정
    private StudyGroup studyGroup;

    @Builder
    public Notice(String title, String content, String writerNickname, LocalDateTime createdAt, User writer,
        StudyGroup studyGroup) {
        this.title = title;
        this.content = content;
        this.writerNickname = writerNickname;
        this.createdAt = createdAt;
        this.writer = writer;
        this.studyGroup = studyGroup;
    }

    public void updateNotice(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
