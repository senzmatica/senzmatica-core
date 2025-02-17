package com.magma.dmsdata.data.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class TypeOfKitDTO {

    @NotNull(message = "Please provide kit type value")
    @NotEmpty(message = "Kit type value can't be empty")
    private String value;

    public TypeOfKitDTO() {
    }

    public TypeOfKitDTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
