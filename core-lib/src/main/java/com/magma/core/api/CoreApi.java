package com.magma.core.api;

import com.magma.dmsdata.data.dto.*;
import com.magma.dmsdata.data.entity.*;
import com.magma.dmsdata.data.support.*;
import com.magma.core.gateway.HTTPMessageHandler;
import com.magma.core.job.CoreSchedule;
import com.magma.core.service.*;
import com.magma.dmsdata.util.*;
import com.magma.dmsdata.validation.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class CoreApi {

    @Autowired
    CoreService coreService;

    @Autowired
    HTTPMessageHandler httpMessageHandler;

    @Autowired
    DataProcessorService dataProcessorService;

    @Autowired
    CoreSchedule coreSchedule;

    @Autowired
    DeviceTestService deviceTestService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    KitCoreService kitService;

    @Value("${project.version}")
    private String version;

    @RequestMapping(value = "/core/memory-status", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, Long>> getMemoryStatistics() {
        HashMap<String, Long> stats = new HashMap<>();
        stats.put("HeapSize", Runtime.getRuntime().totalMemory());
        stats.put("HeapMaxSize", Runtime.getRuntime().maxMemory());
        stats.put("HeapFreeSize", Runtime.getRuntime().freeMemory());
        return new MagmaResponse<>(stats);
    }

    @RequestMapping(value = "/core/open/info", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, String>> getInfo() {
        HashMap<String, String> stats = new HashMap<>();
        stats.put("version", version);
        return new MagmaResponse<>(stats);
    }

    @RequestMapping(value = "/core/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody @Valid DeviceDTO deviceDTO, BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(deviceService.create(deviceDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody DeviceDTO deviceDTO,
                                              @PathVariable("userId") String userId,
                                              BindingResult result) {
        if (result.hasErrors()) {
            throw new com.magma.dmsdata.validation.BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(deviceService.createDevice(deviceDTO, userId));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration/{device}/user/{userId}", method = RequestMethod.PUT)
    public MagmaResponse<String> updateDeviceParameterConfiguration(
            @PathVariable("device") String deviceId,
            @PathVariable("userId") String userId,
            @RequestBody DeviceParameterConfiguration deviceParameterConfiguration) {
        return new MagmaResponse<>(coreService.updateDeviceParameterConfiguration(deviceId, deviceParameterConfiguration, userId));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration/bulk/user/{userId}", method = RequestMethod.PUT)
    public MagmaResponse<List<Map<String, String>>> updateDeviceParameterConfiguration(
            @RequestBody List<DeviceParameterConfiguration> deviceParameterConfigurations, @PathVariable("userId") String userId) {
        return new MagmaResponse<>(coreService.bulkUpdateDeviceParameterConfiguration(deviceParameterConfigurations, userId));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration", method = RequestMethod.GET)
    public MagmaResponse<List<DeviceDTO>> getAllDeviceParameterConfiguration() {
        return new MagmaResponse<>(coreService.getAllDeviceParameterConfigurations());
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.GET)
    public MagmaResponse<Device> getDevice(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(deviceService.findDeviceById(deviceId));
    }


    @RequestMapping(value = "/core/device/protocol-device-summary", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, Integer>> getProtocolSummary() {
        return new MagmaResponse<>(deviceTestService.getProtocolSummary());
    }

    @RequestMapping(value = "/core/device/{device}/data", method = RequestMethod.POST)
    public MagmaResponse<String> postData(@PathVariable("device") String deviceId,
                                          @RequestBody DataHTTP dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(deviceId, dataHTTP));
    }

    @RequestMapping(value = "/core/device/{device}/data-list", method = RequestMethod.POST)
    public MagmaResponse<String> postDataList(@PathVariable("device") String deviceId,
                                              @RequestBody List<DataHTTP> dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessageList(deviceId, dataHTTP));
    }


    @RequestMapping(value = "/core/device/{device}/data-object", method = RequestMethod.POST)
    public MagmaResponse<String> postDataObject(@PathVariable("device") String deviceId,
                                                @RequestBody Object dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(deviceId, dataHTTP));
    }

    @RequestMapping(value = "/core/device/{device}/connectivity", method = RequestMethod.GET)
    public MagmaResponse<Map<Connectivity, Map<String, String>>> getConnectivity(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(coreService.getConnectivityMatrix(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/connectivity", method = RequestMethod.POST)
    public MagmaResponse<Map<Connectivity, Map<String, String>>> updateConnectivity(@PathVariable("device") String deviceId,
                                                                                    @RequestBody Map<Connectivity, Map<String, String>> matrix) {
        return new MagmaResponse<>(coreService.updateConnectivityMatrix(deviceId, matrix));
    }

    @RequestMapping(value = "/core/device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevices(@RequestParam(required = false) List<String> deviceIds,
                                                  @RequestParam(required = false) String batchNumber) {
        if ((deviceIds == null || deviceIds.isEmpty()) && (batchNumber == null || batchNumber.isEmpty())) {
            return new MagmaResponse<>(deviceService.findDevices());
        } else if (batchNumber != null && (deviceIds == null || deviceIds.isEmpty())) {
            return new MagmaResponse<>(deviceService.findDevicesByBatchNumber(batchNumber));
        } else {
            return new MagmaResponse<>(deviceService.findDevicesByIdIn(deviceIds));
        }
    }

    @RequestMapping(value = "/core/user/{userId}/favourite-device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesWithFavouriteOrder(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(deviceService.findDevicesWithFavouriteOrder(userId));
    }

    @RequestMapping(value = "/core/device/ota/upgradable", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesOta() {
        return new MagmaResponse<>(coreService.getDevicesOtaUpgradable());
    }

    @RequestMapping(value = "/core/user/{userId}/device-favourites", method = RequestMethod.POST)
    public MagmaResponse<List<String>> addFavouriteDevices(@PathVariable("userId") String userId,
                                                           @RequestBody List<String> deviceIds) {
        return new MagmaResponse<>(deviceService.addFavouriteDevicesBulk(userId, deviceIds));
    }

    @RequestMapping(value = "/core/user/{userId}/device-favourites", method = RequestMethod.DELETE)
    public MagmaResponse<List<String>> removeFavouriteDevices(@PathVariable("userId") String userId,
                                                              @RequestBody List<String> deviceIds) {
        return new MagmaResponse<>(deviceService.removeFavouriteDevicesBulk(userId, deviceIds));
    }

    @RequestMapping(value = "/core/user/{userId}/device/favourite", method = RequestMethod.GET)
    public MagmaResponse<List<String>> getFavouriteDevices(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(deviceService.findFavouriteDeviceIds(userId));
    }

    @RequestMapping(value = "/core/user/{userId}/device-favourites/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<String> editFavouriteDevices(@PathVariable("deviceId") String deviceId,
                                                      @PathVariable("userId") String userId,
                                                      @RequestParam("action") String action) {
        return new MagmaResponse<>(deviceService.editFavouriteDevices(userId, deviceId, action));
    }

    @RequestMapping(value = "/core/device/{device}/mode", method = RequestMethod.PUT)
    public MagmaResponse<String> setDeviceMode(@PathVariable("device") String deviceId,
                                               @RequestBody String status) {
        return new MagmaResponse<>(deviceService.updateDeviceMode(deviceId, status));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.PUT)
    public MagmaResponse<Device> update(@PathVariable("device") String deviceId,
                                        @RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(deviceService.updateDevice(deviceId, deviceDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/device/{device}", method = RequestMethod.PUT)
    public MagmaResponse<Device> update(@PathVariable("device") String deviceId,
                                        @PathVariable("userId") String userId,
                                        @RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(deviceService.editDevice(deviceId, deviceDTO, userId));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.PATCH)
    public MagmaResponse<String> patch(@PathVariable("device") String deviceId,
                                       @RequestBody Device device) {
        return new MagmaResponse<>(deviceService.patchDevice(deviceId, device));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteDevice(@PathVariable(value = "device", required = false) String deviceId, @RequestParam(value = "batchNumber", required = false) String batchNumber) {
        if (deviceId != null) {
            return new MagmaResponse<>(deviceService.deleteDevice(deviceId));
        } else {
            return new MagmaResponse<>(deviceService.deleteDevicesByBatchNumber(batchNumber));
        }
    }

    @RequestMapping(value = "/core/device/{device}/sensor/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId,
                                                             @PathVariable("sensorNumber") Integer sensorNumber,
                                                             @RequestParam(value = "order", required = false) Direction direction,
                                                             @RequestParam(value = "from", required = false) String from,
                                                             @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(kitService.findSensorHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/device/{device}/sensor/{sensorNumber}/new", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistoryNew(@PathVariable("device") String deviceId,
                                                                @PathVariable("sensorNumber") Integer sensorNumber,
                                                                @RequestParam(value = "order", required = false) Direction direction,
                                                                @RequestParam(value = "from", required = false) String from,
                                                                @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(kitService.findSensorHistoryByKitAndNumberNew(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/device/{device}/sensor/{sensorNumber}", method = RequestMethod.POST)
    public MagmaResponse<Sensor> postSensorsData(@PathVariable("device") String deviceId,
                                                 @PathVariable("sensorNumber") Integer sensorNumber, @RequestBody DataHTTP dataHTTP) {
        return new MagmaResponse<>(kitService.postSensorsData(deviceId, sensorNumber, dataHTTP));
    }

    @RequestMapping(value = "/core/device/{device}/maintenance/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<DeviceMaintenance>> getMaintenanceDataHistory(@PathVariable("device") String deviceId,
                                                                            @PathVariable("sensorNumber") Integer sensorNumber,
                                                                            @RequestParam(value = "order", required = false) Direction direction,
                                                                            @RequestParam(value = "from", required = false) String from,
                                                                            @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(deviceService.findMaintenanceHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/device/{device}/getAllDirectData", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(deviceService.getAllSensorDetailsByDeviceId(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/actuator/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Actuator>> getActuatorDataHistory(@PathVariable("device") String deviceId,
                                                                @PathVariable("sensorNumber") Integer sensorNumber,
                                                                @RequestParam(value = "order", required = false) Direction direction,
                                                                @RequestParam(value = "from", required = false) String from,
                                                                @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(kitService.findActuatorHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

/*    @RequestMapping(value = "/core/kit/{kit}/data", method = RequestMethod.POST)
    public MagmaResponse<String> postData(@PathVariable("kit") String kitId,
                                          @RequestBody DataHTTP dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(kitId, dataHTTP));
    }*/

    @RequestMapping(value = "/core/meta-data", method = RequestMethod.GET)
    public MagmaResponse<MetaData> getProperties() {
        return new MagmaResponse<>(coreService.getProperties());
    }


    @RequestMapping(value = "/core/sensor-failure-value", method = RequestMethod.POST)
    public MagmaResponse<SensorFailureValue> createSensorFailureOfProperties(@RequestBody SensorFailureValue sensorFailureValue) {
        return new MagmaResponse<>(coreService.createSensorFailureEntry(sensorFailureValue));
    }

    @RequestMapping(value = "/core/sensor-failure-value", method = RequestMethod.GET)
    public MagmaResponse<List<SensorFailureValue>> getSensorFailureValuesOfProperties() {
        return new MagmaResponse<>(coreService.getSensorFailureValues());
    }

    @RequestMapping(value = "/core/devices-summary", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, DeviceSummary>> getDevicesSummary() {
        return new MagmaResponse<>(coreService.getDevicesSummaryCall());
    }

    //DeviceTest-Related Apis


    @RequestMapping(value = "/core/deviceTest/batchNumbers", method = RequestMethod.GET)
    public MagmaResponse<List> getAllBatchNumbers() {
        return new MagmaResponse<>(deviceTestService.getAllDevicesBatchNumbers());
    }

    @RequestMapping(value = "/core/batchNumber/{batchNumber}/device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getAllDevicesByBatchNumber(@PathVariable("batchNumber") String batchNumber) {
        return new MagmaResponse<>(deviceTestService.getDevicesOfBatch(batchNumber));
    }

    @RequestMapping(value = "/core/deviceTest/configureBatchNumber/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<Device> configureBatchNumber(@RequestBody String batchNumber, @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(deviceTestService.configureBatchNumber(deviceId, batchNumber));
    }

    @RequestMapping(value = "/core/device/{deviceId}/property/{propertyId}/{label}", method = RequestMethod.PUT)
    public MagmaResponse<Property> changeLabel(@PathVariable("label") String label,
                                               @PathVariable("propertyId") String propertyId,
                                               @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeLabel(deviceId, label, propertyId));
    }

    @RequestMapping(value = "/core/device/{deviceId}/property/changeLabels", method = RequestMethod.PUT)
    public MagmaResponse<List<Property>> changePropertyLabelBulk(@RequestBody List<PropertyDTO> propertyDTOS,
                                                                 @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changePropertyLabels(deviceId, propertyDTOS));
    }

    @RequestMapping(value = "/core/device/{deviceId}/sensor/changeLabels", method = RequestMethod.PUT)
    public MagmaResponse<List<Sensor>> changeSensorLabelBulk(@RequestBody SensorChangeRequestDTO SensorChangeRequestDTO,
                                                             @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeSensorLabels(deviceId, SensorChangeRequestDTO));
    }

    //Connectivity API

    @RequestMapping(value = "/core/connectivity/protocol", method = RequestMethod.GET)
    public MagmaResponse<Map<String, Integer>> getConnectivityProtocolDetails() {
        return new MagmaResponse<>(coreService.getConnectivityProtocolDetails());
    }

    @RequestMapping(value = "/core/connectivity/protocol/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<Map<String, Protocol>> changeConnectivityProtocol(@RequestParam(value = "protocol", required = true) Protocol protocol,
                                                                           @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeConnectivityProtocol(deviceId, protocol));
    }

    @RequestMapping(value = "/core/connectivity/protocol/status", method = RequestMethod.PUT)
    public MagmaResponse<String> enableAndDisableProtocol(@RequestBody List<Map<String, String>> protocolStatusConfigurations) {
        return new MagmaResponse<>(coreService.changeStatusOfTheProtocol(protocolStatusConfigurations));
    }

    @RequestMapping(value = "/core/configureClient", method = RequestMethod.PUT)
    public MagmaResponse<List<Device>> configureClient(@RequestBody Map<String, String> configs) {
        return new MagmaResponse<>(coreService.configureClients(configs));
    }

    @RequestMapping(value = "/core/connectivity", method = RequestMethod.GET)
    public MagmaResponse<List<Map<String, String>>> getClientsProtocolDetails() {
        return new MagmaResponse<>(coreService.getClientsDeviceConnectivity());
    }

    @RequestMapping(value = "/core/connectivity/{client}", method = RequestMethod.GET)
    public MagmaResponse<List<Map<String, String>>> getClientProtocolDetails(@PathVariable("client") String client) {
        return new MagmaResponse<>(coreService.getClientDeviceConnectivity(client));
    }

    @RequestMapping(value = "/core/connectivity/connection/{deviceId}", method = RequestMethod.GET)
    public MagmaResponse<Map<String, String>> getConnectionDetailsOfDevice(@PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.getConnectionDetailsOfDevice(deviceId));
    }

    @RequestMapping(value = "/core/connectivity/credentials", method = RequestMethod.POST)
    public MagmaResponse<Map<String, String>> generateConnectivityCredentials(@RequestBody Map<String, String> connectivityDetails) {
        return new MagmaResponse<>(coreService.generateConnectivityCredentials(connectivityDetails));
    }

    @RequestMapping(value = "/core/kitType", method = RequestMethod.POST)
    public MagmaResponse<TypeOfKit> createKitType(@RequestBody @Valid TypeOfKitDTO typeOfKitDTO,
                                                  BindingResult result) {
        if (result.hasErrors()) {
            throw new com.magma.dmsdata.validation.BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(coreService.createKitType(typeOfKitDTO));
    }

    @RequestMapping(value = "/core/kitType", method = RequestMethod.GET)
    public MagmaResponse<List<TypeOfKit>> getAllKitType() {
        return new MagmaResponse<>(coreService.getAllKitType());
    }

    @RequestMapping(value="/core/sensorCodes", method = RequestMethod.POST)
    public MagmaResponse<List<SensorCode>> addSensorCode(
            @RequestBody List<SensorCode> sensorCodes){
        return new MagmaResponse<>(coreService.addSensorCodes(sensorCodes));
    }
    @RequestMapping(value="/core/sensorCodes", method = RequestMethod.GET)
    public MagmaResponse<List<SensorCode>> getSensorCodes(){
        return new MagmaResponse<>(coreService.getAllSensorCodes());
    }
}
