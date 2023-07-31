package com.ktds.act.apigw.common.token.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.ktds.act.apigw.common.token.entity.ApiLinkToken;
import com.ktds.act.apigw.common.token.service.TokenService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TokenRefresher {
	
	private Map<String, ApiLinkToken> accessTokenMap = new ConcurrentHashMap<String, ApiLinkToken>();
	private Map<String, ApiLinkToken> refreshTokenMap = new ConcurrentHashMap<String, ApiLinkToken>();

	private LocalDateTime lastModifiedDt = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIN);

	private TokenService tokenService;
	
	public TokenRefresher(TokenService service) {

		tokenService = service;
	}

	private long countByExpirDtGreaterThanAndDplyDtLessThanEqual(LocalDateTime dt) {
		
		return tokenService.countByExpirDtGreaterThanAndDplyDtLessThanEqual(LocalDateTime.now(), dt);
	}

	private boolean isChanged(long count) {
		
		if (accessTokenMap.size() != count || refreshTokenMap.size() != count)
			return true;
		
		return false;
	}
	
	private void deleteAccessTokenForChanged() {

		Set<String> tokens = accessTokenMap.keySet();
		Set<String> restOfTokens = tokenService.findAllByAcesTknInAndExpirDtGreaterThanAndDplyDtLessThanEqual(tokens, LocalDateTime.now(), lastModifiedDt)
											.stream().map(token -> token.getAcesTkn()).collect(Collectors.toSet());
	
		Collection<String> deletedTokens = CollectionUtils.subtract(tokens, restOfTokens);
		deletedTokens.forEach(token -> {
			log.info("deleteAccessTokenForChanged() Deleted key: [{}]", token);
			accessTokenMap.remove(token);
		});
	}
	
	private void deleteRefreshTokenForChanged() {

		Set<String> tokens = refreshTokenMap.keySet();
		Set<String> restOfTokens = tokenService.findAllByRfrTknInAndExpirDtGreaterThanAndDplyDtLessThanEqual(tokens, LocalDateTime.now(), lastModifiedDt)
											.stream().map(token -> token.getRfrTkn()).collect(Collectors.toSet());
	
		Collection<String> deletedTokens = CollectionUtils.subtract(tokens, restOfTokens);
		deletedTokens.forEach(token -> {
			log.info("deleteRefreshTokenForChanged() Deleted key: [{}]", token);
			refreshTokenMap.remove(token);
		});
	}
	
	private LocalDateTime getMaxLocalDateTime(List<ApiLinkToken> tokens) {
		
		List<LocalDateTime> times = tokens.stream().map(data -> data.getDplyDt()).collect(Collectors.toList());
		return Collections.max(times);
	}
	
	private long updatApiLinkTokenByDplyDtAfter(LocalDateTime dt) {

		// 다 건의 Username이 존재할 수 있고, 이 경우 중복을 허용하지 않는 ConcurrentHashMap에 의도하지 않은 데이터가 입력될 수 있다.
		// 이를 방지하기 위해 DplyDt에 ASC 정렬을 하여, 가장 최신의 DplyDt가 최종 입력된다.
		// 현재 Username 조회 시에 DB를 바로 접근하기 때문에 위의 구현이 의미가 없을 수 있으나 소스에는 반영되어있다.
		List<ApiLinkToken> changedTokens = tokenService.findAllByExpirDtGreaterThanAndDplyDtGreaterThan(LocalDateTime.now(), dt);
		if (changedTokens.isEmpty()) {
			return 0;
		}
		
		changedTokens.stream()
					.filter(token ->  LocalDateTime.now().isBefore(token.getExpirDt()))
					.forEach(token -> {
			
						log.info("updatApiLinkTokenByDplyDtAfter() Insert AccessToken: [{}]", token.getAcesTkn());
						accessTokenMap.put(token.getAcesTkn(), token);
			
						log.info("updatApiLinkTokenByDplyDtAfter() Insert RefreshToken: [{}]", token.getRfrTkn());
						refreshTokenMap.put(token.getRfrTkn(), token);
					});
		
		lastModifiedDt = getMaxLocalDateTime(changedTokens);
		return changedTokens.size();
	}
	
	public void refresh() {
	
		log.debug("refresh() [TOKEN] BEGIN");
		// 기준시간(lastModifiedDt) 이전에 변경된 건수 확인
		long count = countByExpirDtGreaterThanAndDplyDtLessThanEqual(lastModifiedDt);
		log.debug("refresh() Before Info: [{} / {}]", count, lastModifiedDt);
		if (isChanged(count)) {
			// 변경된 사항이 존재한다는 의미로 'Update'/'Delete'된 케이스가 존재하여 이를 삭제함
			// 'Update': 삭제 후, 추가
			// 'Delete': 삭제
			log.debug("refresh() Changed.");
			deleteAccessTokenForChanged();
			deleteRefreshTokenForChanged();
		}
	
		// 기준시간(lastModifiedDt) 이후에 변경된 건수 확인
		// 'Insert': 신규 추가
		// 'Update': 기삭제된 데이터에 대한 추가
		long updatedCount = updatApiLinkTokenByDplyDtAfter(lastModifiedDt);
		log.debug("refresh() After Info: [{} / {}]", updatedCount, lastModifiedDt);
		log.debug("refresh() END");
	}
}
