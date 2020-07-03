package com.dimples.po;

import java.io.Serializable;

import lombok.Data;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/16
 */
@Data
public class Drug implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    private String primaryId;

    private String caseId;

    private String drugSeq;

    private String roleCod;

    private String drugname;

    private String prodAi;

    private String valVbm;

    private String route;

    private String doseVbm;

    private String cumDoseChr;

    private String cumDoseUnit;

    private String dechal;

    private String rechal;

    private String lotNum;

    private String expDt;

    private String ndaNum;

    private String doseAmt;

    private String doseUnit;

    private String doseForm;

    private String doseFreq;

    private static final long serialVersionUID = 1L;
}