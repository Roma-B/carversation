package com.mercedesbenz.carversation.payload;

public enum MessageTypeEnum {
    CHAT_REQUEST("CHAT_REQUEST"),
    CHAT_REQUEST_RESPONSE("CHAT_REQUEST_RESPONSE"),
    CHAT_MESSAGE("CHAT_MESSAGE");

    private final String value;

    MessageTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Optional: for reverse lookup from string
    public static MessageTypeEnum fromValue(String value) {
        for (MessageTypeEnum type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
