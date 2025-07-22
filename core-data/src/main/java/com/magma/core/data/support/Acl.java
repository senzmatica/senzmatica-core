package com.magma.core.data.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Acl {

    private String pattern;

    private Integer max_qos;

    public Acl() {
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMax_qos() {
        return max_qos;
    }

    public void setMax_qos(Integer max_qos) {
        this.max_qos = max_qos;
    }

    // TODO : Have to extend this class for Sub vs Pub
}