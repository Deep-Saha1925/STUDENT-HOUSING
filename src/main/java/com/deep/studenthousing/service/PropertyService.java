package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.PropertyImage;
import com.deep.studenthousing.repository.PropertyImageRepository;
import com.deep.studenthousing.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ImageUploadService imageUploadService;
    private final PropertyImageRepository propertyImageRepository;

    public PropertyService(PropertyRepository propertyRepository, ImageUploadService imageUploadService, PropertyImageRepository propertyImageRepository) {
        this.propertyRepository = propertyRepository;
        this.imageUploadService = imageUploadService;
        this.propertyImageRepository = propertyImageRepository;
    }

    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Property findById(Long propertyId) {
        return propertyRepository.findById(propertyId).get();
    }

    public List<Property> findByCityContainingIgnoreCase(String query) {
        return propertyRepository.findByCityContainingIgnoreCase(query);
    }

    public List<Property> searchProperties(String city, Double rent) {
        return propertyRepository.searchProperties(city, rent);
    }


    public void deletePropertyImage(Long imageId){
        PropertyImage propertyImage = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // 1. Delete from Cloudinary
        imageUploadService.deleteImage(propertyImage.getImageUrl());

        // 2. Delete from DB
        propertyImageRepository.delete(propertyImage);
    }

}