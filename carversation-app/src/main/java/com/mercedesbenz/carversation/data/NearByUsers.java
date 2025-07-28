package com.mercedesbenz.carversation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearByUsers {
    private String myName;
    private List<User> nearByUsers;
}