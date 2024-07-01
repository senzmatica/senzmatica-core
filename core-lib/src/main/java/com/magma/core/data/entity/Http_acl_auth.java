package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
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

    private String passHash;

    private String backupPassHash;

    private String backupClient_id;

    private String grant_type;

    private String scope;

    private List<String> authorities;

    private boolean status = true;

    private boolean protect = false;

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

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public String getBackupPassHash() {
        return backupPassHash;
    }

    public void setBackupPassHash(String backupPassHash) {
        this.backupPassHash = backupPassHash;
    }

    public String GetBackupClient_id() {
        return backupClient_id;
    }

    public void SetBackupClient_id(String backupClient_id) {
        this.backupClient_id = backupClient_id;
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

    public boolean isProtect() {
        return protect;
    }

    public void setProtect(boolean protect) {
        this.protect = protect;
    }

    @Override
    public String toString() {
        return "Http_acl_auth{" +
                "id='" + id + '\'' +
                ", client_id='" + client_id + '\'' +
                ", client_secret='" + client_secret + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", passHash='" + passHash + '\'' +
                ", backupPassHash='" + backupPassHash + '\'' +
                ", backupClient_id='" + backupClient_id + '\'' +
                ", grant_type='" + grant_type + '\'' +
                ", scope='" + scope + '\'' +
                ", authorities=" + authorities +
                ", status=" + status +
                '}';
    }

    public void validate() {
        if (!"client_credentials".equals(grant_type) && !"password".equals(grant_type) && !"refresh_token".equals(grant_type)) {
            throw new MagmaException(MagmaStatus.INVALID_GRANT_TYPE);
        }

        if (!"read,write,trust".equals(scope) && !"read,write".equals(scope)) {
            throw new MagmaException(MagmaStatus.INVALID_SCOPE);
        }
    }

}
