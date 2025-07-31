package com.mercedesbenz.carversation.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;

@Entity(name = "messages")
@Table(name = "messages")
@Getter
@Setter
public class MessagesEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "sender_vin", nullable = false)
    private String senderVin;
    @Column(name = "receiver_vin", nullable = false)
    private String receiverVin;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "message_type", nullable = true)
    private String messageType;
    @Column(name = "created_at")
    private Timestamp createdAt;
}
