package com.mercedesbenz.carversation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessagePayload {
    @JsonProperty("senderVin")
    private String senderVin;

    @JsonProperty("receiverVin")
    private String receiverVin;

    @JsonProperty("message")
    private String message;

    @JsonProperty("messageType")
    private MessageTypeEnum messageTypeEnum;
}
