package com.magma.core.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magma.core.data.support.Acl;
import com.magma.core.validation.NotEmptyString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "vmq_acl_auth")
public class Vmq_acl_auth {

    @Id
    private String id;

    private String mountpoint;

    @NotNull(message = "Client Id can't be null")
    @NotEmpty(message = "Client Id can't be empty")
    private String client_id;

    @NotNull(message = "Username can't be null")
    @NotEmpty(message = "Username can't be empty")
    private String username;

    @NotNull(message = "Password can't be null")
    @NotEmpty(message = "Password can't be empty")
    private String password;

    private String passhash;

    private String backupPasshash;

    private boolean status = true;

    private boolean isProtect = false;

    @NotEmptyString(message = "Publish Acl can't be null or empty")
    private List<Acl> publish_acl = new ArrayList<>();

    @NotEmptyString(message = "Subscribe Acl can't be null or empty")
    private List<Acl> subscribe_acl = new ArrayList<>();

    public Vmq_acl_auth() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
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

    public List<Acl> getPublish_acl() {
        return publish_acl;
    }

    public void setPublish_acl(List<Acl> publish_acl) {
        this.publish_acl = publish_acl;
    }

    public List<Acl> getSubscribe_acl() {
        return subscribe_acl;
    }

    public void setSubscribe_acl(List<Acl> subscribe_acl) {
        this.subscribe_acl = subscribe_acl;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isProtect() {
        return isProtect;
    }

    public void setProtect(boolean protect) {
        isProtect = protect;
    }

    @Override
    public String toString() {
        return "Vmq_acl_auth{" +
                "id='" + id + '\'' +
                ", mountpoint='" + mountpoint + '\'' +
                ", client_id='" + client_id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", passhash='" + passhash + '\'' +
                ", backupPasshash='" + backupPasshash + '\'' +
                ", publish_acl=" + publish_acl +
                ", subscribe_acl=" + subscribe_acl +
                '}';
    }
}
