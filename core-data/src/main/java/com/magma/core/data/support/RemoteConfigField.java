package com.magma.core.data.support;


import java.util.Objects;
import java.util.Set;

public class RemoteConfigField {

    private String parameterCategory;

    private String parameter;

    private String parameterId;

    private String inputType;

    private String format;

    private String defaultValue;

    public RemoteConfigField() {
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String id) {
        this.parameterId = id;
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

    public String getParameterCategory() {
        return parameterCategory;
    }

    public void setParameterCategory(String parameterCategory) {
        this.parameterCategory = parameterCategory;
    }

    public boolean isEqual(RemoteConfigField other) {
        if (other == this) {
            return true;
        }
        return Objects.equals(parameterCategory, other.parameterCategory) &&
                Objects.equals(parameter, other.parameter) &&
                Objects.equals(parameterId, other.parameterId) &&
                Objects.equals(inputType, other.inputType) &&
                Objects.equals(format, other.format) &&
                Objects.equals(defaultValue, other.defaultValue);
    }
}

