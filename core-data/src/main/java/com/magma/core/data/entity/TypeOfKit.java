package com.magma.core.data.entity;

import org.springframework.data.annotation.Id;

public class TypeOfKit {

    @Id
    private String id;

    private String name;

    private String value;

    public TypeOfKit() {

    }

    public TypeOfKit(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
