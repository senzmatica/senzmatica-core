package com.magma.core.data.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.core.util.Aggregation;
import com.magma.core.util.OperationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Operation implements Comparable<Operation> {

    private Aggregation aggregation;

    private List<Integer> sensorNumberList;

    private Integer propertyNumber;

    private OperationType type;

    private Map<String, Object> params = new HashMap<>();

    public Operation() {
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public List<Integer> getSensorNumberList() {
        return sensorNumberList;
    }

    public void setSensorNumberList(List<Integer> sensorNumberList) {
        this.sensorNumberList = sensorNumberList;
    }

    public Integer getPropertyNumber() {
        return propertyNumber;
    }

    public void setPropertyNumber(Integer propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        return propertyNumber.equals(operation.propertyNumber) && sensorNumberList.equals(operation.sensorNumberList);

    }

    @Override
    public int hashCode() {
        return propertyNumber.hashCode();
    }

    @Override
    public int compareTo(Operation o) {
        return propertyNumber - o.getPropertyNumber();
    }

    @Override
    public String toString() {
        return "Operation{" +
                "aggregation=" + aggregation +
                ", sensorNumberList=" + sensorNumberList +
                ", propertyNumber=" + propertyNumber +
                ", type=" + type +
                '}';
    }

}