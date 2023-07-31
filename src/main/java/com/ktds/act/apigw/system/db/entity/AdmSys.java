package com.ktds.act.apigw.system.db.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "admSysDply")
@Getter
@Setter
@ToString
public class AdmSys {
	@Id
	private String id;
	private String sysId;
	private String sysNm;
	private String sysCd;
	private String apiLinkCd;
	private Edpt edpt;
	private CirctBrkr circtBrkr;
	private LocalDateTime dplyDt;
	private String dplyType;
}
