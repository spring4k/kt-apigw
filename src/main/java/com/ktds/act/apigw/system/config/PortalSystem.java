package com.ktds.act.apigw.system.config;

import java.util.List;

import com.ktds.act.apigw.system.db.entity.AdmSys;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "data")
@Data
public class PortalSystem {
    private List<AdmSys> system;
}
