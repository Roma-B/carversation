package com.mercedesbenz.carversation.repository;

import com.mercedesbenz.carversation.data.entity.ConversationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationsRepository extends JpaRepository<ConversationsEntity, Long> {
   public String findBySenderVinAndReceiverVin(String senderVin, String receiverVin);
}
