package com.magma.dmsdata.data.support;

import java.util.List;

public class MetaData {
    private List sensors;
    private List actuators;
    private List kitTypes;

    public List getActuators() {
        return actuators;
    }

    public void setActuators(List actuators) {
        this.actuators = actuators;
    }

    public List getSensors() {
        return sensors;
    }

    public void setSensors(List sensors) {
        this.sensors = sensors;
    }

    public List getKitTypes() {
        return kitTypes;
    }

    public void setKitTypes(List kitTypes) {
        this.kitTypes = kitTypes;
    }
}
