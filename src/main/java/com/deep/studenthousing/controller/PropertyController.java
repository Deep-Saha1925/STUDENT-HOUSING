package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final UserService userService;
    private final PropertyService propertyService;

    public PropertyController(UserService userService, PropertyService propertyService) {
        this.userService = userService;
        this.propertyService = propertyService;
    }

    // Show all properties for a specific owner
    @GetMapping("/owner/{ownerId}")
    public String listOwnerProperties(@PathVariable Long ownerId, Model model) {
        User owner = userService.findById(ownerId);

        if(owner == null){
            return "error-page";
        }

        if(owner.getRole().equals(Role.STUDENT)){
            return "error-page";
        }

        List<Property> properties = userService.findByOwner(owner);
        model.addAttribute("properties", properties);
        model.addAttribute("ownerId", ownerId);
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
                               HttpServletRequest request) {
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
                                 @ModelAttribute("property") Property updatedProperty) {
        Property property = propertyService.findById(propertyId);

        if (property.getOwner().getRole() != Role.OWNER) {
            throw new RuntimeException("Unauthorized: Only Owners can update properties.");
        }

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: Owner mismatch!");
        }

        property.setTitle(updatedProperty.getTitle());
        property.setDescription(updatedProperty.getDescription());
        property.setCity(updatedProperty.getCity());
        property.setArea(updatedProperty.getArea());
        property.setRent(updatedProperty.getRent());

        propertyService.save(property);

        return "redirect:/properties/owner/" + ownerId;
    }
}
