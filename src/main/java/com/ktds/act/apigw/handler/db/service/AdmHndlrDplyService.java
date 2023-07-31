package com.ktds.act.apigw.handler.db.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ktds.act.apigw.handler.db.entity.AdmHndlrDply;
import com.ktds.act.apigw.handler.db.repository.AdmHndlrDplyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdmHndlrDplyService {

	@Autowired
	private AdmHndlrDplyRepository admHndlrDplyRepository;


	public List<AdmHndlrDply> getAdmHndlrList(String dplyType)  {

//		return admHndlrDplyRepository.findAll();
		return admHndlrDplyRepository.findByDplyType(dplyType);
				// .collectList().block();

	}

	public List<AdmHndlrDply> getAdmHndlrList(LocalDateTime lastModifiedDate)  {

		return admHndlrDplyRepository.findByDplyDtAfter(lastModifiedDate);
				// .collectList().block();
	}

	
	
}
