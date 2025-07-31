package com.mercedesbenz.carversation.repository;

import com.mercedesbenz.carversation.data.entity.MessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessagesEntity, Long> {


}
