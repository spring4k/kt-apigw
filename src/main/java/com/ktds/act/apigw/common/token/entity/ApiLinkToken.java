package com.ktds.act.apigw.common.token.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document
@Getter
@Setter
@ToString
public class ApiLinkToken {

	@Id
	private String id;
	private String username;
	private String acesTkn;
	private String rfrTkn;
	private ApiLinkTokenData data;
	private LocalDateTime expirDt;
	private LocalDateTime dplyDt;
}
