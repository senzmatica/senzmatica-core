package com.magma.dmsdata.data.dto;

public class SensorCodeDTO {
    private String code;
    private String codeValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    @Override
    public String toString() {
        return "SensorCode{" +
                "code='" + code + '\'' +
                ", codeValue='" + codeValue + '\'' +
                '}';
    }
}
