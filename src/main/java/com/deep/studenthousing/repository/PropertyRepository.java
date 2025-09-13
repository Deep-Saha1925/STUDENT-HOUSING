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

    @Query("SELECT p FROM Property p " +
            "WHERE (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:rent IS NULL OR p.rent <= :rent)")
    List<Property> searchProperties(@Param("city") String city,
                                    @Param("rent") Double rent);
}
