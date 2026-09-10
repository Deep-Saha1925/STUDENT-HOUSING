package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.service.PropertyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    // Key under which the student's last-picked gender filter is remembered
    // for the session, so property-details can hard-block bookings that
    // don't match without ever storing gender on the student's profile.
    public static final String SESSION_GENDER_KEY = "genderPreference";

    private final PropertyService propertyService;

    public SearchController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/student-search")
    public String searchPropertiesByStudent(@RequestParam(value = "city", required = false) String city,
                                            @RequestParam(value = "rent", required = false) String rentStr,
                                            @RequestParam(value = "rentalType", required = false) String rentalType,
                                            @RequestParam(value = "gender", required = false) String gender,
                                            Model model,
                                            HttpSession session) {
        // Clean params
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }
        if (rentalType != null && rentalType.trim().isEmpty()) {
            rentalType = null;
        }
        gender = normalizeGender(gender, session);

        Double rent = null;
        if (rentStr != null && !rentStr.trim().isEmpty()) {
            rent = Double.parseDouble(rentStr);
        }

        // Call service
        List<Property> properties = propertyService.searchProperties(city, rent, rentalType, gender)
                .stream()
                .filter(Property::isAvailable)
                .toList();

        model.addAttribute("properties", properties);
        model.addAttribute("city", city);
        model.addAttribute("rent", rent);
        model.addAttribute("rentalType", rentalType);
        model.addAttribute("gender", gender);
        return "search";
    }

    @GetMapping("/search")
    public String searchProperties(@RequestParam(value = "city", required = false) String city,
                                   @RequestParam(value = "rent", required = false) String rentStr,
                                   @RequestParam(value = "rentalType", required = false) String rentalType,
                                   @RequestParam(value = "gender", required = false) String gender,
                                   Model model,
                                   HttpSession session) {
        // Clean params
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }
        if (rentalType != null && rentalType.trim().isEmpty()) {
            rentalType = null;
        }
        gender = normalizeGender(gender, session);

        Double rent = null;
        if (rentStr != null && !rentStr.trim().isEmpty()) {
            rent = Double.parseDouble(rentStr);
        }

        // Calling service
        List<Property> properties = propertyService.searchProperties(city, rent, rentalType, gender)
                .stream()
                .filter(Property::isAvailable)
                .toList();

        model.addAttribute("properties", properties);
        return "fragments/property-list :: propertyList";
    }

    // Distinguishes "no gender param at all" (e.g. the bare /student-search
    // link from the dashboard — a fresh page visit, not a filter choice) from
    // "gender param explicitly sent as empty" (the search form's JS always
    // sends gender=, even for 'Any rental type', because that's a real choice).
    //
    // - param absent      -> leave the session alone, reuse whatever was remembered
    // - param = ""        -> explicit "Any" -> clear the remembered preference
    // - param = a value   -> remember it for the session (reused later on
    //                        property-details for the booking hard-block)
    private String normalizeGender(String gender, HttpSession session) {
        if (gender == null) {
            return (String) session.getAttribute(SESSION_GENDER_KEY);
        }
        gender = gender.trim().toUpperCase();
        if (gender.isEmpty()) {
            session.removeAttribute(SESSION_GENDER_KEY);
            return null;
        }
        session.setAttribute(SESSION_GENDER_KEY, gender);
        return gender;
    }

}