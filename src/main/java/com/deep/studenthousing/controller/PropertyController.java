package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.BookingService;
import com.deep.studenthousing.service.ImageUploadService;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private AccessGuard guard;

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
        // Mass-assignment protection: a browser can send any field name. These
        // must only ever be set by the server (otherwise someone could post
        // "id=<another listing>" to overwrite it, or "owner.id=..." to steal one).
        binder.setDisallowedFields("id", "owner", "owner.*", "imageUrls", "available");
    }

    @GetMapping("/nearby")
    public String nearByProperties(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius,
            @RequestParam(value = "rentalType", required = false) String rentalType,
            @RequestParam(value = "rent", required = false) String rentStr,
            @RequestParam(value = "gender", required = false) String gender,
            Model model,
            org.springframework.security.core.Authentication authentication
    ){
        // Location search is a logged-in-only feature. SecurityConfig permits
        // /properties/** broadly (property detail pages are public), so the
        // gate has to live here rather than in the filter chain.
        if (authentication == null) {
            model.addAttribute("properties", List.of());
            model.addAttribute("loginRequired", true);
            return "fragments/property-list :: propertyList";
        }
        try {
            List<Property> nearby = propertyService.findNearBy(lat, lng, radius,
                    cleanRentalType(rentalType), parseRent(rentStr), cleanGender(gender));
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

    // Blank / unknown filter values mean "no filter" for the nearby search.
    private String cleanRentalType(String rentalType) {
        if (rentalType == null) return null;
        String t = rentalType.trim().toUpperCase();
        return (t.equals("MONTHLY") || t.equals("DAILY")) ? t : null;
    }

    private String cleanGender(String gender) {
        if (gender == null) return null;
        String g = gender.trim().toUpperCase();
        return (g.equals("MALE") || g.equals("FEMALE") || g.equals("FAMILY")) ? g : null;
    }

    private Double parseRent(String rentStr) {
        if (rentStr == null || rentStr.isBlank()) return null;
        try { return Double.parseDouble(rentStr.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    // JSON endpoint for map pins — separate from /nearby (which returns an HTML fragment)
    @GetMapping("/nearby/map")
    @ResponseBody
    public List<PropertyMapDTO> nearbyPropertiesForMap(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius,
            @RequestParam(value = "rentalType", required = false) String rentalType,
            @RequestParam(value = "rent", required = false) String rentStr,
            @RequestParam(value = "gender", required = false) String gender,
            org.springframework.security.core.Authentication authentication
    ) {
        // Same gate as /nearby above — don't leak property coordinates to guests.
        if (authentication == null) {
            return List.of();
        }
        try {
            List<Property> nearby = propertyService.findNearBy(lat, lng, radius,
                    cleanRentalType(rentalType), parseRent(rentStr), cleanGender(gender));
            return nearby.stream()
                    .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                    .map(p -> new PropertyMapDTO(
                            p.getId(), p.getTitle(), p.getArea(), p.getCity(),
                            p.getLatitude(), p.getLongitude(), p.getMonthlyRent(),
                            p.getDailyRent(), p.isAvailableMonthly(), p.isAvailableDaily(),
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
    public String listOwnerProperties(@PathVariable Long ownerId, Model model, Authentication authentication) {
        guard.requireSelfOwner(ownerId, authentication);
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
    public String addPropertyForm(@PathVariable Long ownerId, Model model, Authentication authentication) {
        guard.requireSelfOwner(ownerId, authentication);
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
                               HttpServletRequest request,
                               Authentication authentication)  throws IOException {
        User owner = guard.requireSelfOwner(ownerId, authentication);

        //Only Owners can add property
        if (owner.getRole() != Role.OWNER) {
            model.addAttribute("errorMessage", "Only Owners can add properties!");

            String referer = request.getHeader("Referer");
            model.addAttribute("backUrl", (referer != null) ? referer : "/users");
            return "error-page";
        }

        property.setId(null);          // always a brand-new row, never an overwrite
        property.setOwner(owner);
        sanitizeAndNormalize(property);
        // Validate the images before anything is stored, so a bad file doesn't leave a half-created listing.
        imageUploadService.validate(images);
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
                                   Model model,
                                   Authentication authentication) {
        Property property = requireOwnedProperty(ownerId, propertyId, authentication);

        model.addAttribute("property", property);
        model.addAttribute("ownerId", ownerId);
        return "edit-property";
    }

    // POST only: a GET link can be triggered by any other website (<img src=...>) and
    // CSRF protection does not cover GET. Ownership is checked against the logged-in user.
    @PostMapping("/owner/{ownerId}/delete/{propertyId}")
    public String deleteProperty(
            @PathVariable Long ownerId,
            @PathVariable Long propertyId,
            Authentication authentication
    ){
        requireOwnedProperty(ownerId, propertyId, authentication);
        propertyService.deleteById(propertyId);

        return "redirect:/properties/owner/" + ownerId;
    }

    @PostMapping("/owner/{ownerId}/edit/{propertyId}")
    public String updateProperty(@PathVariable Long ownerId,
                                 @PathVariable Long propertyId,
                                 @ModelAttribute("property") Property updatedProperty,
                                 @RequestParam(value = "images", required = false) MultipartFile[] images,
                                 Authentication authentication) throws IOException {
        Property property = requireOwnedProperty(ownerId, propertyId, authentication);

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
        sanitizeAndNormalize(property);

        // Handle new image uploads
        if (images != null && images.length > 0 && !images[0].isEmpty()) {
            imageUploadService.validate(images);
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
                              @RequestParam("imageUrl") String imageUrl,
                              Authentication authentication){
        Property property = requireOwnedProperty(ownerId, propertyId, authentication);

        // Only touch Cloudinary for an image that really belongs to this listing;
        // otherwise a caller could make us delete arbitrary files by guessing URLs.
        boolean removed = property.getImageUrls() != null && property.getImageUrls().remove(imageUrl);
        if (removed) {
            propertyService.save(property);
            imageUploadService.deleteImage(imageUrl);
        }

        return "redirect:/properties/owner/" + ownerId + "/edit/" + propertyId;
    }


    // Server-side safety net (client JS already does this, but never trust the client alone):
    // an unavailable rental type should never carry a rent value, and a listing
    // needs to offer at least one type or it isn't bookable at all.
    // Server-side validation of the free-form fields (the HTML form limits are only a hint).
    private void sanitizeAndNormalize(Property property) {
        property.setTitle(requireText(property.getTitle(), "Title", 150));
        property.setCity(requireText(property.getCity(), "City", 100));
        String area = property.getArea() == null ? "" : property.getArea().trim();
        if (area.length() > 150) {
            throw new IllegalArgumentException("Area is too long (maximum 150 characters).");
        }
        property.setArea(area);
        String description = property.getDescription();
        if (description != null) {
            description = description.trim();
            if (description.length() > 2000) {
                throw new IllegalArgumentException("Description is too long (maximum 2000 characters).");
            }
            property.setDescription(description);
        }
        if (property.isAvailableMonthly() && (property.getMonthlyRent() <= 0 || property.getMonthlyRent() > 10_000_000)) {
            throw new IllegalArgumentException("Monthly rent must be a positive amount.");
        }
        if (property.isAvailableDaily()
                && (property.getDailyRent() == null || property.getDailyRent() <= 0 || property.getDailyRent() > 10_000_000)) {
            throw new IllegalArgumentException("Daily rent must be a positive amount.");
        }
        // Coordinates must be real; drop them (so they get geocoded from City/Area) if not.
        Double lat = property.getLatitude();
        Double lng = property.getLongitude();
        if (lat == null || lng == null || lat.isNaN() || lng.isNaN()
                || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            property.setLatitude(null);
            property.setLongitude(null);
        }
        normalizeRentalFields(property);
    }

    private String requireText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(label + " is too long (maximum " + maxLength + " characters).");
        }
        return trimmed;
    }

    // The logged-in user must be the owner in the URL AND the listing must really be theirs.
    private Property requireOwnedProperty(Long ownerId, Long propertyId, Authentication authentication) {
        guard.requireSelfOwner(ownerId, authentication);
        Property property = propertyService.findById(propertyId);
        if (property.getOwner() == null || !property.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedActionException("This property belongs to a different owner account.");
        }
        return property;
    }

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
                                     @RequestParam(value = "available", required = false) String available,
                                     Authentication authentication) {
        Property property = requireOwnedProperty(ownerId, propertyId, authentication);
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