package com.ktds.act.apigw.common.token;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ktds.act.apigw.common.token.entity.ApiLinkToken;
import com.ktds.act.apigw.common.token.entity.ApiLinkTokenData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenFactory implements InitializingBean {

	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	private static TokenFactoryManager manager;

	@Autowired
	public TokenFactory(TokenFactoryManager _manager) {
		manager = _manager;
	}

	// 테스트 용도
	public static Set<String> getAllAcccessToken() {

		return manager.getAllAcccessToken();
	}
	
	public static Optional<ApiLinkToken> getTokenByAccess(String acesTkn) {
	
		ApiLinkToken token = manager.getTokenByAccess(acesTkn).orElseGet(() -> {
												return manager.getTokenByAccessFromDB(acesTkn);
											});
		return Optional.ofNullable(token);
	}

	// 테스트 용도
	public static Set<String> getAllRefreshToken() {
		
		return manager.getAllRefreshToken();
	}
	
	public static Optional<ApiLinkToken> getTokenByRefresh(String rfrTkn) {
	
		ApiLinkToken token = manager.getTokenByRefresh(rfrTkn).orElseGet(() -> {
												return manager.getTokenByRefreshFromDB(rfrTkn);
											});
		return Optional.ofNullable(token);
	}

	// Username을 이용한 Token 조회
	// 다 건의 Username이 입력될 수 있으며, 이 경우 가장 최근의(DplyDt) 데이터 한 건(Paging)만 추출한다. 
	public static Optional<ApiLinkToken> getTokenByUsername(String username) {
	
		return manager.getTokenByUsername(username);
	}
	
	public static void putToken(String username, String acesTkn, String rfrTkn, ApiLinkTokenData data, LocalDateTime expirDt) {

		Assert.notNull(username, "username must not be null!");
		Assert.notNull(data, "data must not be null!");
		Assert.notNull(expirDt, "expirDt must not be null!");

		manager.putToken(username, acesTkn, rfrTkn, data, expirDt);
	}
	
	private void refresh() {
		manager.refresh();
	}

    @Scheduled(fixedDelayString = "${common.token.schedule.dbSync}", timeUnit = TimeUnit.SECONDS)
	private void syncTokenFactory() {
	
		applicationEventPublisher.publishEvent(new TokenFactoryEvent(this));
	}
	
	@EventListener(TokenFactoryEvent.class)
	private void onTokenFactoryEvent() {
		
		this.refresh();
	}

	@Override
	public void afterPropertiesSet() {
		log.info("TokenFactory.afterPropertiesSet - Start");

		refresh();

		log.info("TokenFactory.afterPropertiesSet - End");
	}
}
