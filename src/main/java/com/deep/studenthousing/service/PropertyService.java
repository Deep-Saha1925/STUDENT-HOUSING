package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.PropertyImage;
import com.deep.studenthousing.repository.PropertyImageRepository;
import com.deep.studenthousing.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ImageUploadService imageUploadService;
    private final PropertyImageRepository propertyImageRepository;
    private final GeoCodingService geoCodingService;

    public PropertyService(PropertyRepository propertyRepository, ImageUploadService imageUploadService, PropertyImageRepository propertyImageRepository, GeoCodingService geoCodingService) {
        this.propertyRepository = propertyRepository;
        this.imageUploadService = imageUploadService;
        this.propertyImageRepository = propertyImageRepository;
        this.geoCodingService = geoCodingService;
    }

//    public void save(Property property) {
//        propertyRepository.save(property);
//    }

    public void save(Property property){
        if(property.getLatitude() == null && property.getCity() != null
                && !property.getCity().isBlank()){
            double[] coords = geoCodingService.getCoordinates(property.getCity(), property.getArea());

            if(coords != null){
                property.setLatitude(coords[0]);
                property.setLongitude(coords[1]);
            }
        }

        System.out.println(property.getLatitude() + " " + property.getLongitude());

        propertyRepository.save(property);
    }

    public List<Property> findAll(){
        return propertyRepository.findAll();
    }

    public Page<Property> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return propertyRepository.findAll(pageable);
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

    public List<Property> findNearBy(double lat, double lng, double radius) {
        System.out.println("Finding nearby: lat=" + lat + " lng=" + lng + " radius=" + radius);

        // Step 1 — GPS based Haversine
        List<Property> gpsResults = propertyRepository.findNearby(lat, lng, radius);
        System.out.println("GPS results: " + gpsResults.size());

        if (!gpsResults.isEmpty()) {
            return gpsResults;
        }

        // Step 2 — Fallback: reverse geocode → text match
        System.out.println("No GPS results, falling back to text search...");
        Map<String, String> location = geoCodingService.getCityAndArea(lat, lng);
        String city = location.get("city");
        String area  = location.get("area");

        System.out.println("Reverse geocoded → city: " + city + " area: " + area);

        if (city.isBlank() && area.isBlank()) {
            return List.of();
        }

        List<Property> textResults = propertyRepository.findByCityOrArea(city, area);
        System.out.println("Text match results: " + textResults.size());
        return textResults;
    }


}