package com.magma.dmsdata.data.support;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private String id;
    private String username;
    private String lastName;

    private List<String> references = new ArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", lastName='" + lastName + '\'' +
                ", references=" + references +
                '}';
    }
}
