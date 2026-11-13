package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Complaint;
import com.deep.studenthousing.entity.ComplaintMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintMessageRepository extends JpaRepository<ComplaintMessage, Long> {
    List<ComplaintMessage> findByComplaintOrderByCreatedAtAsc(Complaint complaint);
    long countByComplaint(Complaint complaint);
}
