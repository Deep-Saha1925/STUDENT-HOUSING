package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_messages")
@Getter
@Setter
@NoArgsConstructor
public class ComplaintMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "complaint_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Complaint complaint;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    private boolean fromAdmin;

    @Column(nullable = false, length = 2000)
    private String message = "";

    @Enumerated(EnumType.STRING)
    private ComplaintStatus statusAfter;

    private LocalDateTime createdAt = LocalDateTime.now();
}
