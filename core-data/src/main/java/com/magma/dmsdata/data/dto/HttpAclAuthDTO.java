package com.magma.dmsdata.data.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class HttpAclAuthDTO {

    @NotEmpty(message = "Client ID can't be empty")
    @NotNull(message = "Client ID can't be null")
    private String client_id;

    @NotEmpty(message = "Client secret can't be empty")
    @NotNull(message = "Client secret can't be null")
    private String client_secret;

    @NotNull(message = "Grant type can't be null")
    private String grant_type;

    @NotNull(message = "Scope can't be null")
    private String scope;

    @NotNull(message = "Authorities can't be null")
    private List<String> authorities;

    public HttpAclAuthDTO() {

    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}
