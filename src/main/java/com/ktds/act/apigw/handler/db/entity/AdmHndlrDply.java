package com.ktds.act.apigw.handler.db.entity;


import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Data
@Document
@ToString
public class AdmHndlrDply {

    @Id
    private String id;
    private String hndlrId;
    private String trtSect;
    private String type; // SYS/CSTM
    private List<String> dpdncHndlr;
    private boolean bodyRfrn;
    private String cstmClassAdr;
    private String cstmClassSrc;
    private String cstmClassNm;
    // private String optn;
    // private String desc;
    private LocalDateTime dplyDt;
    private String dplyType;


}

