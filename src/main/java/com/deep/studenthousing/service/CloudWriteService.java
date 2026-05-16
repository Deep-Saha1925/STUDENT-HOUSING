package com.deep.studenthousing.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudWriteService {
    @Transactional("cloudTransactionManager")
    public <T> void save(T entity, EntityManager em) {
        em.merge(entity);
    }
}