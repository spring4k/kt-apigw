package com.ktds.act.apigw.redis;
import java.util.ArrayList;
import java.util.List;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.JCachingProvider;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;



@Configuration
@ConditionalOnExpression(
    "'${common.service.sla.db}'.equals('REDIS')"
)
public class RedisConfig  {

    @Value("${spring.redis.host}")
    String REDIS_HOST;

    @Value("${spring.redis.port}")
    String REDIS_PORT;

    @Value("${spring.redis.password}")
    String REDIS_PASSWORD;


    @Autowired
    private Cluster cluster;

    @Value("${common.service.sla.cache}")
    String hashKey;

    // redis cluster 사용에 따른 분기
    @Bean
    public Config config() {
        Config config = new Config();
        if(cluster.isUse()){
            List<String> hosts = cluster.getNodes();
            List<String> nodeAddressString = new ArrayList<>();
            hosts.forEach(host -> {
                nodeAddressString.add("redis://"+host);
            });
            config.useClusterServers()
                    .setNodeAddresses(nodeAddressString)
                    // .setPassword(REDIS_PASSWORD)
                    ;
        }else{
            // config.useSingleServer().setAddress("redis://"+REDIS_HOST + ":" + REDIS_PORT);
            config.useSingleServer()
            .setConnectionPoolSize(100)
            .setSubscriptionConnectionPoolSize(100)
            .setAddress("redis://"+REDIS_HOST + ":" + REDIS_PORT)
            // .setPassword(REDIS_PASSWORD)
            ;
        }
        return config;
    }

    // Redis 와 cache 연동
    // @Bean
    // Cache<String, byte[]> cache(Config config) {
    //     CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
    //     return cacheManager.createCache(hashKey, RedissonConfiguration.fromConfig(config));
    // }

    @Bean
    public CacheManager cacheManager(Config config) {
        CacheManager manager = Caching.getCachingProvider(JCachingProvider.class.getName()).getCacheManager();
        manager.createCache(hashKey, RedissonConfiguration.fromConfig(config));
        return manager;
    }

    @Bean
    ProxyManager<String> buckets(CacheManager cacheManager) {
        return new JCacheProxyManager<>(cacheManager.getCache(hashKey));
    }

    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(Config config) {
        return Redisson.create(config);
    }

    @Bean
	public RedisTemplate<String, Object> redisTemplate(RedissonClient redisson) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redissonConnectionFactory(redisson));
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

}
