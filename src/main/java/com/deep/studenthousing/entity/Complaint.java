package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/** An issue a student raises about a property they have an approved booking for. */
@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User student;

    // Booking that makes the student eligible, and the property it is for.
    // Plain ids + title snapshot (no foreign keys), so deleting a booking/property never fails.
    private Long bookingId;
    private Long propertyId;
    private String propertyTitle;

    @Enumerated(EnumType.STRING)
    private ComplaintCategory category = ComplaintCategory.OTHER;

    @Column(nullable = false, length = 150)
    private String subject;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.NEW;

    private boolean adminUnread = true;
    private boolean studentUnread = false;

    // Set when the admin sends this issue to the owner (the id of that owner conversation).
    private Long forwardedReportId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
