package com.deep.studenthousing.dto;

public record PropertyMapDTO(
        Long id,
        String title,
        String area,
        String city,
        double latitude,
        double longitude,
        double monthlyRent,
        boolean available,
        String thumbnailUrl
) {}