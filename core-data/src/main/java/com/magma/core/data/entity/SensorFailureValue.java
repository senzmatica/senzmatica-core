package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class SensorFailureValue {
    @Id
    private String code;
    private List<Double> values;

    public SensorFailureValue() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public boolean validate() {
        return this.code != null && this.values != null;
    }

    @Override
    public String toString() {
        return "SensorFailureValue{" +
                "code='" + code + '\'' +
                ", values=" + values +
                '}';
    }
}
