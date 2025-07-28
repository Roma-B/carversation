package com.mercedesbenz.carversation.data;

import lombok.Data;

@Data
public class User {
    private String vin;
    private String name;
    private double lat;
    private double lng;
}