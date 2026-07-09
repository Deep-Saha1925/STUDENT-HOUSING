package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.Booking;
import com.deep.studenthousing.entity.BookingStatus;
import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.RentalType;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getBookingsForProperty(Long propertyId) {
        return bookingRepository.findByPropertyIdOrderByStartDateAsc(propertyId);
    }

    public List<Booking> getBookingsForStudent(User student) {
        return bookingRepository.findByStudentOrderByStartDateDesc(student);
    }

    public boolean isAvailable(Long propertyId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return false;
        }
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(propertyId, startDate, endDate);
        return overlaps.isEmpty();
    }

    public Booking createBooking(Property property, User student, RentalType rentalType,
                                 LocalDate startDate, LocalDate endDate) {
        if (rentalType == RentalType.MONTHLY && !property.isAvailableMonthly()) {
            throw new IllegalStateException("This property is not available for monthly rental.");
        }
        if (rentalType == RentalType.DAILY && !property.isAvailableDaily()) {
            throw new IllegalStateException("This property is not available for daily rental.");
        }
        if (!isAvailable(property.getId(), startDate, endDate)) {
            throw new IllegalStateException("Selected dates are not available for this property.");
        }

        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setStudent(student);
        booking.setRentalType(rentalType);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        // Requires the owner to approve before it's confirmed. It still blocks
        // the calendar for other students in the meantime (see findOverlappingBookings),
        // so two people can't be approved into the same dates.
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    public void approveBooking(Long bookingId, Long ownerId) {
        Booking booking = getOwnedBooking(bookingId, ownerId);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    public void rejectBooking(Long bookingId, Long ownerId) {
        Booking booking = getOwnedBooking(bookingId, ownerId);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private Booking getOwnedBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!booking.getProperty().getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Not authorized to manage this booking.");
        }
        return booking;
    }

    public void cancelBooking(Long bookingId, Long requesterId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        boolean isStudent = booking.getStudent().getId().equals(requesterId);
        boolean isOwner = booking.getProperty().getOwner().getId().equals(requesterId);

        if (!isStudent && !isOwner) {
            throw new IllegalStateException("Not authorized to cancel this booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}