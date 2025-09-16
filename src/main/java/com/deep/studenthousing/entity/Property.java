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
    private double rent;

    private boolean available = true;   //checking whether is available for student.

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    //image urls
    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

}
