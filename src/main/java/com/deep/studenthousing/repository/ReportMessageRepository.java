package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Report;
import com.deep.studenthousing.entity.ReportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportMessageRepository extends JpaRepository<ReportMessage, Long> {
    List<ReportMessage> findByReportOrderByCreatedAtAsc(Report report);
    long countByReport(Report report);
}
