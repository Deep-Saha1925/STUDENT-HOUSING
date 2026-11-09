package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<Notice> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Notice save(Notice notice) {
        return noticeRepository.save(notice);
    }

    public void toggle(Long id) {
        noticeRepository.findById(id).ifPresent(n -> {
            n.setActive(!n.isActive());
            noticeRepository.save(n);
        });
    }

    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    /**
     * Notices to show on the index page for the given viewer.
     * role == null means a guest (only ALL notices). Admins see everything active.
     */
    public List<Notice> visibleFor(Role role) {
        List<NoticeAudience> audiences;
        if (role == null) {
            audiences = List.of(NoticeAudience.ALL);
        } else if (role == Role.ADMIN) {
            audiences = List.of(NoticeAudience.values());
        } else if (role == Role.STUDENT) {
            audiences = List.of(NoticeAudience.ALL, NoticeAudience.STUDENT);
        } else {
            audiences = List.of(NoticeAudience.ALL, NoticeAudience.OWNER);
        }
        return noticeRepository.findVisible(LocalDateTime.now(), audiences);
    }
}
