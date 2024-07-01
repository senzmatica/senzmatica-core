package com.magma.dmsdata.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.dmsdata.data.entity.Device;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class CorporateSensorSummary {

    private String corporateId;

    private String corporateName;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    private DateTime summaryDate;

    private int totalNumbersOfSensorsInCorporate;

    private List<String> sensorCodesInCorporate;

    private List<String> sensorsInCorporate;

    // <"Temp",<"failureSensors",5>>
    private Map<String, Map<String, Integer>> propertySensorSummaryMap;

    // <"Temp",<"failureSensorsDevices',[D1,D2]>>
    private Map<String, Map<String, List<Device>>> propertySensorSummaryDeviceMap;

    private Map<String, Map<String, Integer>> nonPropertySensorSummaryMap;

    public CorporateSensorSummary() {
    }

    public CorporateSensorSummary(String corporateId) {
        this.corporateId = corporateId;
        this.summaryDate = new DateTime();
    }

    public String getCorporateId() {
        return corporateId;
    }

    public void setCorporateId(String corporateId) {
        this.corporateId = corporateId;
    }

    public DateTime getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(DateTime summaryDate) {
        this.summaryDate = summaryDate;
    }

    public int getTotalNumbersOfSensorsInCorporate() {
        return totalNumbersOfSensorsInCorporate;
    }

    public void setTotalNumbersOfSensorsInCorporate(int totalNumbersOfSensorsInCorporate) {
        this.totalNumbersOfSensorsInCorporate = totalNumbersOfSensorsInCorporate;
    }

    public List<String> getSensorCodesInCorporate() {
        return sensorCodesInCorporate;
    }

    public void setSensorCodesInCorporate(List<String> sensorCodesInCorporate) {
        this.sensorCodesInCorporate = sensorCodesInCorporate;
    }

    public List<String> getSensorsInCorporate() {
        return sensorsInCorporate;
    }

    public void setSensorsInCorporate(List<String> sensorsInCorporate) {
        this.sensorsInCorporate = sensorsInCorporate;
    }

    public Map<String, Map<String, Integer>> getPropertySensorSummaryMap() {
        return propertySensorSummaryMap;
    }

    public void setPropertySensorSummaryMap(Map<String, Map<String, Integer>> propertySensorSummaryMap) {
        this.propertySensorSummaryMap = propertySensorSummaryMap;
    }

    public Map<String, Map<String, Integer>> getNonPropertySensorSummaryMap() {
        return nonPropertySensorSummaryMap;
    }

    public void setNonPropertySensorSummaryMap(Map<String, Map<String, Integer>> nonPropertySensorSummaryMap) {
        this.nonPropertySensorSummaryMap = nonPropertySensorSummaryMap;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public Map<String, Map<String, List<Device>>> getPropertySensorSummaryDeviceMap() {
        return propertySensorSummaryDeviceMap;
    }

    public void setPropertySensorSummaryDeviceMap(
            Map<String, Map<String, List<Device>>> propertySensorSummaryDeviceMap) {
        this.propertySensorSummaryDeviceMap = propertySensorSummaryDeviceMap;
    }
}
