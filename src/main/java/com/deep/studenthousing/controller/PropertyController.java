package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.ImageUploadService;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final ImageUploadService imageUploadService;

    public PropertyController(UserService userService, PropertyService propertyService, ImageUploadService imageUploadService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping("/nearby")
    public String nearByProperties(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius,
            Model model
    ){
        try {
            List<Property> nearby = propertyService.findNearBy(lat, lng, radius);
            model.addAttribute("properties", nearby);
            model.addAttribute("nearbyMode", true);
            model.addAttribute("searchedRadius", radius);
            return "fragments/property-list :: propertyList";
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("properties", List.of());
            return "fragments/property-list :: propertyList";
        }
    }

    // Show all properties for a specific owner
    @GetMapping("/owner/{ownerId}")
    public String listOwnerProperties(@PathVariable Long ownerId, Model model) {
        User owner = userService.findById(ownerId);

        if (owner == null) {
            return "access-denied";
        }

        if (owner.getRole().equals(Role.STUDENT)) {
            return "error-page";
        }

        List<Property> properties = userService.findByOwner(owner);

        // Add owner, ownerId, and properties to model
        model.addAttribute("owner", owner);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("properties", properties);

        return "owner-properties";
    }

    // Show form to add property for specific owner
    @GetMapping("/owner/{ownerId}/add")
    public String addPropertyForm(@PathVariable Long ownerId, Model model) {
        model.addAttribute("property", new Property());
        model.addAttribute("ownerId", ownerId);
        return "add-property";
    }

    // Save property for specific owner
    @PostMapping("/owner/{ownerId}/add")
    public String saveProperty(@PathVariable Long ownerId,
                               @ModelAttribute("property") Property property,
                               Model model,
                               @RequestParam("images") MultipartFile[] images,
                               HttpServletRequest request)  throws IOException {
        User owner = userService.findById(ownerId);

        //Only Owners can add property
        if (owner.getRole() != Role.OWNER) {
            model.addAttribute("errorMessage", "Only Owners can add properties!");

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", (referer != null) ? referer : "/users");
            return "error-page";
        }

        property.setOwner(owner);
        propertyService.save(property);

        if(images != null && images.length > 0){
            List<String> imageUrls = imageUploadService.uploadMultipleImages(images, ownerId, property.getId());
            property.setImageUrls(imageUrls);

            propertyService.save(property);
        }

        return "redirect:/properties/owner/" + ownerId;
    }

    // Update property (only by the owner who owns it)
    @GetMapping("/owner/{ownerId}/edit/{propertyId}")
    public String editPropertyForm(@PathVariable Long ownerId,
                                   @PathVariable Long propertyId,
                                   Model model) {
        Property property = propertyService.findById(propertyId);

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: Owner mismatch!");
        }

        model.addAttribute("property", property);
        model.addAttribute("ownerId", ownerId);
        return "edit-property";
    }

    @PostMapping("/owner/{ownerId}/edit/{propertyId}")
    public String updateProperty(@PathVariable Long ownerId,
                                 @PathVariable Long propertyId,
                                 @ModelAttribute("property") Property updatedProperty,
                                 @RequestParam(value = "images", required = false) MultipartFile[] images) throws IOException {
        Property property = propertyService.findById(propertyId);

        if (property.getOwner().getRole() != Role.OWNER) {
            throw new RuntimeException("Unauthorized: Only Owners can update properties.");
        }

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: Owner mismatch!");
        }

        // Update fields
        property.setTitle(updatedProperty.getTitle());
        property.setDescription(updatedProperty.getDescription());
        property.setCity(updatedProperty.getCity());
        property.setArea(updatedProperty.getArea());
        //property.setRent(updatedProperty.getRent());

        property.setMonthlyRent(updatedProperty.getMonthlyRent());
        property.setDailyRent(updatedProperty.getDailyRent());
        property.setAvailableMonthly(updatedProperty.isAvailableMonthly());
        property.setAvailableDaily(updatedProperty.isAvailableDaily());

        // Handle new image uploads
        if (images != null && images.length > 0 && !images[0].isEmpty()) {
            List<String> imageUrls = imageUploadService.uploadMultipleImages(images, ownerId, propertyId);

            // Append new images to existing ones
            if (property.getImageUrls() != null) {
                property.getImageUrls().addAll(imageUrls);
            } else {
                property.setImageUrls(imageUrls);
            }
        }

        propertyService.save(property);

        return "redirect:/properties/owner/" + ownerId;
    }

    @PostMapping("/owner/{ownerId}/edit/{propertyId}/delete-image")
    public String deleteImage(@PathVariable Long ownerId,
                              @PathVariable Long propertyId,
                              @RequestParam("imageUrl") String imageUrl){
        Property property = propertyService.findById(propertyId);

        if (property.getOwner().getId().equals(ownerId)) {
            // Remove image from property
            property.getImageUrls().remove(imageUrl);
            propertyService.save(property);

            // (Optional) Delete from Cloudinary
            imageUploadService.deleteImage(imageUrl);
        }

        return "redirect:/properties/owner/" + ownerId + "/edit/" + propertyId;
    }


    @PostMapping("/owner/{ownerId}/availability/{propertyId}")
    public String toggleAvailability(@PathVariable Long ownerId,
                                     @PathVariable Long propertyId,
                                     @RequestParam(value = "available", required = false) String available) {
        Property property = propertyService.findById(propertyId);
        property.setAvailable(available != null); // Checkbox checked = true, else false
        propertyService.save(property);
        return "redirect:/properties/owner/" + ownerId;
    }

    //view property
    //view property
    @GetMapping("/{id}")
    public String viewProperty(@PathVariable Long id, Model model,
                               org.springframework.security.core.Authentication authentication,
                               @RequestParam(value = "booked", required = false) String booked,
                               @RequestParam(value = "reason", required = false) String reason,
                               @RequestParam(value = "cancelled", required = false) String cancelled){
        Property property = propertyService.findById(id);
        model.addAttribute("property", property);

        boolean loggedIn = authentication != null;
        boolean isStudentViewer = false;
        boolean isOwnProperty = false;
        User currentUser = null;

        if (loggedIn) {
            currentUser = userService.findByEmail(authentication.getName());
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
                isStudentViewer = currentUser.getRole() == Role.STUDENT;
                isOwnProperty = property.getOwner() != null && property.getOwner().getId().equals(currentUser.getId());
            }
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("isStudentViewer", isStudentViewer);
        model.addAttribute("isOwnProperty", isOwnProperty);
        model.addAttribute("canBook", isStudentViewer && !isOwnProperty);

        if ("success".equals(booked)) {
            model.addAttribute("bookedSuccess", true);
        } else if ("error".equals(booked)) {
            model.addAttribute("bookedError", true);
            model.addAttribute("bookedErrorReason", reason);
        }
        if ("true".equals(cancelled)) {
            model.addAttribute("cancelledSuccess", true);
        }

        return "property-details";
    }
}
