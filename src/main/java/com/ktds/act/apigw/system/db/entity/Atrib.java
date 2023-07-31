package com.ktds.act.apigw.system.db.entity;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class Atrib {

    private List<String> url;
    private String certi;
    private String certiKey;
    
    private String ecod;
    private List<String> addr;
    private int minPool;
    private int maxPool;

}
