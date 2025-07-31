package com.mercedesbenz.carversation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestPayload {
    @JsonProperty("messageType")
    private MessageTypeEnum messageTypeEnum;
    @JsonProperty("requestFrom")
    private String requestFrom;
    @JsonProperty("requestTo")
    private String requestTo;
}
