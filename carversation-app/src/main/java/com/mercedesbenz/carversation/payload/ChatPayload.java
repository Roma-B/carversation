package com.mercedesbenz.carversation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPayload {

    @JsonProperty("vin")
    private String vin;

    @JsonProperty("message")
    private String message;

    @JsonProperty("messageType")
    private MessageTypeEnum messageType;

    @JsonProperty("status")
    private String status;
}
