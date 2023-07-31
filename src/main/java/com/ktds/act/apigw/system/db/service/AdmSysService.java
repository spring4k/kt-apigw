package com.ktds.act.apigw.system.db.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ktds.act.apigw.api.db.entity.AdmApiDply;
import com.ktds.act.apigw.api.db.repository.AdmApiDplyRepository;
import com.ktds.act.apigw.system.db.entity.AdmSys;
import com.ktds.act.apigw.system.db.repository.AdmSysRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AdmSysService {

	@Autowired
	private AdmSysRepository admSysRepository;

	
	
	// public Flux<AdmSys> getAllAdmSys()  {
	public List<AdmSys> getAllAdmSys()  {

		return admSysRepository.findAll();
	}

	public List<AdmSys> getAdmSysList(String dplyType)  {

//		return admSysRepository.findAll();
		return admSysRepository.findByDplyType(dplyType);
						// .collectList().block();
	}

	public List<String> getAdmSysId()  {

		return admSysRepository.findAll().stream().map(admSys -> admSys.getSysId())
							.collect(Collectors.toList());
		/*
		return admSysRepository.findAll()
		.map(admSys -> admSys.getSysId())
		.collectList().block();
		*/
	}

	public Long count()  {

		// return admSysRepository.count().block();
		return admSysRepository.count();

	}

	public List<AdmSys> getAdmSysList(LocalDateTime lastModifiedDate)  {

		// return admSysRepository.findByUpdDtAfter(lastModifiedDate).collectList().block();
		return admSysRepository.findByDplyDtAfter(lastModifiedDate);
	}

	
	
}
