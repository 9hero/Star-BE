package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.dto.request.ChatUpdateReadMessagesRequest;
import com.mercury.star_be.chat.entity.QChatMessage;
import com.querydsl.core.dml.InsertClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.mercury.star_be.chat.entity.QChatMessage.chatMessage;
import static com.mercury.star_be.chat.entity.QChatRead.chatRead;
@Repository
@RequiredArgsConstructor
public class ChatCustomRepositoryImpl implements ChatCustomRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Long> findUnreadMessageIds(Long chatRoomId, Long userId) {
        return queryFactory
                .select(chatMessage.id)
                .from(chatMessage)
                .leftJoin(chatMessage.chatReads, chatRead)
                .on(chatRead.chatUser.id.eq(userId))
                .where(chatMessage.chatRoom.id.eq(chatRoomId)
                        .and(chatRead.chatMessage.isNull()))
                .fetch();

    }

    @Override
    @Transactional
    public void insertChatReads(ChatUpdateReadMessagesRequest request, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        StringBuilder sql = new StringBuilder("INSERT INTO chat_read (chat_message_id, chat_user_id, created_at) VALUES ");
        for (int i = 0; i < request.getUnreadMessages().size(); i++) {
            sql.append("(")
                    .append(request.getUnreadMessages().get(i)).append(", ")
                    .append(userId).append(", ")
                    .append("'").append(now).append("')");
            if (i < request.getUnreadMessages().size() - 1) {
                sql.append(", ");
            }
        }

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }

    @Override
    public void updateChatReads(ChatUpdateReadMessagesRequest request) {
        QChatMessage chatMessage = QChatMessage.chatMessage;

        long updatedCount = queryFactory
                .update(chatMessage)
                .set(chatMessage.unreadCount, chatMessage.unreadCount.subtract(1))
                //unreadCount가 0보다 클때
                .where(
                        chatMessage.id.in(request.getUnreadMessages())
                                .and(chatMessage.unreadCount.gt(0))
                )
                .execute();

        if (updatedCount > 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }

}
