package com.mercedesbenz.carversation.controller;

import com.mercedesbenz.carversation.data.NearByUsers;
import com.mercedesbenz.carversation.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class UserController {

    @Autowired
    private UsersService usersService;

    @PostMapping(value = "/users/nearby", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NearByUsers> getNearbyCars(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam String vin,
            @RequestParam double radius) {
        return ok(usersService.findUsersWithinRadius(lat, lng, vin, radius));
    }

    @GetMapping("/users")
    public String getUserDetails() {
        return "User details";
    }
}