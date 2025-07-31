package com.mercedesbenz.carversation.repository;

import com.mercedesbenz.carversation.data.entity.ChatRequestsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRequestsRepository extends JpaRepository<ChatRequestsEntity, Long> {
    ChatRequestsEntity findById(String id);
}
