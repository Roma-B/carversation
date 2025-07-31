package com.mercedesbenz.carversation.service;

import com.mercedesbenz.carversation.data.entity.ChatRequestsEntity;
import com.mercedesbenz.carversation.data.entity.ConversationsEntity;
import com.mercedesbenz.carversation.data.entity.MessagesEntity;
import com.mercedesbenz.carversation.repository.ChatRequestsRepository;
import com.mercedesbenz.carversation.repository.ConversationsRepository;
import com.mercedesbenz.carversation.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ConversationsRepository conversationsRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRequestsRepository chatRequestsRepository;

    public List<MessagesEntity> GetAllMessagesByConversationId(String conversationId) {
        // Fetch all messages for the given conversation ID
        return messageRepository.findByConversationId(conversationId);
    }

    // Saves messages
    public void SaveMessages(String conversationId, String sender, String receiver, String message) {
        // Create a new message entity
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setId(String.valueOf(UUID.randomUUID()));
        messageEntity.setConversationId(conversationId);
        messageEntity.setSenderVin(sender);
        messageEntity.setReceiverVin(receiver);
        messageEntity.setContent(message);
        messageEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // Save the message to the repository
        messageRepository.save(messageEntity);
    }

    public void SaveOrUpdateChatRequest(String chatId, String senderVin, String receiverVin, String status) {
        // Create a new chat request entity
        ChatRequestsEntity chatRequest = new ChatRequestsEntity();
        if (chatRequestsRepository.findById(chatId) != null) {
            // If the chat request already exists, update it
            ChatRequestsEntity existingRequest = chatRequestsRepository.findById(chatId);
            existingRequest.setStatus(status);
            chatRequest = existingRequest;
        } else {
            chatRequest.setId(String.valueOf(chatId));
            chatRequest.setSenderVin(senderVin);
            chatRequest.setReceiverVin(receiverVin);
            chatRequest.setStatus(status);
            chatRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        // Save the chat request to the repository
        chatRequestsRepository.save(chatRequest);
    }

    public void createConversation(String conversationId, String senderVin, String receiverVin) {
        ConversationsEntity conversation = new ConversationsEntity();
        conversation.setId(conversationId);
        conversation.setSenderVin(senderVin);
        conversation.setReceiverVin(receiverVin);
        conversation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        conversationsRepository.save(conversation);
    }
}
