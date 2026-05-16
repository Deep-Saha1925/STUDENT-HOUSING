package com.deep.studenthousing.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DualWriteService {

    @PersistenceContext(unitName = "local")
    private EntityManager localEm;

    @PersistenceContext(unitName = "cloud")
    private EntityManager cloudEm;

    @Transactional("transactionManager")
    public <T> void saveLocal(T entity) {
        localEm.merge(entity);
    }

    @Transactional("cloudTransactionManager")
    public <T> void saveCloud(T entity) {
        cloudEm.merge(entity);
    }

    public <T> void saveBoth(T entity) {
        saveLocal(entity);
        saveCloud(entity);
    }
}