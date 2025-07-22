package com.magma.core.data.dto;

import java.util.List;

public class SensorChangeRequestDTO {
    private List<SensorDTO> sensorDTOS;
    private List<SensorCodeDTO> sensorCodeDTOS;

    public List<SensorDTO> getSensorDTOS() {
        return sensorDTOS;
    }

    public void setSensorDTOS(List<SensorDTO> sensorDTOS) {
        this.sensorDTOS = sensorDTOS;
    }

    public List<SensorCodeDTO> getSensorCodeDTOS() {
        return sensorCodeDTOS;
    }

    public void setSensorCodeDTOS(List<SensorCodeDTO> sensorCodeDTOS) {
        this.sensorCodeDTOS = sensorCodeDTOS;
    }
}

