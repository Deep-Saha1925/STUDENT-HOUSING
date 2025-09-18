package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.service.PropertyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final PropertyService propertyService;

    public SearchController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/student-search")
    public String searchPropertiesByStudent(@RequestParam(value = "city", required = false) String city,
                                   @RequestParam(value = "rent", required = false) String rentStr,
                                   Model model) {
        // Clean params
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }

        Double rent = null;
        if (rentStr != null && !rentStr.trim().isEmpty()) {
            rent = Double.parseDouble(rentStr);
        }

        // Call service
        List<Property> properties = propertyService.searchProperties(city, rent)
                .stream()
                .filter(Property::isAvailable)
                .toList();

        model.addAttribute("properties", properties);
        return "search";
    }

    @GetMapping("/search")
    public String searchProperties(@RequestParam(value = "city", required = false) String city,
                                   @RequestParam(value = "rent", required = false) String rentStr,
                                   Model model) {
        // Clean params
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }

        Double rent = null;
        if (rentStr != null && !rentStr.trim().isEmpty()) {
            rent = Double.parseDouble(rentStr);
        }

        // Call service
        List<Property> properties = propertyService.searchProperties(city, rent)
                                                    .stream()
                                                    .filter(Property::isAvailable)
                                                    .toList();

        model.addAttribute("properties", properties);
        return "fragments/property-list :: propertyList";
    }

}
