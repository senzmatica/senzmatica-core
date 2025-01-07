package com.magma.dmsdata.data.entity;

import org.springframework.data.annotation.Id;

import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.Protocol;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.SensorCode;

import java.util.List;

public class ProductTypes {

    @Id
    private String id;
    private String productName;
    private SensorCode[] sensorCodes;
    private ActuatorCode[] actuatorCodes;
    private boolean persistence;
    private Protocol protocol;
    private Connectivity connectivity;
    private String codecName;
    private boolean transcoder;

    public ProductTypes(String productName) {
        this.productName = productName;
    }

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

    public SensorCode[] getSensorCodes() {
        return sensorCodes;
    }

    public void setSensorCodes(SensorCode[] sensorCodes) {
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
}
