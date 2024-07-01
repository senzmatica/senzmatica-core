package com.magma.dmsdata.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "http_acl_auth")
public class Http_acl_auth {
    @Id
    private String id;

    private String client_id;

    private String client_secret;

    private String username;

    private String password;

    private String passhash;

    private String backupPasshash;

    private String grant_type;

    private String scope;

    private List<String> authorities;

    private boolean status = true;

    public Http_acl_auth() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public String getBackupPasshash() {
        return backupPasshash;
    }

    public void setBackupPasshash(String backupPasshash) {
        this.backupPasshash = backupPasshash;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Http_acl_auth{" +
                "id='" + id + '\'' +
                ", client_id='" + client_id + '\'' +
                ", client_secret='" + client_secret + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", passhash='" + passhash + '\'' +
                ", backupPasshash='" + backupPasshash + '\'' +
                ", grant_type='" + grant_type + '\'' +
                ", scope='" + scope + '\'' +
                ", authorities=" + authorities +
                ", status=" + status +
                '}';
    }

    public void validate() {
        if (!"client_credentials".equals(grant_type) && !"password".equals(grant_type)
                && !"refresh_token".equals(grant_type)) {
            throw new MagmaException(MagmaStatus.INVALID_GRANT_TYPE);
        }

        if (!"read,write,trust".equals(scope) && !"read,write".equals(scope)) {
            throw new MagmaException(MagmaStatus.INVALID_SCOPE);
        }
    }

}
