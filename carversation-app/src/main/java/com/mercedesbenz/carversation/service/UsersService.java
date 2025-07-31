package com.mercedesbenz.carversation.service;

import com.mercedesbenz.carversation.data.NearByUsers;
import com.mercedesbenz.carversation.data.User;
import com.mercedesbenz.carversation.data.entity.UserEntity;
import com.mercedesbenz.carversation.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.mercedesbenz.carversation.service.AiNameGenerator.GetUniqueRandomName;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AiNameGenerator aiNameGenerator;

    public NearByUsers findUsersWithinRadius(double lat, double lng, String vin, double radius) {

        usersRepository.upsertCarLocation(vin, lat, lng, GetUniqueRandomName());
        List<UserEntity> entities = usersRepository.findNearbyUsers(vin, lat, lng, radius);
//        List<String> uniqueNames = aiNameGenerator.getUniqueRandomNames(entities.size());
//        List<String> uniqueNames = aiNameGenerator.getUniqueNamesFromAI();
        List<User> nearByUsers = mapToUsers(entities);
        return new NearByUsers(vin, nearByUsers);
    }

    public List<User> mapToUsers(List<UserEntity> entities) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            UserEntity entity = entities.get(i);
            User user = new User();
            user.setVin(entity.getVin());
            user.setLat(entity.getLat());
            user.setLng(entity.getLng());
            user.setName(entity.getName());
            users.add(user);
        }
        return users;
    }
}