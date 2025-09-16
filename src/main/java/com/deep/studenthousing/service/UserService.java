package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.exception.UserNotFoundException;
import com.deep.studenthousing.repository.PropertyRepository;
import com.deep.studenthousing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.deep.studenthousing.entity.Role.ADMIN;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository, PropertyRepository propertyRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long ownerId) {
        return userRepository.findById(ownerId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + ownerId));
    }

    public List<Property> findByOwner(User owner) {
        return propertyRepository.findByOwner(owner);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteById(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found!!"));

        String folderPath = "owner_" + user.getId();

        try{
            cloudinaryService.deleteFolder(folderPath);
        } catch (Exception e) {
            System.out.println("Failed to delete Cloudinary folder: " + e.getMessage());
        }

        userRepository.delete(user);
    }

    public User findFirstByRole() {
        return userRepository.findFirstByRole(ADMIN).get();
    }
}
