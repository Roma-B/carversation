package com.mercedesbenz.carversation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ChatPayload  {
    @JsonProperty("vin")
    private String vin;
    @JsonProperty("message")
    private String message;
/*
    public ChatPayload(String vin, String message) {
        this.vin = vin;
        this.message = message;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }*/
}
