package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.entity.ChatMessageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageFileRepository extends JpaRepository<ChatMessageFile, Long> {
}
