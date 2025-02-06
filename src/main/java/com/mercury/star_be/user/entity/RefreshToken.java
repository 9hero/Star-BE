package com.mercury.star_be.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@NoArgsConstructor
@Entity(name = "refreshtokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false) // 외래키 설정
    private User user;

    @Setter
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Setter
    @Column(name = "expired_at", nullable = false)
    private Date expiredAt;

    // Builder 수정: expiredAt 외에도 isActive도 설정 가능하도록 추가
    @Builder
    public RefreshToken(Boolean isActive, User user, Date expiredAt) {
        this.isActive = (isActive != null) ? isActive : true; // 기본값 true 설정
        this.user = user;
        this.expiredAt = expiredAt;
    }

    // @PrePersist를 사용하여 createdAt 및 isActive 자동 설정
    @PrePersist
    public void onPrePersist() {
        // this.createdAt = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        this.createdAt = new Date(System.currentTimeMillis());
        if (this.isActive == null) {
            this.isActive = true; // 기본값을 true로 설정
        }
    }

}
