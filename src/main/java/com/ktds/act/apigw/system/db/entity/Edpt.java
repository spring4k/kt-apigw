package com.ktds.act.apigw.system.db.entity;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Edpt {
    
    String prot;
    Atrib atrib;

    public Optional<String> chooseUrl() {
   
//    	if (!StringUtils.containsAnyIgnoreCase(prot, "HTTP", "HTTPS", "MTLS", "CLOSEABLE", "NONE")) {
        if (!StringUtils.containsAnyIgnoreCase(prot, "HTTP", "HTTPS", "MTLS", "NONE")) {
    		log.info("chooseUrl() Unsupport protocol: [{}]", prot);
    		return Optional.empty();
    	}
    	
    	return Optional.ofNullable(atrib.getUrl().get(ThreadLocalRandom.current().nextInt(atrib.getUrl().size())));
    }
}