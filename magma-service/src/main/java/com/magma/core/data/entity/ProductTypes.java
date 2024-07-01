package com.magma.core.data.entity;

import com.magma.core.data.support.Connectivity;
import com.magma.core.data.support.Protocol;
import com.magma.core.util.ActuatorCode;
import org.springframework.data.annotation.Id;

import java.util.List;

public class ProductTypes {

    @Id
    private String id;
    private String productName;
    private List<String> deviceCategory;
    private String[] sensorCodes;
    private ActuatorCode[] actuatorCodes;
    private boolean persistence;
    private Protocol protocol;
    private Connectivity connectivity;
    private String codecName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<String> getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(List<String> deviceCategory) {
        this.deviceCategory = deviceCategory;
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

}
