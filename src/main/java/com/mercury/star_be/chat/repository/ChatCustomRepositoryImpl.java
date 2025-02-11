package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.dto.request.ChatUpdateReadMessagesRequest;
import com.mercury.star_be.chat.entity.ChatRoomType;
import com.mercury.star_be.chat.entity.QChatMessage;
import com.mercury.star_be.chat.entity.QChatRoom;
import com.mercury.star_be.chat.entity.QUserChatRoom;
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
    public void insertUnreadMessagesToChatRead(Long chatRoomId, Long userId) {
        List<Long> unreadMessageIds = findUnreadMessageIds(chatRoomId, userId);

        if (!unreadMessageIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();

            StringBuilder sql = new StringBuilder("INSERT INTO chat_read (created_at, chat_message_id, chat_user_id) VALUES ");
            for (int i = 0; i < unreadMessageIds.size(); i++) {
                sql.append("(")
                        .append("'").append(now).append("', ")
                        .append(unreadMessageIds.get(i)).append(", ")
                        .append(userId).append(")");
                if (i < unreadMessageIds.size() - 1) {
                    sql.append(", ");
                }
            }

            entityManager.createNativeQuery(sql.toString()).executeUpdate();
        }
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
    @Transactional
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

    @Override
    public Long findChatRoomIdByUserIds(Long senderId, Long receiverId) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QUserChatRoom userChatRoom1 = new QUserChatRoom("userChatRoom1");
        QUserChatRoom userChatRoom2 = new QUserChatRoom("userChatRoom2");

        return queryFactory
                .select(chatRoom.id)
                .from(chatRoom)
                .join(userChatRoom1).on(chatRoom.id.eq(userChatRoom1.chatRoom.id))
                .join(userChatRoom2).on(chatRoom.id.eq(userChatRoom2.chatRoom.id))
                .where(chatRoom.chatRoomType.eq(ChatRoomType.DM)
                        .and(userChatRoom1.chatUser.id.eq(senderId))
                        .and(userChatRoom2.chatUser.id.eq(receiverId)))
                .fetchOne();
    }


    @Override
    public Long findExistingChatRoomId(Long senderId, Long receiverId) {
        QChatRoom chatRoom = QChatRoom.chatRoom;

        return queryFactory
                .select(chatMessage.chatRoom.id)
                .from(chatMessage)
                .join(chatMessage.chatRoom, chatRoom)
                .where((chatMessage.chatReceiver.id.eq(senderId).and(chatMessage.chatSender.id.eq(receiverId)))
                        .or(chatMessage.chatReceiver.id.eq(receiverId).and(chatMessage.chatSender.id.eq(senderId)))
                        .and(chatRoom.chatRoomType.eq(ChatRoomType.DM)))
                .limit(1)
                .fetchOne();
    }

    // 새로운 메서드 추가: 두 사람 간의 채팅 기록 수를 카운트
    public Long countByChatSenderIdAndChatReceiverIdAndRoomTypeDM(Long senderId, Long receiverId) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        return queryFactory
                .select(chatMessage.count())
                .from(chatMessage)
                .join(chatMessage.chatRoom, chatRoom)
                .where(
                        chatRoom.chatRoomType.eq(ChatRoomType.DM)
                                .and(
                                        (chatMessage.chatSender.id.eq(senderId).and(chatMessage.chatReceiver.id.eq(receiverId)))
                                                .or(chatMessage.chatSender.id.eq(receiverId).and(chatMessage.chatReceiver.id.eq(senderId)))
                                )
                )
                .fetchOne();
    }





}
