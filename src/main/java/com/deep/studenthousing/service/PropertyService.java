package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
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
}