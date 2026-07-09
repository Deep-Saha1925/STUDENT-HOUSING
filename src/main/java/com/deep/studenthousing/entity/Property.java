package com.deep.studenthousing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String city;
    private String area;

    @Column(name = "rent")
    private double monthlyRent;

    private Double dailyRent;

    // Whether the owner offers this property for each rental type.
    // columnDefinition gives existing rows a value when Hibernate adds these
    // columns via ddl-auto=update — without it, ALTER TABLE ... NOT NULL fails
    // on a table that already has rows.
    @Column(columnDefinition = "boolean default true", nullable = false)
    private boolean availableMonthly = true;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean availableDaily = false;

    private Double latitude;
    private Double longitude;

    private boolean available = true;   //checking whether is available for student.

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    //image urls
    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

}