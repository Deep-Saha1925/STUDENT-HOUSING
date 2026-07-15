package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Booking;
import com.deep.studenthousing.entity.BookingStatus;
import com.deep.studenthousing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPropertyIdOrderByStartDateAsc(Long propertyId);

    List<Booking> findByStudentOrderByStartDateDesc(User student);

    // All bookings with a given status across every property owned by this owner —
    // one query, used to build a per-property pending-request indicator on the
    // owner's My Properties dashboard instead of querying per-row.
    List<Booking> findByProperty_Owner_IdAndStatus(Long ownerId, BookingStatus status);

    /**
     * Any active (not cancelled) booking on this property whose date range
     * overlaps [startDate, endDate]. A property is occupied by at most one
     * booking (monthly or daily) at a time, so this single check covers both types.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.property.id = :propertyId
          AND b.status <> com.deep.studenthousing.entity.BookingStatus.CANCELLED
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
        """)
    List<Booking> findOverlappingBookings(@Param("propertyId") Long propertyId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}