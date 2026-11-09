package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NoticeSeverity severity = NoticeSeverity.INFO;

    @Enumerated(EnumType.STRING)
    private NoticeAudience audience = NoticeAudience.ALL;

    // Admin can switch a notice off without deleting it.
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Optional: after this moment the notice disappears by itself.
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
