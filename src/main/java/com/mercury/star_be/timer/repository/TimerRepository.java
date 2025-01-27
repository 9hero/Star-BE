package com.mercury.star_be.timer.repository;

import com.mercury.star_be.timer.entity.Timer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimerRepository extends JpaRepository<Timer, Long> {
    List<Timer> findByStudyGroupId(Long groupId);

    @Query("SELECT t FROM Timer t WHERE t.studyGroup.id = :groupId AND t.user.id = :userId")
    Timer findByStudyGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT t FROM Timer t WHERE t.studyGroup.id = :groupId AND t.user.id IN :userIds")
    List<Timer> findByStudyGroupIdAndUserIdIn(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);

    @Query("SELECT t FROM Timer t " +
            "WHERE t.studyGroup.id = :groupId " +
            "AND t.user.id IN :userIds " +
            "AND t.studyDate = CURRENT_DATE") // 현재 날짜와 비교
    List<Timer> findByStudyGroupIdAndUserIdInAndToday(
            @Param("groupId") Long groupId,
            @Param("userIds") List<Long> userIds
    );
    @Query("SELECT t FROM Timer t " +
            "JOIN t.user u " +
            "WHERE t.studyGroup.id = :groupId " +
            "AND u.id IN :userIds " +
            "AND t.studyDate = CURRENT_DATE")
    List<Timer> findTimersWithUsersBygidAnduid(@Param("groupId") Long groupId,
                                               @Param("userIds") List<Long> userIds);
}
