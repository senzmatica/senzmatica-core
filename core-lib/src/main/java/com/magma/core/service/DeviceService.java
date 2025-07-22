package com.magma.core.service;

import com.magma.core.data.dto.DeviceDTO;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.ProductVersion;
import com.magma.core.data.support.UserInfo;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import com.magma.core.util.ProductStatus;
import com.magma.util.MagmaTime;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeviceService {

    @Value("${device.data.interval}")
    private Integer interval;

    @Autowired
    UserConnectorService userConnectorService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    KitRepository kitRepository;

    @Autowired
    CorporateConnectorService corporateConnectorService;

    @Autowired
    UserFavouriteService userFavouriteService;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    KitCoreService kitService;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    DeviceMaintenanceRepository deviceMaintenanceRepository;

    @Autowired
    DeviceTestService deviceTestService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);

    public Device create(DeviceDTO deviceDTO) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);

        return deviceCreateLogic(device,deviceDTO.getProductId());
    }

    public Device createDevice(DeviceDTO deviceDTO, String userId) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        UserInfo createdBy = new UserInfo();
        UserInfo user = userConnectorService.findUser(userId);

        BeanUtils.copyProperties(deviceDTO, device);
        if(user != null) {
            BeanUtils.copyProperties(user, createdBy);
            device.setCreatedBy(createdBy);
        }

        return deviceCreateLogic(device,null);
    }

    private Device deviceCreateLogic(Device device, String productId) {

        if (!device.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        if (deviceRepository.findOne(device.getId()) != null) {
            throw new MagmaException(MagmaStatus.DEVICE_ID_ALREADY_EXISTS);
        }
        if (!deviceRepository.findByName(device.getName()).isEmpty()) {
            throw new MagmaException(MagmaStatus.DEVICE_NAME_ALREADY_EXISTS);
        }

        if (device.getInterval() == null) {
            device.setInterval(interval);
        }

        if (device.getMaintain() == null) {
            device.setMaintain(false);
        }

        if (device.getStatus() == null) {
            device.setStatus(Status.ACTIVE);
        }

        if (device.getBattery() == null) {
            device.setBattery(new Battery(240.0, 300.0));
        }

        if (!device.getShiftMap().isEmpty()) {
            device.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
        }
        device.setBatchNumber(device.getBatchNumber());
        device.setCreationDate(new DateTime());

        //If device is added via setup senzmatica then need to set product Object to device with empty version details
        if(productId!=null){
            ProductType productTypes = productTypeRepository.findOne(productId);
            if(productTypes!=null){
                ProductData product = new ProductData();
                product.setProductId(productTypes.getId());
                product.setProductType(productTypes.getProductName()); // here for now product type is used in ProductData class. we may need to change that to productName in future
                product.setCurrentProductVersion("0.0.0");
                List<String> availableVersionsInProduct = new ArrayList<>();
                for (ProductVersion productVersion : productTypes.getVersions()) {
                    if (productVersion.getVersionNum().equals("0.0.0")) {
                        availableVersionsInProduct.add(productVersion.getVersionNum());
                    }
                }
                product.setAvailableProductVersions(availableVersionsInProduct.toString() );
                device.setProduct(product);
            }
        }


        return deviceRepository.save(device);
    }

    public List<Device> findDevices() {
        LOGGER.debug("Find all Devices request found");

        List<Device> allDevices = new ArrayList<>();
        List<Device> devices = deviceRepository.findAll();
        devices.forEach((d) -> {
            Kit kit = kitRepository.findByDevices(d.getId());
            if (kit != null) {
                d.setReferenceName(corporateConnectorService.referenceName(kit.getId()));
            }
            //Adding sensor codes,Actuator codes,persistence, protocol and connectivity if   device is added under senzmatica 3.0
            // dont un comment this, store these details to db when creating a device
//            if(d.getProductType()!=null){
//                ProductTypes deviceProductType = productTypesRepository.findOne(d.getProductType());
//                d.setSensorCodes(deviceProductType.getSensorCodes());
//                d.setActuatorCodes(deviceProductType.getActuatorCodes());
//                d.setPersistence(deviceProductType.isPersistence());
//                d.setProtocol(deviceProductType.getProtocol());
//                d.setConnectivity(deviceProductType.getConnectivity());
//            }
            if(d.getStatus()==null){
                d.setStatus(Status.ACTIVE);
            }
            allDevices.add(d);
        });
        return devices;
    }

    public List<Device> findDevicesWithFavouriteOrder(String userId) {
        LOGGER.debug("Find all Devices With Favourite Order request found For User:{}", userId);
        return userFavouriteService.getAllDevicesWithFavouriteOrder(userId);
    }

    public List<String> findFavouriteDeviceIds(String userId) {
        LOGGER.debug("Find Ids of Favourite devices - For User:{}", userId);
        return userFavouriteService.getUserFavouriteDevices(userId);
    }

    public List<String> addFavouriteDevicesBulk(String userId, List<String> deviceIds) {
        LOGGER.debug("Add Bulk FavouriteDevices for a user -{} , deviceIds -{}", userId, deviceIds);
        return userFavouriteService.addBulkDevicesAsFavourite(userId, deviceIds);
    }

    public List<String> removeFavouriteDevicesBulk(String userId, List<String> deviceIds) {
        LOGGER.debug("Remove Bulk FavouriteDevices for a user -{} , deviceIds -{}", userId, deviceIds);
        return userFavouriteService.removeDevicesFromFavourite(userId, deviceIds);
    }

    public String editFavouriteDevices(String userId, String deviceId, String action) {
        LOGGER.debug("{} Favourite Device  Request For User:{},Device Id:{}", action.toUpperCase(Locale.ROOT), userId, deviceId);

        if (action.equals("add")) {
            userFavouriteService.updateDeviceAsFavourite(userId, deviceId);
            return "Successfully added as favourite";

        } else if (action.equals("remove")) {
            userFavouriteService.RemoveDeviceAsFavourite(userId, deviceId);
            return "Successfully removed from favourite";
        } else {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    public Device findDeviceById(String deviceId) {
        LOGGER.debug("Find Device request found : {}", deviceId);
        Device device = deviceRepository.findOne(deviceId);
        if (device == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        //Adding sensor codes,Actuator codes,persistence, protocol and connectivity if   device is added under senzmatica 3.0
        if (device.getProductType() != null) {
            ProductType deviceProductType = productTypeRepository.findByProductName(device.getProductType());
            if (deviceProductType == null) {
                throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND); //Change status
            }
            device.setSensorCodes(deviceProductType.getSensorCodes());
            device.setActuatorCodes(deviceProductType.getActuatorCodes());
            device.setPersistence(deviceProductType.isPersistence());
            device.setProtocol(deviceProductType.getProtocol());
            device.setConnectivity(deviceProductType.getConnectivity());
        }
        if(device.getStatus()==null){
            device.setStatus(Status.ACTIVE);
        }
        return device;
    }

    public List<Device> findDevicesByBatchNumber(String batchNumber) {
        LOGGER.debug("Find Devices request found : {}", batchNumber);
        return deviceRepository.findByBatchNumber(batchNumber);
    }

    public List<Device> findDevicesByIdIn(List<String> ids) {
        LOGGER.debug("Find Devices request found : {}", ids);
        return deviceRepository.findByIdIn(ids);
    }

    public String deleteDevicesByBatchNumber(String BatchNumber) {
        LOGGER.debug("Delete Devices request found : {}", BatchNumber);
        List<Device> devices = deviceRepository.findByBatchNumber(BatchNumber);
        deviceRepository.delete(devices);
        return "Successfully Deleted";
    }

    public List<Device> findDevicesByIdInAndOfflineIsTrue(List<String> ids) {
        LOGGER.debug("Find Devices request found : {}", ids);
        return deviceRepository.findByIdInAndOfflineIsTrue(ids);
    }

    public Device updateDevice(String deviceId, DeviceDTO deviceDTO) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, deviceDTO);

        Device db = findDeviceById(deviceId);

        return deviceUpdateLogic(db, deviceDTO);
    }

    public Device editDevice(String deviceId, DeviceDTO deviceDTO, String userId) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, deviceDTO);

        Device db = findDeviceById(deviceId);
        UserInfo user = userConnectorService.findUser(userId);
        UserInfo modifiedBy = new UserInfo();

        if(user != null) {
            BeanUtils.copyProperties(user, modifiedBy);
            db.setModifiedBy(modifiedBy);
        }

        return deviceUpdateLogic(db, deviceDTO);
    }

    public Device deviceUpdateLogic(Device db, DeviceDTO deviceDTO) {
        if (!deviceDTO.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (MagmaUtil.validate(deviceDTO.getName())) {
            db.setName(deviceDTO.getName());
        } else {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (MagmaUtil.validate(deviceDTO.getDescription())) {
            db.setDescription(deviceDTO.getDescription());
        }

        if (deviceDTO.getStatus() != null) {
            db.setStatus(deviceDTO.getStatus());
        }

        if (deviceDTO.getInterval() != null) {
            db.setInterval(deviceDTO.getInterval());
        }

        if (deviceDTO.getMaintain() != null) {
            db.setMaintain(deviceDTO.getMaintain());
        }

        if (deviceDTO.getBattery() == null) {
            db.setBattery(new Battery(240.0, 300.0));
        }

        if (deviceDTO.getMetaData() != null && !deviceDTO.getMetaData().isEmpty()) {
            updateMetaData(db, deviceDTO.getMetaData());
        }

        if (deviceDTO.getPersistence() != null) {
            db.setPersistence(deviceDTO.getPersistence());
        }

        if (deviceDTO.getSensorCodes() != null) {
            db.setSensorCodes(deviceDTO.getSensorCodes());
            db.setNoOfSensors(deviceDTO.getSensorCodes().length);
        }

        if (deviceDTO.getActuatorCodes() != null) {
            db.setActuatorCodes(deviceDTO.getActuatorCodes());
            db.setNoOfActuators(deviceDTO.getActuatorCodes().length);
        }

        if (deviceDTO.getProtocol() != null) {
            db.setProtocol(deviceDTO.getProtocol());
        }

        if (deviceDTO.getConnectivity() != null) {
            db.setConnectivity(deviceDTO.getConnectivity());
        }

        if (deviceDTO.getBatchNumber() != null) {
            db.setBatchNumber(deviceDTO.getBatchNumber());
        }

        if (deviceDTO.getTemperatureUnit() != null) {
            db.setTemperatureUnit(deviceDTO.getTemperatureUnit());
        }
        if (deviceDTO.getRemoteConfigTopic() != null) {
            db.setRemoteConfigTopic(deviceDTO.getRemoteConfigTopic());
        }
        if (deviceDTO.getRemoteConfigAckTopic() != null) {
            db.setRemoteConfigAckTopic(deviceDTO.getRemoteConfigAckTopic());
        }
        if (deviceDTO.getOtaRequestTopic() != null) {
            db.setOtaRequestTopic(deviceDTO.getOtaRequestTopic());
        }
        if (deviceDTO.getOtaAckTopic() != null) {
            db.setOtaAckTopic(deviceDTO.getOtaAckTopic());
        }

        if (!deviceDTO.getShiftMap().isEmpty()) {
            deviceDTO.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
            db.setShiftMap(deviceDTO.getShiftMap());
        }

        return deviceRepository.save(db);
    }

    private void updateMetaData(Device device, Map<String, String> metaData) {
        if (device.getMetaData() == null) {
            device.setMetaData(metaData);
        }
        device.getMetaData().putAll(metaData);
    }

//    public String updatePersistence(String kitId, Boolean persistence) {
//        LOGGER.debug("Persistence request found Kit : {}, Persistence : {}", kitId, persistence);
//
//        if (persistence == null) {
//            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
//        }
//
//        Kit db = kitService.findKitById(kitId);
//        db.setPersistence(persistence);
//
//        //TODO: Can update via MongoTemplate
//        List<Device> devices = deviceRepository.findByIdIn(db.getDevices());
//        devices.stream().forEach(device -> device.setPersistence(persistence));
//        deviceRepository.saveAll(devices);
//
//        kitRepository.save(db);
//
//        return "Successfully Updated";
//    }

    public String updateDeviceMode(String deviceId, String status) {
        LOGGER.debug("device mode update request found Device : {}, status : {}", deviceId, status);

        if (status == null) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Device deviceDb = deviceRepository.findOne(deviceId);
        if (deviceDb != null) {
            deviceDb.setStatus(Status.valueOf(status));
        }

        deviceRepository.save(deviceDb);
        Kit kitWithDevice=kitRepository.findByDevices(deviceId);
        if(kitWithDevice!=null){
            List<String> devicesInKit=kitWithDevice.getDevices();

            boolean activeDeviceInKit = devicesInKit.stream()
                    .map(deviceRepository::findOne)
                    .map(optionalDevice -> optionalDevice)
                    .filter(Objects::nonNull)
                    .anyMatch(device -> device.getStatus() != Status.INACTIVE);
            if (activeDeviceInKit){
                kitWithDevice.setStatus(Status.ACTIVE);}
            else {
                kitWithDevice.setStatus(Status.INACTIVE);
            }
            kitRepository.save(kitWithDevice);}

        return "Device mode successfully Updated";
    }

    public String deleteDevice(String deviceId) {
        LOGGER.debug("Delete request found DeviceId : {}", deviceId);
        deviceRepository.delete(deviceId);
        return "Successfully Deleted";
    }

    public List<Sensor> getAllSensorDetailsByDeviceId(String deviceId) {
        Device requestedDevice = deviceRepository.findOne(deviceId);
        if (requestedDevice == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        return sensorRepository.findByDeviceId(deviceId);
    }

    public String patchDevice(String deviceId, Device device) {
        LOGGER.debug("Update request found DeviceId : {}, Device : {}", deviceId, device);

        device.setId(deviceId);

        Device db = findDeviceById(deviceId);

        if (MagmaUtil.validate(device.getName())) {
            db.setName(device.getName());
        }

        if (MagmaUtil.validate(device.getDescription())) {
            db.setDescription(device.getDescription());
        }

        if (device.getStatus() != null) {
            db.setStatus(device.getStatus());
        }

        if (device.getInterval() != null) {
            db.setInterval(device.getInterval());
        }

        if (device.getMaintain() != null) {
            db.setMaintain(device.getMaintain());
        }

        if (device.getBattery() == null) {
            db.setBattery(new Battery(240.0, 300.0));
        }

        if (device.getPersistence() != null) {
            db.setPersistence(device.getPersistence());
        }

        if (device.getSensorCodes() != null) {
            db.setSensorCodes(device.getSensorCodes());
            db.setNoOfSensors(device.getSensorCodes().length);
        }

        if (device.getActuatorCodes() != null) {
            db.setActuatorCodes(device.getActuatorCodes());
            db.setNoOfActuators(device.getActuatorCodes().length);
        }

        if (device.getProtocol() != null) {
            db.setProtocol(device.getProtocol());
        }

        if (device.getConnectivity() != null) {
            db.setConnectivity(device.getConnectivity());
        }

        if (!device.getShiftMap().isEmpty()) {
            device.getShiftMap().forEach((key1, value1) -> {
                value1.forEach((key, value) -> {
                    switch (value.getOperation()) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            if (!value.getMeta().containsKey("pivot")) {
                                throw new MagmaException(MagmaStatus.NEED_A_PIVOT_FOR_SHIFTING);
                            }
                            break;
                    }
                });
            });
            db.setShiftMap(device.getShiftMap());
        }
        if (device.getRemoteConfigTopic() != null) {
            db.setRemoteConfigTopic(device.getRemoteConfigTopic());
        }
        if (device.getRemoteConfigAckTopic() != null) {
            db.setRemoteConfigAckTopic(device.getRemoteConfigAckTopic());
        }
        if (device.getOtaRequestTopic() != null) {
            db.setOtaRequestTopic(device.getOtaRequestTopic());
        }
        if (device.getOtaAckTopic() != null) {
            db.setOtaAckTopic(device.getOtaAckTopic());
        }
        deviceRepository.save(db);

        return "Successfully Updated";
    }

    public List<DeviceMaintenance> findMaintenanceHistoryByKitAndNumber(String deviceId, Integer number, Sort.Direction direction, String from, String to) {
        LOGGER.debug("Find Sensor request found Device : {}, Number : {}", deviceId, number);

        Device device = findDeviceById(deviceId);

        if (device.getSensorCodes().length < number) {
            throw new MagmaException(MagmaStatus.SENSOR_NOT_FOUND);
        }

        if (to == null && from == null) {
            to = MagmaTime.format(MagmaTime.now());
            from = MagmaTime.format(MagmaTime.now().minusDays(1));
        } else if (to == null) {
            to = from;
            from += " 00:00";
            to += " 23:59";
        } else if (from == null) {
            from = to;
            from += " 00:00";
            to += " 23:59";
        } else {
            from += " 00:00";
            to += " 23:59";
        }

        if (direction != null && direction.isDescending()) {
            return deviceMaintenanceRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to)
            );
        } else {
            return  deviceMaintenanceRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to)
            );

        }
    }
}
