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

    @GetMapping("/search")
    public String searchProperties(@RequestParam(value = "query", required = false) String query,
                                   Model model) {
        List<Property> properties;
        if (query != null && !query.isEmpty()) {
            properties = propertyService.findByCityContainingIgnoreCase(query);
        } else {
            properties = propertyService.findAll();
        }
        model.addAttribute("properties", properties);
        model.addAttribute("query", query);
        return "search";
    }
}
