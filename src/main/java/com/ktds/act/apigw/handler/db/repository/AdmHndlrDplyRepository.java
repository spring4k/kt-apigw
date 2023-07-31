package com.ktds.act.apigw.handler.db.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ktds.act.apigw.handler.db.entity.AdmHndlrDply;

import reactor.core.publisher.Flux;

@Repository
public interface AdmHndlrDplyRepository extends MongoRepository<AdmHndlrDply, Object> {

    // Flux<AdmHndlrDply> findByDplyDtAfter(LocalDateTime lastModifiedDate);
    List<AdmHndlrDply> findByDplyDtAfter(LocalDateTime lastModifiedDate);
    List<AdmHndlrDply> findByDplyType(String dplyType);

}
