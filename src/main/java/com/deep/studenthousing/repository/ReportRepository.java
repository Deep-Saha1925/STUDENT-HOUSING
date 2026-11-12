package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Report;
import com.deep.studenthousing.entity.ReportStatus;
import com.deep.studenthousing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByOwnerOrderByUpdatedAtDesc(User owner);
    List<Report> findAllByOrderByUpdatedAtDesc();
    List<Report> findByStatusOrderByUpdatedAtDesc(ReportStatus status);
    long countByAdminUnreadTrue();
    long countByOwnerAndCreatedAtAfter(User owner, java.time.LocalDateTime since);
    long countByOwnerAndOwnerUnreadTrue(User owner);
}
