// package com.ktds.act.apigw.redis;

// import javax.annotation.PostConstruct;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.HashOperations;
// import org.springframework.data.redis.core.ListOperations;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.SetOperations;
// import org.springframework.data.redis.core.ValueOperations;
// import org.springframework.stereotype.Component;

// @Component
// public class RedisFactory {
// 	private static HashOperations<String, Object, Object> hashOperations = null;
// 	private static SetOperations<String, Object> setOperations = null;
// 	private static ListOperations<String, Object> listOperations = null;
// 	private static ValueOperations<String, Object> valueOperations = null;

// 	@Autowired
// 	RedisTemplate<String, Object> redisTemplate;

// 	@PostConstruct
// 	public void afterPropertiesSet() {
// 		hashOperations = redisTemplate.opsForHash();
// 		setOperations = redisTemplate.opsForSet();
// 		listOperations = redisTemplate.opsForList();
// 		valueOperations = redisTemplate.opsForValue();
// 	}

// 	/**
// 	 * Find Hash Data
// 	 * @param type
// 	 * @param key
// 	 * @return
// 	 */
// 	public static Object getHashOperations(String key, Object hashKey) {
// 		return hashOperations.get(key, hashKey);
// 	}

// 	/**
// 	 * Find Hash Data
// 	 * @param type
// 	 * @param key
// 	 * @return
// 	 */
// 	public static void putHashOperations(String key, Object hashKey, Object value) {
// 		hashOperations.put(key, hashKey, value);
// 	}

// 	/**
// 	 * Find Set Data
// 	 * @param key
// 	 * @return
// 	 */
// 	public static Object getSetOperations(String key) {
// 		return setOperations.pop(key);
// 	}

// 	/**
// 	 * Find List Data
// 	 * @param key
// 	 * @return
// 	 */
// 	public static Object getListOperations(String key) {
// 		return listOperations.leftPop(key);
// 	}

// 	/**
// 	 * Find Value Data
// 	 * @param key
// 	 * @return
// 	 */
// 	public static Object getValueOperations(String key) {
// 		return valueOperations.get(key);
// 	}
// }
