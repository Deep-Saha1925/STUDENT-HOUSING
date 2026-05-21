package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByCity(String city);

    List<Property> findByOwner(User owner);

    List<Property> findByCityContainingIgnoreCase(String query);

    List<Property> findByRentLessThanEqual(Double rent);

    @Query(value = "SELECT * FROM properties p " +
            "WHERE (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:rent IS NULL OR p.rent <= :rent)",
            nativeQuery = true)
    List<Property> searchProperties(@Param("city") String city,
                                    @Param("rent") Double rent);

    /**
     * Haversine formula in PostgreSQL.
     * Returns properties within given radius (km), sorted nearest first.
     * Only returns available properties that have coordinates.
     */
    @Query(value = """
    SELECT *, 
           (6371 * acos(
               cos(radians(:lat)) * cos(radians(p.latitude))
               * cos(radians(p.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(p.latitude))
           )) AS distance
    FROM properties p
    WHERE p.latitude  IS NOT NULL
      AND p.longitude IS NOT NULL
      AND p.available = true
      AND (6371 * acos(
               cos(radians(:lat)) * cos(radians(p.latitude))
               * cos(radians(p.longitude) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(p.latitude))
           )) <= :radius
    ORDER BY distance ASC
    """, nativeQuery = true)
    List<Property> findNearby(@Param("lat") double lat,
                              @Param("lng") double lng,
                              @Param("radius") double radius);

}
