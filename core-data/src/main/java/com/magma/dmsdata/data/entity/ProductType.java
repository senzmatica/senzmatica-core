package com.magma.dmsdata.data.entity;

import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.data.support.ProductVersion;
import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.Protocol;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class ProductType {

    @Id
    private String id;
    private String productName;
    private List<ProductVersion> versions;
    private String[] sensorCodes;
    private ActuatorCode[] actuatorCodes;
    private boolean persistence;
    private Protocol protocol;
    private Connectivity connectivity;
    private String codecName;
    private boolean transcoder;
    private boolean otaUpgradable;
    private boolean remotelyConfigurable;
    private String dataFormat;

    public ProductType(String productName) {
        this.productName = productName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ProductVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ProductVersion> versions) {
        this.versions = versions;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String[] getSensorCodes() {
        return sensorCodes;
    }

    public void setSensorCodes(String[] sensorCodes) {
        this.sensorCodes = sensorCodes;
    }

    public ActuatorCode[] getActuatorCodes() {
        return actuatorCodes;
    }

    public void setActuatorCodes(ActuatorCode[] actuatorCodes) {
        this.actuatorCodes = actuatorCodes;
    }

    public boolean isPersistence() {
        return persistence;
    }

    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Connectivity getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public boolean isTranscoder() {
        return transcoder;
    }

    public void setTranscoder(boolean transcoder) {
        this.transcoder = transcoder;
    }

    public boolean isOtaUpgradable() {
        return otaUpgradable;
    }

    public void setOtaUpgradable(boolean otaUpgradable) {
        this.otaUpgradable = otaUpgradable;
    }

    public boolean isRemotelyConfigurable() {
        return remotelyConfigurable;
    }

    public void setRemotelyConfigurable(boolean remotelyConfigurable) {
        this.remotelyConfigurable = remotelyConfigurable;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

}
