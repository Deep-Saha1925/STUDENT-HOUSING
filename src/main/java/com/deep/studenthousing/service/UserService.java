package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.PropertyRepository;
import com.deep.studenthousing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public UserService(UserRepository userRepository, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long ownerId) {
        return userRepository.findById(ownerId).orElseThrow();
    }

    public List<Property> findByOwner(User owner) {
        return propertyRepository.findByOwner(owner);
    }
}
