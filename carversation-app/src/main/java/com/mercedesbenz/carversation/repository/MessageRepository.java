package com.mercedesbenz.carversation.repository;

import com.mercedesbenz.carversation.data.entity.MessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessagesEntity, Long> {

    List<MessagesEntity> findByConversationId(String conversationId);
}
