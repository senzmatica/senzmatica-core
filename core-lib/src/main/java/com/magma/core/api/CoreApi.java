package com.magma.core.api;

import com.magma.core.data.dto.DeviceDTO;
import com.magma.core.data.dto.DeviceParameterConfigurationDTO;
import com.magma.core.data.dto.PropertyDTO;
import com.magma.core.data.dto.TypeOfKitDTO;
import com.magma.core.data.entity.*;
import com.magma.core.data.support.*;
import com.magma.core.gateway.HTTPMessageHandler;
import com.magma.core.job.CoreSchedule;
import com.magma.core.service.*;
import com.magma.util.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
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

    @Autowired
    DeviceTestService deviceTestService;

    @Autowired
    DeviceTestAutomation deviceTestAutomation;

    @Autowired
    DeviceService deviceService;

    @Autowired
    KitCoreService kitService;

    @RequestMapping(value = "/core/memory-status", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, Long>> getMemoryStatistics() {
        HashMap<String, Long> stats = new HashMap<>();
        stats.put("HeapSize", Runtime.getRuntime().totalMemory());
        stats.put("HeapMaxSize", Runtime.getRuntime().maxMemory());
        stats.put("HeapFreeSize", Runtime.getRuntime().freeMemory());
        return new MagmaResponse<>(stats);
    }

    @RequestMapping(value = "/core/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody DeviceDTO deviceDTO) {
        return new MagmaResponse<>(deviceService.create(deviceDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/device", method = RequestMethod.POST)
    public MagmaResponse<Device> createDevice(@RequestBody DeviceDTO deviceDTO,
                                              @PathVariable("userId") String userId) {
        return new MagmaResponse<>(deviceService.createDevice(deviceDTO, userId));
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


    @RequestMapping(value = "/core/device/{device}/data-object", method = RequestMethod.POST)
    public MagmaResponse<String> postDataObject(@PathVariable("device") String deviceId,
                                                @RequestBody Object dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(deviceId, dataHTTP));
    }

    @RequestMapping(value = "/core/kit/{kitId}/manualData", method = RequestMethod.POST)
    public MagmaResponse<Property> postManualData(@PathVariable("kitId") String kitId,
                                                  @RequestBody PropertyDTO propertyDTO) {
        return new MagmaResponse<>(dataProcessorService.handleManualData(kitId, propertyDTO));
    }

    @RequestMapping(value = "/core/kit/{kitId}/manualDataBulk", method = RequestMethod.POST)
    public MagmaResponse<List<Property>> postManualDataBulk(@PathVariable("kitId") String kitId,
                                                            @RequestBody List<PropertyDTO> propertyDTO) {
        return new MagmaResponse<>(dataProcessorService.handleManualDataBulk(kitId, propertyDTO));
    }

    @RequestMapping(value = "/core/kit/{kitId}/storePropertyFromImage", method = RequestMethod.POST)
    public MagmaResponse<Property> storeDataFromImage(@PathVariable("kitId") String kitId, @RequestBody PropertyDTO propertyDTO) {
        return new MagmaResponse<>(dataProcessorService.storePropertyFromS3Image(kitId, propertyDTO));
    }

    @RequestMapping(value = "/core/kit/{kitId}/getPropertyFromImage", method = RequestMethod.POST)
    public MagmaResponse<Double> getDataFromImage(@PathVariable("kitId") String kitId, @RequestBody String imageURL) {
        return new MagmaResponse<>(dataProcessorService.getPropertyFromS3Image(kitId, imageURL));
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
    public MagmaResponse<List<Device>> getDevices() {
        return new MagmaResponse<>(deviceService.findDevices());
    }

    @RequestMapping(value = "/core/user/{userId}/device", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesWithFavouriteOrder(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(deviceService.findDevicesWithFavouriteOrder(userId));
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

    @RequestMapping(value = "/core/device/{device}/persistence", method = RequestMethod.PUT)
    public MagmaResponse<String> setPersistence(@PathVariable("device") String deviceId,
                                                @RequestBody Device device) {
        return new MagmaResponse<>(deviceService.updatePersistence(deviceId, device.getPersistence()));
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
    public MagmaResponse<String> deleteDevice(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(deviceService.deleteDevice(deviceId));
    }

    @RequestMapping(value = "/core/device/{device}/sensor/{sensorNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId,
                                                             @PathVariable("sensorNumber") Integer sensorNumber,
                                                             @RequestParam(value = "order", required = false) Direction direction,
                                                             @RequestParam(value = "from", required = false) String from,
                                                             @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(kitService.findSensorHistoryByKitAndNumber(deviceId, sensorNumber, direction, from, to));
    }

    @RequestMapping(value = "/core/device/{device}/getAllDirectData", method = RequestMethod.GET)
    public MagmaResponse<List<Sensor>> getSensorsDataHistory(@PathVariable("device") String deviceId) {
        return new MagmaResponse<>(deviceService.getAllSensorDetailsByDeviceId(deviceId));
    }

    ;

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

    @RequestMapping(value = "/core/kit/{kit}/property/{propertyNumber}/alert-limit", method = RequestMethod.POST)
    public MagmaResponse<AlertLimit> createPropertyAlertLimit(@PathVariable("kit") String kitId,
                                                              @PathVariable("propertyNumber") Integer propertyNumber,
                                                              @RequestBody AlertLimit alertLimit) {
        return new MagmaResponse<>(coreService.createOrUpdateAlertLimit(kitId, propertyNumber, alertLimit));
    }

    @RequestMapping(value = "/core/meta-data", method = RequestMethod.GET)
    public MagmaResponse<MetaData> getProperties() {
        return new MagmaResponse<>(coreService.getProperties());
    }

    @RequestMapping(value = "/core/kit/{kit}/graph-kit-history", method = RequestMethod.GET)
    public MagmaResponse<LinkedList<HashMap<String, Object>>> getGraphDataOfKit(@PathVariable("kit") String kitId,
                                                                                @RequestParam(value = "from", required = false) String from,
                                                                                @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.getGraphDataOfKit(kitId, from, to));
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

    @RequestMapping(value = "/core/kit/{kit}/properties", method = RequestMethod.POST)
    public MagmaResponse<String> createProperties(@PathVariable("kit") String kitId,
                                                  @RequestBody List<Property> properties) {
        return new MagmaResponse<>(coreService.createProperties(kitId, properties));
    }

    @RequestMapping(value = "/core/kit/{kit}/device/{device}/property/{propertyNumber}", method = RequestMethod.PUT)
    public MagmaResponse<String> populatePropertiesHistory(@PathVariable("kit") String kitId,
                                                           @PathVariable("device") String deviceId,
                                                           @PathVariable("propertyNumber") Integer propertyNumber,
                                                           @RequestParam(value = "from", required = false) String from,
                                                           @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.populateProperty(kitId, deviceId, propertyNumber, from, to));
    }

    @RequestMapping(value = "/core/kit/{kit}/device/{device}/property/{propertyNumber}/{model}", method = RequestMethod.PUT)
    public MagmaResponse<List<Property>> populatePropertyWithModel(@PathVariable("kit") String kitId,
                                                                   @PathVariable("device") String deviceId,
                                                                   @PathVariable("propertyNumber") Integer propertyNumber,
                                                                   @PathVariable("model") String model,
                                                                   @RequestParam(value = "from", required = false) String from,
                                                                   @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.populatePropertyWithModel(kitId, deviceId, propertyNumber, model, from, to));
    }

    @RequestMapping(value = "/checkNewMails", method = RequestMethod.GET)
    public String checkNewMails() {
        coreSchedule.sendMaintenanceAlert();
        return "Mails send";
    }

    //DeviceTest-Related Apis


    @RequestMapping(value = "/core/deviceTest/batchNumbers", method = RequestMethod.GET)
    public MagmaResponse<List> getAllBatchNumbers() {
        return new MagmaResponse<>(deviceTestService.getAllDevicesBatchNumbers());
    }

    @RequestMapping(value = "/core/deviceTest/configureBatchNumber/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<Device> configureBatchNumber(@RequestBody Integer batchNumber, @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(deviceTestService.configureBatchNumber(deviceId, batchNumber));
    }

    @RequestMapping(value = "/core/deviceTest/testcase/batch/{batchNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<TestCase>> getAllTestcases(@PathVariable("batchNumber") Integer batchNumber) {
        return new MagmaResponse<>(deviceTestService.getTestcasesOfBatch(batchNumber));
    }

    @RequestMapping(value = "/core/deviceTest/subTestcase/batch/{batchNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<SubTestCase>> getAllSubTestcases(@PathVariable("batchNumber") Integer batchNumber) {
        return new MagmaResponse<>(deviceTestService.getSubTestcasesOfBatch(batchNumber));
    }

    @RequestMapping(value = "/core/deviceTest/subTestcase/batch/{batchNumber}/{subTestcaseId}", method = RequestMethod.GET)
    public MagmaResponse<SubTestCase> getSubTestcaseOfBatch(@PathVariable("batchNumber") Integer batchNumber,
                                                            @PathVariable("subTestcaseId") String subTestcaseId) {
        return new MagmaResponse<>(deviceTestService.getSubTestcaseOfBatch(batchNumber, subTestcaseId));
    }

//    @RequestMapping(value = "/core/deviceTest/configure/{batchNumber}/testcase/{mainTestcaseId}/subTestcase/{subTestcaseId}", method = RequestMethod.PUT)
//    public MagmaResponse<TestCase> configureSubTestCase(@PathVariable("batchNumber") Integer batchNumber,
//                                                        @PathVariable("mainTestcaseId") String testcaseId,
//                                                        @PathVariable("subTestcaseId") String subTestcaseId,
//                                                        @RequestBody SubTestCase subTestCase) {
//        return new MagmaResponse<>(deviceTestService.configureSubTestcaseOfBatch(batchNumber, testcaseId, subTestcaseId, subTestCase));
//    }

//    @RequestMapping(value = "/core/deviceTest/configure/deviceConfiguration/{batchNumber}", method = RequestMethod.POST)
//    public MagmaResponse<String> deviceTestConfiguration(@PathVariable("batchNumber") Integer batchNumber,
//                                                         @RequestBody DeviceConfiguration configuration) {
//        return new MagmaResponse<>(deviceTestService.deviceConfiguration(batchNumber, configuration));
//    }

//    @RequestMapping(value = "/core/deviceTest/devicesTestSummary/{batchNumber}", method = RequestMethod.GET)
//    public MagmaResponse<TestSummary> getTestSummaryOfBatch(@PathVariable("batchNumber") Integer batchNumber) {
//        return new MagmaResponse<>(deviceTestService.getTestSummaryForBatchOfDevices(batchNumber));
//    }

    @RequestMapping(value = "/core/deviceTest/removeSensors/{batchNumber}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteTestSensorsOfBatch(@PathVariable("batchNumber") Integer batchNumber) {
        return new MagmaResponse<>(deviceTestService.clearTestSensorsOfBatch(batchNumber));
    }

    @RequestMapping(value = "/core/deviceTest/testConditions", method = RequestMethod.GET)
    public MagmaResponse<List<String>> getAllTestConditions() {
        return new MagmaResponse<>(deviceTestService.getAllTestConditions());
    }

    @RequestMapping(value = "/core/device/{deviceId}/property/{propertyId}/{label}", method = RequestMethod.PUT)
    public MagmaResponse<Property> changeLabel(@PathVariable("label") String label,
                                               @PathVariable("propertyId") String propertyId,
                                               @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeLabel(deviceId, label, propertyId));
    }

    @RequestMapping(value = "/core/device/{deviceId}/property/changeLabels", method = RequestMethod.PUT)
    public MagmaResponse<List<Property>> changeLabelBulk(@RequestBody List<PropertyDTO> propertyDTOS,
                                                         @PathVariable("deviceId") String deviceId) {
        return new MagmaResponse<>(coreService.changeLabels(deviceId, propertyDTOS));
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
    public MagmaResponse<TypeOfKit> createKitType(@RequestBody @Valid TypeOfKitDTO typeOfKitDTO, BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(coreService.createKitType(typeOfKitDTO));
    }

    @RequestMapping(value = "/core/kitType", method = RequestMethod.GET)
    public MagmaResponse<List<TypeOfKit>> getAllKitType() {
        return new MagmaResponse<>(coreService.getAllKitType());
    }

    @RequestMapping(value = "/core/deviceTest/setupTesting", method = RequestMethod.POST)
    public MagmaResponse<TestCase> setUpTesting(
            @RequestBody TestCase testCase) {
        return new MagmaResponse<>(deviceTestAutomation.createNewTest(testCase));
    }

    @RequestMapping(value = "/core/deviceTest/setupParameter/{testCaseId}", method = RequestMethod.PUT)
    public MagmaResponse<TestCase> setUpParameters(
            @PathVariable("testCaseId") String testCaseId,
            @RequestBody SubTestCase testCases) {
        return new MagmaResponse<>(deviceTestAutomation.editSubTestCaseParameters(testCaseId, testCases));
    }

    @RequestMapping(value = "/core/deviceTest/testReportTable/{batchNo}", method = RequestMethod.GET)
    public MagmaResponse<List<Map<String, Map<String, Map<String, List<TestCard>>>>>> testReportTable(
            @PathVariable("batchNo") Integer batchNo,
            @RequestParam(value = "testCaseTitle", required = true) String testCaseTitle) {
        return new MagmaResponse<>(deviceTestAutomation.getSummaryTable(batchNo, testCaseTitle));
    }
}
