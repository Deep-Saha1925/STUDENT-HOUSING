package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import com.deep.studenthousing.service.BookingService;
import com.deep.studenthousing.service.ImageUploadService;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.deep.studenthousing.dto.PropertyMapDTO;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final ImageUploadService imageUploadService;
    private final BookingService bookingService;

    public PropertyController(UserService userService, PropertyService propertyService, ImageUploadService imageUploadService, BookingService bookingService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.imageUploadService = imageUploadService;
        this.bookingService = bookingService;
    }

    // The hidden latitude/longitude fields on add/edit-property.html are only
    // populated when the owner clicks "Use My Current Location" — otherwise
    // they're submitted as an empty string. Property.latitude/longitude are
    // Double (nullable), and Spring's default binder rejects "" for a Double
    // with a conversion error. allowEmpty=true tells it to treat "" as null
    // instead, so the form works whether or not the owner used the button.
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
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

    // JSON endpoint for map pins — separate from /nearby (which returns an HTML fragment)
    @GetMapping("/nearby/map")
    @ResponseBody
    public List<PropertyMapDTO> nearbyPropertiesForMap(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius
    ) {
        try {
            List<Property> nearby = propertyService.findNearBy(lat, lng, radius);
            return nearby.stream()
                    .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                    .map(p -> new PropertyMapDTO(
                            p.getId(), p.getTitle(), p.getArea(), p.getCity(),
                            p.getLatitude(), p.getLongitude(), p.getMonthlyRent(),
                            p.isAvailable(),
                            (p.getImageUrls() != null && !p.getImageUrls().isEmpty())
                                    ? p.getImageUrls().get(0) : null
                    ))
                    .toList();
        } catch (Exception e) {
            System.err.println("nearby/map failed for lat=" + lat + " lng=" + lng + " radius=" + radius);
            e.printStackTrace();
            return List.of();
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
        model.addAttribute("pendingCounts", bookingService.getPendingBookingCountsByOwner(ownerId));

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
        normalizeRentalFields(property);
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
            throw new UnauthorizedActionException("This property belongs to a different owner account.");
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
            throw new UnauthorizedActionException("Only owner accounts can update properties.");
        }

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedActionException("This property belongs to a different owner account.");
        }

        // Update fields
        property.setTitle(updatedProperty.getTitle());
        property.setDescription(updatedProperty.getDescription());
        property.setCity(updatedProperty.getCity());
        property.setArea(updatedProperty.getArea());
        property.setMonthlyRent(updatedProperty.getMonthlyRent());
        property.setDailyRent(updatedProperty.getDailyRent());
        property.setAvailableMonthly(updatedProperty.isAvailableMonthly());
        property.setAvailableDaily(updatedProperty.isAvailableDaily());
        property.setAllowedForMale(updatedProperty.isAllowedForMale());
        property.setAllowedForFemale(updatedProperty.isAllowedForFemale());
        property.setAllowedForFamily(updatedProperty.isAllowedForFamily());
        // Hidden fields always carry through the correct value — either the
        // freshly-captured GPS coords (if the owner clicked "Use My Location")
        // or the previously-saved ones (pre-filled by th:field), so this is
        // safe to copy unconditionally. Without this line, edits could never
        // update — or even preserve — a property's coordinates.
        property.setLatitude(updatedProperty.getLatitude());
        property.setLongitude(updatedProperty.getLongitude());
        normalizeRentalFields(property);

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


    // Server-side safety net (client JS already does this, but never trust the client alone):
    // an unavailable rental type should never carry a rent value, and a listing
    // needs to offer at least one type or it isn't bookable at all.
    private void normalizeRentalFields(Property property) {
        if (!property.isAvailableMonthly()) {
            property.setMonthlyRent(0);
        }
        if (!property.isAvailableDaily()) {
            property.setDailyRent(null);
        }
        if (!property.isAvailableMonthly() && !property.isAvailableDaily()) {
            // Fall back to monthly rather than silently saving an unbookable property.
            property.setAvailableMonthly(true);
        }
        if (!property.isAllowedForMale() && !property.isAllowedForFemale() && !property.isAllowedForFamily()) {
            // Same idea: an owner who unchecks all three didn't mean "no one can book this" —
            // treat it as "open to all" rather than silently locking the listing.
            property.setAllowedForMale(true);
            property.setAllowedForFemale(true);
            property.setAllowedForFamily(true);
        }
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
    @GetMapping("/{id}")
    public String viewProperty(@PathVariable Long id, Model model,
                               org.springframework.security.core.Authentication authentication,
                               @RequestParam(value = "booked", required = false) String booked,
                               @RequestParam(value = "reason", required = false) String reason,
                               @RequestParam(value = "cancelled", required = false) String cancelled,
                               HttpSession session){
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

        // Gender hard-block: reuse whatever gender the student last picked in
        // the search filter (stored in session) — nothing is stored on the
        // student's profile. If the listing has no gender restriction at all,
        // it's always bookable regardless of session state.
        String sessionGender = (String) session.getAttribute(SearchController.SESSION_GENDER_KEY);
        boolean genderMatches = property.isAllowedForGender(sessionGender);
        boolean genderBlocked = isStudentViewer && !isOwnProperty && !property.isOpenToAllGenders() && !genderMatches;

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("isStudentViewer", isStudentViewer);
        model.addAttribute("isOwnProperty", isOwnProperty);
        model.addAttribute("sessionGender", sessionGender);
        model.addAttribute("genderBlocked", genderBlocked);
        model.addAttribute("canBook", isStudentViewer && !isOwnProperty && !genderBlocked);

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