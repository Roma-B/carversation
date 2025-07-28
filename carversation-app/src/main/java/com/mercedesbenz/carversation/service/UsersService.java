package com.mercedesbenz.carversation.service;

import com.mercedesbenz.carversation.data.NearByUsers;
import com.mercedesbenz.carversation.data.User;
import com.mercedesbenz.carversation.data.entity.UserEntity;
import com.mercedesbenz.carversation.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public NearByUsers findUsersWithinRadius(double lat, double lng, String vin, double radius) {
        usersRepository.updateCarLocation(vin, lat, lng);
        List<User> nearByUsers = mapToUsers(usersRepository.findNearbyUsers(vin, lat, lng, radius));
        return new NearByUsers(vin, nearByUsers);
    }

    public List<User> mapToUsers(List<UserEntity> entities) {
        return entities.stream()
                .map(this::mapToUser)
                .collect(Collectors.toList());
    }

    private User mapToUser(UserEntity entity) {
        User user = new User();
        user.setVin(entity.getVin());
        user.setLat(entity.getLat());
        user.setLng(entity.getLng());
        return user;
    }
}