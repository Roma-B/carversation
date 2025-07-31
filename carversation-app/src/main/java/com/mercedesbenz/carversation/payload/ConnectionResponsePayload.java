package com.mercedesbenz.carversation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercedesbenz.carversation.data.NearByUsers;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
@Setter
public class ConnectionResponsePayload {
    @JsonProperty("messageType")
    private MessageTypeEnum messageType;
    @JsonProperty("nearByUsers")
    private NearByUsers nearByUsers;
}

