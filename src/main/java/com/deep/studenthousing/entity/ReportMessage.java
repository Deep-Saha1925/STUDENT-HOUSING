package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/** One entry in a report's conversation (the owner's first message is the report description). */
@Entity
@Table(name = "report_messages")
@Getter
@Setter
@NoArgsConstructor
public class ReportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Report report;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    private boolean fromAdmin;

    @Column(nullable = false, length = 2000)
    private String message = "";

    // Set when this entry also changed the report's status (shown as a chip in the thread).
    @Enumerated(EnumType.STRING)
    private ReportStatus statusAfter;

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getStatusAfterLabel() {
        return statusAfter == null ? null
                : Report.labelFor(statusAfter, report != null && report.isStartedByAdmin());
    }
}
