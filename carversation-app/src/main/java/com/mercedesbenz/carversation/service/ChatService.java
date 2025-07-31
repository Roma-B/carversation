package com.mercedesbenz.carversation.service;

import com.mercedesbenz.carversation.data.entity.ChatRequestsEntity;
import com.mercedesbenz.carversation.data.entity.ConversationsEntity;
import com.mercedesbenz.carversation.data.entity.MessagesEntity;
import com.mercedesbenz.carversation.repository.ChatRequestsRepository;
import com.mercedesbenz.carversation.repository.ConversationsRepository;
import com.mercedesbenz.carversation.repository.MessageRepository;
import com.mercedesbenz.carversation.util.GlobalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ConversationsRepository conversationsRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRequestsRepository chatRequestsRepository;

    // Saves messages
    public void SaveMessages(String sender, String receiver, String message) {
        String conversationId = "";
        // get a conversationId, if a conversation already exists, otherwise create a new one
        HashSet<String> id = (HashSet<String>) Set.of(sender, receiver);
        if (GlobalStore.CONVERSATION_ID_MAP.get(id) == null) {
            conversationId = createConversation(sender, receiver);
            GlobalStore.CONVERSATION_ID_MAP.put(id, conversationId);
        }
        // Create a new message entity
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setId(conversationId);
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
        }else{
            chatRequest.setId(String.valueOf(chatId));
            chatRequest.setSenderVin(senderVin);
            chatRequest.setReceiverVin(receiverVin);
            chatRequest.setStatus(status);
            chatRequest.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        // Save the chat request to the repository
        chatRequestsRepository.save(chatRequest);
    }

    public String createConversation(String senderVin, String receiverVin)  {
        ConversationsEntity conversation = new ConversationsEntity();
        String conversationId = String.valueOf(UUID.randomUUID());
        conversation.setId(conversationId);
        conversation.setSenderVin(senderVin);
        conversation.setReceiverVin(receiverVin);
        conversation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        conversationsRepository.save(conversation);
        return conversationId;
    }
}
