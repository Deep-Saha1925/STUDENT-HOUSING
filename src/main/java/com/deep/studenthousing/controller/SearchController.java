package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.service.PropertyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
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

    // How many properties an anonymous (not logged in) visitor gets to see
    // on the public homepage before being asked to log in for the rest.
    public static final int GUEST_PREVIEW_LIMIT = 6;

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

        // /student-search is already login-only (see SecurityConfig), so it's
        // never guest-limited — set explicitly so the shared fragment has it.
        model.addAttribute("guestLimited", false);
        model.addAttribute("hiddenCount", 0);

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
                                   HttpSession session,
                                   Authentication authentication) {
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

        // This endpoint is public (SecurityConfig permits /search to anyone) and is
        // hit both by the guest homepage and by the logged-in student's search page —
        // only cap results when there's no logged-in user behind the request.
        boolean guestLimited = authentication == null && properties.size() > GUEST_PREVIEW_LIMIT;
        model.addAttribute("guestLimited", guestLimited);
        model.addAttribute("hiddenCount", guestLimited ? properties.size() - GUEST_PREVIEW_LIMIT : 0);
        if (guestLimited) {
            properties = properties.stream().limit(GUEST_PREVIEW_LIMIT).toList();
        }

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