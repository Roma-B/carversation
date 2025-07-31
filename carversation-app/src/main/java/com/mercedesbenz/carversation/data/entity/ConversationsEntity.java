package com.mercedesbenz.carversation.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Timestamp;

@Entity(name = "conversations")
@Table(name = "conversations")
@Getter
@Setter
public class ConversationsEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "sender_vin", nullable = false)
    private String senderVin;
    @Column(name = "receiver_vin", nullable = false)
    private String receiverVin;
    @CurrentTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
}
