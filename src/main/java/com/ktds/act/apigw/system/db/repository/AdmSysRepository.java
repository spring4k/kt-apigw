package com.ktds.act.apigw.system.db.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ktds.act.apigw.system.db.entity.AdmSys;

import reactor.core.publisher.Flux;

@Repository
public interface AdmSysRepository extends MongoRepository<AdmSys, Long> {
	// Flux<AdmSys> findByUpdDtAfter(LocalDateTime updDt);
	List<AdmSys> findByDplyDtAfter(LocalDateTime updDt);
	List<AdmSys> findByDplyType(String dplyType);
}
