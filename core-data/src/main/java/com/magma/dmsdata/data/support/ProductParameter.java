package com.magma.dmsdata.data.support;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProductParameter {

    private String parameterCategory;

    private String parameter;

    private String id;

    private String inputType;

    private String format;

    private String defaultValue;

    private List<String> joinParameter;

    private Set<String> defaultValues;

    public ProductParameter() {
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setJoinParameter(List<String> joinParameter) {
        this.joinParameter = joinParameter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getJoinParameter() {
        return joinParameter;
    }

    public String getParameterCategory() {
        return parameterCategory;
    }

    public Set<String> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Set<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setParameterCategory(String parameterCategory) {
        this.parameterCategory = parameterCategory;
    }

    public boolean isEqual(ProductParameter other) {
        if (other == this) {
            return true;
        }
        return Objects.equals(parameterCategory, other.parameterCategory) &&
                Objects.equals(parameter, other.parameter) &&
                Objects.equals(id, other.id) &&
                Objects.equals(inputType, other.inputType) &&
                Objects.equals(format, other.format) &&
                Objects.equals(defaultValue, other.defaultValue) &&
                Objects.equals(joinParameter, other.joinParameter);
    }
}
