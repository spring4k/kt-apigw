package com.ktds.act.apigw.common.token.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ktds.act.apigw.common.token.entity.ApiLinkToken;

@Repository
public interface TokenRepository extends MongoRepository<ApiLinkToken, String> {

	public Optional<ApiLinkToken> findByUsernameAndAcesTknAndRfrTkn(String username, String acesTkn, String rfrTkn);
	
	public Long countByExpirDtGreaterThanAndDplyDtLessThanEqual(LocalDateTime expirDt, LocalDateTime dplyDt);

	public List<ApiLinkToken> findByUsernameAndExpirDtGreaterThan(String username, LocalDateTime expirDt, Pageable paging);

	public List<ApiLinkToken> findAllByAcesTknInAndExpirDtGreaterThanAndDplyDtLessThanEqual(Iterable<String> acesTkns, LocalDateTime expirDt, LocalDateTime dplyDt);
	
	public List<ApiLinkToken> findAllByRfrTknInAndExpirDtGreaterThanAndDplyDtLessThanEqual(Iterable<String> rfrTkns, LocalDateTime expirDt, LocalDateTime dplyDt);
	
	public List<ApiLinkToken> findAllByExpirDtGreaterThanAndDplyDtGreaterThan(LocalDateTime expirDt, LocalDateTime dplyDt, Sort sort);
	
	public ApiLinkToken findByAcesTknAndExpirDtGreaterThan(String acesTkn, LocalDateTime expirDt);
	
	public ApiLinkToken findByRfrTknAndExpirDtGreaterThan(String rfrTkn, LocalDateTime expirDt);
}
