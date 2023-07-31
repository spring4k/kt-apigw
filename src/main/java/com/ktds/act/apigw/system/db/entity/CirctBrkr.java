package com.ktds.act.apigw.system.db.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CirctBrkr {

    // private Long resCd;
	private List<String> resCd;
    private Long failRate;
    private Long minBaseCnt;
    private Long baseReachTm;
    private Long blckDuratTm;

    @Override
    public boolean equals(Object obj) {
    	if (obj == this)				return true;
    	if (!(obj instanceof CirctBrkr)) {
    		// TODO
    		// throws Exception
    		return false;
    	}
    
    	CirctBrkr circtBrkr = (CirctBrkr)obj; 
    	if (circtBrkr.getResCd().equals(this.resCd)
    			&& circtBrkr.getFailRate().equals(this.failRate)
    			&& circtBrkr.getMinBaseCnt().equals(this.minBaseCnt)
    			&& circtBrkr.getBaseReachTm().equals(this.baseReachTm)
    			&& circtBrkr.getBlckDuratTm().equals(this.blckDuratTm)) {
    		return true;
    	}
    	
    	return false;
    }
}
