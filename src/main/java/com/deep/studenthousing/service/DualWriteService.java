package com.deep.studenthousing.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DualWriteService {

    private final LocalWriteService localWriteService;
    private final CloudWriteService cloudWriteService;

    public DualWriteService(LocalWriteService localWriteService, CloudWriteService cloudWriteService) {
        this.localWriteService = localWriteService;
        this.cloudWriteService = cloudWriteService;
    }

    @PersistenceContext(unitName = "local")
    private EntityManager localEm;

    @PersistenceContext(unitName = "cloud")
    private EntityManager cloudEm;

    public <T> void saveBoth(T entity) {
        localWriteService.save(entity, localEm);
        cloudWriteService.save(entity, cloudEm);
    }
}