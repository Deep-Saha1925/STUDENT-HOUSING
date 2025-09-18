package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.service.PropertyService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final PropertyService propertyService;

    public HomeController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "city", required = false) String city,
                        @RequestParam(value = "rent", required = false) String rentStr,
                        Model model){
        model.addAttribute("message", "Welcome to Student Housing Finder");
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }

        Double rent = null;
        if (rentStr != null && !rentStr.trim().isEmpty()) {
            rent = Double.parseDouble(rentStr);
        }

        List<Property> properties = propertyService.searchProperties(city, rent).stream()
                        .filter(Property::isAvailable)
                                .toList();

        model.addAttribute("properties", properties);
        model.addAttribute("city", city);
        model.addAttribute("rent", rent);
        return "index";
    }

}
