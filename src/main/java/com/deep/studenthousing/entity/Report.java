package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    @OnDelete(action = OnDeleteAction.CASCADE)   // deleting a user also removes their reports
    private User owner;

    @Enumerated(EnumType.STRING)
    private ReportType type = ReportType.OTHER;

    @Column(nullable = false, length = 150)
    private String subject;

    // Optional link to one of the owner's properties. Stored as a plain id + title snapshot
    // (not a foreign key) so deleting a property never fails because of an old report.
    private Long propertyId;
    private String propertyTitle;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.NEW;

    // "Unread" flags drive the highlight/badge for each side.
    private boolean adminUnread = true;
    private boolean ownerUnread = false;

    // Set later (step 2b) when an approved report has been turned into a public notice.
    private boolean noticePublished = false;

    // True when the admin started the conversation (a message sent to an owner),
    // as opposed to an owner sending a report. Existing rows default to false.
    @Column(columnDefinition = "boolean not null default false")
    private boolean startedByAdmin = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Status values are shared (no new enum values), but admin-started messages read differently.
    public static String labelFor(ReportStatus s, boolean startedByAdmin) {
        if (startedByAdmin) {
            switch (s) {
                case NEED_INFO:    return "Awaiting owner reply";
                case UNDER_REVIEW: return "Owner replied";
                case APPROVED:     return "Resolved";
                default: break;
            }
        }
        return s.getLabel();
    }

    public String getStatusLabel() { return labelFor(status, startedByAdmin); }

    public String labelOf(ReportStatus s) { return labelFor(s, startedByAdmin); }
}
