package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Complaint;
import com.deep.studenthousing.entity.ComplaintStatus;
import com.deep.studenthousing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentOrderByUpdatedAtDesc(User student);
    List<Complaint> findAllByOrderByUpdatedAtDesc();
    List<Complaint> findByStatusOrderByUpdatedAtDesc(ComplaintStatus status);
    long countByAdminUnreadTrue();
    long countByStudentAndStudentUnreadTrue(User student);
    long countByStudentAndCreatedAtAfter(User student, LocalDateTime since);
    boolean existsByStudentAndPropertyIdAndStatusIn(User student, Long propertyId, Collection<ComplaintStatus> statuses);
}
