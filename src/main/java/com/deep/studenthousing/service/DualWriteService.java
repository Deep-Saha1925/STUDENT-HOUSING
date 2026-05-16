package com.deep.studenthousing.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class DualWriteService {

    @PersistenceContext(unitName = "localEntityManagerFactory")
    private EntityManager localEm;

    @PersistenceContext(unitName = "cloudEntityManagerFactory")
    private EntityManager cloudEm;



}
