package com.magma.core.api;

import com.magma.core.data.dto.*;
import com.magma.core.data.entity.*;
import com.magma.core.data.support.*;
import com.magma.core.gateway.HTTPMessageHandler;
import com.magma.core.job.CoreSchedule;
import com.magma.core.service.CoreService;
import com.magma.core.service.DataProcessorService;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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


    //Device-Related Apis
    @RequestMapping(value = "/core/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(coreService.create(deviceDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody DeviceDTO deviceDTO,
                                              @PathVariable("userId") String userId) {
        return new MagmaResponse<>(coreService.createDevice(deviceDTO, userId));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration", method = RequestMethod.POST)
    public MagmaResponse<List<Device>> createDeviceParameterConfiguration(@RequestBody DeviceParameterConfigurationDTO deviceParameterConfiguration) {
        return new MagmaResponse<>(coreService.addDeviceParameterConfiguration(deviceParameterConfiguration));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration/{device}", method = RequestMethod.PUT)
    public MagmaResponse<String> updateDeviceParameterConfiguration(
            @PathVariable("device") String deviceId,
            @RequestBody DeviceParameterConfiguration deviceParameterConfiguration) {
        return new MagmaResponse<>(coreService.updateDeviceParameterConfiguration(deviceId, deviceParameterConfiguration));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration/bulk", method = RequestMethod.PUT)
    public MagmaResponse<String> updateDeviceParameterConfiguration(
            @RequestBody List<DeviceParameterConfiguration> deviceParameterConfigurations) {
        return new MagmaResponse<>(coreService.bulkUpdateDeviceParameterConfiguration(deviceParameterConfigurations));
    }

    @RequestMapping(value = "/core/device/deviceParameterConfiguration", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getAllDeviceParameterConfiguration() {
        return new MagmaResponse<>(coreService.getAllDeviceParameterConfigurations());
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.GET)
    public MagmaResponse<Device> getDevice(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(coreService.findDeviceById(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/data", method = RequestMethod.POST)
    public MagmaResponse<String> postData(@PathVariable("device") String deviceId,
                                          @RequestBody DataHTTP dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(deviceId, dataHTTP));
    }

    @RequestMapping(value = "/core/device/{device}/connectivity", method = RequestMethod.GET)
    public MagmaResponse<Map> getConnectivity(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(coreService.getConnectivityMatrix(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/connectivity", method = RequestMethod.POST)
    public MagmaResponse<Map> updateConnectivity(@PathVariable("device") String deviceId,
                                                 @RequestBody Map<Connectivity, Map<String, String>> matrix) {
        return new MagmaResponse<>(coreService.updateConnectivityMatrix(deviceId, matrix));
    }

    @RequestMapping(value = "/core/device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevices() {
        return new MagmaResponse<>(coreService.findDevices());
    }

    @RequestMapping(value = "/core/user/{userId}/device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesWithFavouriteOrder(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(coreService.findDevicesWithFavouriteOrder(userId));
    }

    @RequestMapping(value = "/core/user/{userId}/device/favourite", method = RequestMethod.GET)
    public MagmaResponse<List<String>> getFavouriteDevices(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(coreService.findFavouriteDeviceIds(userId));
    }

    @RequestMapping(value = "/core/device/{device}/persistence", method = RequestMethod.PUT)
    public MagmaResponse<String> setPersistence(@PathVariable("device") String deviceId,
                                                @RequestBody Device device) {
        return new MagmaResponse<>(coreService.updatePersistence(deviceId, device.getPersistence()));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.PUT)
    public MagmaResponse<Device> update(@PathVariable("device") String deviceId,
                                        @RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(coreService.updateDevice(deviceId, deviceDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/device/{device}", method = RequestMethod.PUT)
    public MagmaResponse<Device> update(@PathVariable("device") String deviceId,
                                        @PathVariable("userId") String userId,
                                        @RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(coreService.editDevice(deviceId, deviceDTO, userId));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.PATCH)
    public MagmaResponse<String> patch(@PathVariable("device") String deviceId,
                                       @RequestBody Device device) {
        return new MagmaResponse<>(coreService.patchDevice(deviceId, device));
    }

    @RequestMapping(value = "/core/device/{device}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteDevice(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(coreService.deleteDevice(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/sensor/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId,
                                                             @PathVariable("sensorNumber") Integer sensorNumber,
                                                             @RequestParam(value = "order", required = false) Direction direction,
                                                             @RequestParam(value = "from", required = false) String from,
                                                             @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.findSensorHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/device/{device}/getAllDirectData", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(coreService.getAllSensorDetailsByDeviceId(deviceId));
    }

    ;

    @RequestMapping(value = "/core/device/{device}/actuator/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Actuator>> getActuatorDataHistory(@PathVariable("device") String deviceId,
                                                                @PathVariable("sensorNumber") Integer sensorNumber,
                                                                @RequestParam(value = "order", required = false) Direction direction,
                                                                @RequestParam(value = "from", required = false) String from,
                                                                @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.findActuatorHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/meta-data", method = RequestMethod.GET)
    public MagmaResponse<MetaData> getProperties() {
        return new MagmaResponse<>(coreService.getProperties());
    }

    @RequestMapping(value = "/core/device/{deviceId}/property/{propertyId}/{label}", method = RequestMethod.PUT)
    public MagmaResponse<Property> changeLabel(@PathVariable("label") String label, @PathVariable("propertyId") String propertyId, @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeLabel(deviceId, label, propertyId));
    }
}
