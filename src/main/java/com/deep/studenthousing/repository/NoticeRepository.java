package com.deep.studenthousing.repository;

import com.deep.studenthousing.entity.Notice;
import com.deep.studenthousing.entity.NoticeAudience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notice n WHERE n.active = true " +
           "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
           "AND n.audience IN :audiences ORDER BY n.createdAt DESC")
    List<Notice> findVisible(@Param("now") LocalDateTime now,
                             @Param("audiences") Collection<NoticeAudience> audiences);
}
