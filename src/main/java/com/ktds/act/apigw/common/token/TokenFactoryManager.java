package com.ktds.act.apigw.common.token;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.ktds.act.apigw.common.token.entity.ApiLinkToken;
import com.ktds.act.apigw.common.token.entity.ApiLinkTokenData;
import com.ktds.act.apigw.common.token.service.TokenService;
import com.ktds.act.apigw.common.token.vo.TokenRefresher;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Component
public class TokenFactoryManager {

	private TokenService tokenService;
	private TokenRefresher tokenRefresher;
	
	public TokenFactoryManager(TokenService service) {
		log.info("TokenFactoryManager() Created. [{}]", service);

		tokenService = service;
		tokenRefresher = new TokenRefresher(tokenService);
	}

	// 테스트 용도
	public Set<String> getAllAcccessToken() {
		return tokenRefresher.getAccessTokenMap().keySet();
	}
	
	public ApiLinkToken getTokenByAccessFromDB(String token) {
	
		log.info("getTokenByAccessFromDB() token: [{}]", token);
		return tokenService.findByAccessToken(token, LocalDateTime.now());
	}
	
	public Optional<ApiLinkToken> getTokenByAccess(String token) {
		
		return Optional.ofNullable(tokenRefresher.getAccessTokenMap().get(token));
	}

	// 테스트 용도
	public Set<String> getAllRefreshToken() {
		return tokenRefresher.getRefreshTokenMap().keySet();
	}	

	public ApiLinkToken getTokenByRefreshFromDB(String token) {
	
		log.info("getTokenByRefreshFromDB() token: [{}]", token);
		return tokenService.findByRefreshToken(token, LocalDateTime.now());
	}
	
	public Optional<ApiLinkToken> getTokenByRefresh(String token) {
		
		return Optional.ofNullable(tokenRefresher.getRefreshTokenMap().get(token));
	}
	
	public Optional<ApiLinkToken> getTokenByUsername(String username) {
	
		return tokenService.getTokenByUsername(username, LocalDateTime.now());
	}
	
	public void putToken(String username, String acesTkn, String rfrTkn, ApiLinkTokenData data, LocalDateTime expirDt) {

		ApiLinkToken token = new ApiLinkToken();
		
		token.setUsername(username);
		token.setAcesTkn(acesTkn);
		token.setRfrTkn(rfrTkn);
		token.setData(data);
		token.setExpirDt(expirDt);
		token.setDplyDt(LocalDateTime.ofInstant(Instant.ofEpochMilli(new Date().getTime()),
                TimeZone.getDefault().toZoneId()));
	
		tokenService.putApiLinkToken(token);
	}
	
	public void refresh() {

		tokenRefresher.refresh();
	}
}
