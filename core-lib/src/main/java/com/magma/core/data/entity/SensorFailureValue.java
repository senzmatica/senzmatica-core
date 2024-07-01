package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.core.util.SensorCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class SensorFailureValue {
    @Id
    private SensorCode code;
    private List<Double> values;

    public SensorFailureValue() {
    }

    public SensorCode getCode() {
        return code;
    }

    public void setCode(SensorCode code) {
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
