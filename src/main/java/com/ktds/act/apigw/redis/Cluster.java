package com.ktds.act.apigw.redis;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.redis.cluster")
@Data
public class Cluster {
    private boolean use;
    private List<String> nodes;
}
