package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Booking;
import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.RentalType;
import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.UserRepository;
import com.deep.studenthousing.service.BookingService;
import com.deep.studenthousing.service.PropertyService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/properties")
public class BookingController {

    private final BookingService bookingService;
    private final PropertyService propertyService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, PropertyService propertyService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
        this.userRepository = userRepository;
    }

    // Owner-facing: list all bookings made on one of their properties.
    @GetMapping("/owner/{ownerId}/bookings/{propertyId}")
    public String ownerViewBookings(@PathVariable Long ownerId,
                                    @PathVariable Long propertyId,
                                    @RequestParam(value = "cancelled", required = false) String cancelled,
                                    Model model) {
        Property property = propertyService.findById(propertyId);

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: Owner mismatch!");
        }

        model.addAttribute("property", property);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("bookings", bookingService.getBookingsForProperty(propertyId));
        model.addAttribute("cancelledSuccess", "true".equals(cancelled));
        return "property-bookings";
    }

    // Returns the property's active booked date ranges as JSON, so the
    // calendar widget on property-details.html can grey them out.
    @GetMapping("/{id}/booked-dates")
    @ResponseBody
    public List<Map<String, String>> bookedDates(@PathVariable Long id) {
        return bookingService.getBookingsForProperty(id).stream()
                .filter(b -> b.getStatus() != com.deep.studenthousing.entity.BookingStatus.CANCELLED)
                .map(b -> Map.of(
                        "startDate", b.getStartDate().toString(),
                        "endDate", b.getEndDate().toString()
                ))
                .toList();
    }

    @PostMapping("/{id}/book")
    public String bookProperty(@PathVariable Long id,
                               @RequestParam("rentalType") RentalType rentalType,
                               @RequestParam("startDate") String startDateStr,
                               @RequestParam("endDate") String endDateStr,
                               Authentication authentication,
                               Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        User student = userRepository.findByEmail(authentication.getName());
        if (student == null || student.getRole() != Role.STUDENT) {
            return "redirect:/access-denied";
        }

        Property property = propertyService.findById(id);
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        try {
            bookingService.createBooking(property, student, rentalType, startDate, endDate);
            return "redirect:/properties/" + id + "?booked=success";
        } catch (IllegalStateException e) {
            return "redirect:/properties/" + id + "?booked=error&reason=" + e.getMessage();
        }
    }

    @PostMapping("/bookings/{bookingId}/approve")
    public String approveBooking(@PathVariable Long bookingId,
                                 @RequestParam("propertyId") Long propertyId,
                                 Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User owner = userRepository.findByEmail(authentication.getName());
        if (owner == null) {
            return "redirect:/access-denied";
        }
        try {
            bookingService.approveBooking(bookingId, owner.getId());
            return "redirect:/properties/owner/" + owner.getId() + "/bookings/" + propertyId + "?actionSuccess=approved";
        } catch (Exception e) {
            return "redirect:/properties/owner/" + owner.getId() + "/bookings/" + propertyId + "?actionError=" + e.getMessage();
        }
    }

    @PostMapping("/bookings/{bookingId}/reject")
    public String rejectBooking(@PathVariable Long bookingId,
                                @RequestParam("propertyId") Long propertyId,
                                Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User owner = userRepository.findByEmail(authentication.getName());
        if (owner == null) {
            return "redirect:/access-denied";
        }
        try {
            bookingService.rejectBooking(bookingId, owner.getId());
            return "redirect:/properties/owner/" + owner.getId() + "/bookings/" + propertyId + "?actionSuccess=rejected";
        } catch (Exception e) {
            return "redirect:/properties/owner/" + owner.getId() + "/bookings/" + propertyId + "?actionError=" + e.getMessage();
        }
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId,
                                @RequestParam("propertyId") Long propertyId,
                                Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User requester = userRepository.findByEmail(authentication.getName());
        if (requester == null) {
            return "redirect:/access-denied";
        }

        bookingService.cancelBooking(bookingId, requester.getId());

        boolean isOwner = requester.getRole() == Role.OWNER;
        if (isOwner) {
            return "redirect:/properties/owner/" + requester.getId() + "/bookings/" + propertyId + "?cancelled=true";
        }
        return "redirect:/properties/" + propertyId + "?cancelled=true";
    }

    // Student-facing: "My Bookings" — every booking the student has made, across
    // both rental types, so they can track pending/confirmed/cancelled status.
    @GetMapping("/my-bookings")
    public String myBookings(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User student = userRepository.findByEmail(authentication.getName());
        if (student == null || student.getRole() != Role.STUDENT) {
            return "redirect:/access-denied";
        }
        List<Booking> bookings = bookingService.getBookingsForStudent(student);
        model.addAttribute("bookings", bookings);
        model.addAttribute("dailyBookings", bookings.stream().filter(b -> b.getRentalType() == RentalType.DAILY).toList());
        model.addAttribute("monthlyBookings", bookings.stream().filter(b -> b.getRentalType() == RentalType.MONTHLY).toList());
        return "my-bookings";
    }

    @GetMapping("/owner/{ownerId}/bookings/{propertyId}")
    public String ownerViewBookings(@PathVariable Long ownerId,
                                    @PathVariable Long propertyId,
                                    @RequestParam(value = "cancelled", required = false) String cancelled,
                                    @RequestParam(value = "actionSuccess", required = false) String actionSuccess,
                                    @RequestParam(value = "actionError", required = false) String actionError,
                                    Model model) {
        Property property = propertyService.findById(propertyId);

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: Owner mismatch!");
        }

        model.addAttribute("property", property);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("bookings", bookingService.getBookingsForProperty(propertyId));
        model.addAttribute("cancelledSuccess", "true".equals(cancelled));
        model.addAttribute("actionSuccess", actionSuccess);
        model.addAttribute("actionError", actionError);
        return "property-bookings";
    }
}