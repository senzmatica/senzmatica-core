package com.magma.dmsdata.data.dto;

import com.magma.dmsdata.data.support.Connectivity;
import com.magma.dmsdata.data.support.Protocol;
import com.magma.dmsdata.util.ActuatorCode;
import com.magma.dmsdata.util.SensorCode;
import com.magma.util.MagmaUtil;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ProductTypeDTO {

    @Id
    private String id;
    @NotEmpty(message = "Product Name can't be empty")
    @NotNull(message = "Product Name can't be null")
    private String productName;

    private SensorCode[] sensorCodes;
    private ActuatorCode[] actuatorCodes;

    private boolean persistence;

    @NotNull(message = "Protocol can't be null")
    private Protocol protocol;

    @NotNull(message = "Connectivity can't be null")
    private Connectivity connectivity;
    private String codecName;
    private boolean transcoder;

    public boolean validate() {
        return MagmaUtil.validate(id)
                && productName != null
                && sensorCodes != null
                && actuatorCodes != null
                && (sensorCodes.length != 0 || actuatorCodes.length != 0)
                && connectivity != null
                && protocol != null;
    }

    public boolean addValidate() {
        return productName != null
                && sensorCodes != null
                && actuatorCodes != null
                && (sensorCodes.length != 0 || actuatorCodes.length != 0)
                && connectivity != null
                && protocol != null;
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
