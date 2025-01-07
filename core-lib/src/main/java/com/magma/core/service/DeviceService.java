package com.magma.core.service;

import com.magma.dmsdata.data.dto.DeviceDTO;
import com.magma.dmsdata.data.entity.*;
import com.magma.dmsdata.data.support.UserInfo;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import com.magma.core.data.repository.*;
import com.magma.util.MagmaResponse;
import com.magma.util.MagmaUtil;
import com.magma.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    ProductTypesRepository productTypesRepository;

    @Autowired
    SensorRepository sensorRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);

    public Device create(DeviceDTO deviceDTO) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);

        return deviceCreateLogic(device);
    }

    public Device createDevice(DeviceDTO deviceDTO, String userId) {
        LOGGER.debug("Create Device request found : {}", deviceDTO);

        Device device = new Device();
        UserInfo createdBy = new UserInfo();
        LOGGER.debug("####User Id : {}", userId);
        UserInfo user = userConnectorService.findUser(userId);
        LOGGER.debug("####User : {}", user);

        if (user != null) {
            BeanUtils.copyProperties(user, createdBy);
            device.setCreatedBy(createdBy);
        }
        BeanUtils.copyProperties(deviceDTO, device);

        return deviceCreateLogic(device);
    }

    private Device deviceCreateLogic(Device device) {

        if (!device.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        if (deviceRepository.findById(device.getId()).orElse(null) != null) {
            throw new MagmaException(MagmaStatus.DEVICE_ALREADY_EXISTS);
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
//                ProductTypes deviceProductType = productTypesRepository.findById(d.getProductType()).orElse(null);
//                d.setSensorCodes(deviceProductType.getSensorCodes());
//                d.setActuatorCodes(deviceProductType.getActuatorCodes());
//                d.setPersistence(deviceProductType.isPersistence());
//                d.setProtocol(deviceProductType.getProtocol());
//                d.setConnectivity(deviceProductType.getConnectivity());
//            }
            allDevices.add(d);
        });
        return allDevices;
    }

    public Device findDeviceById(String deviceId) {
        LOGGER.debug("Find Device request found : {}", deviceId);
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        //Adding sensor codes,Actuator codes,persistence, protocol and connectivity if   device is added under senzmatica 3.0
        if (device.getProductType() != null) {
            ProductTypes deviceProductType = productTypesRepository.findById(device.getProductType()).orElse(null);
            device.setSensorCodes(deviceProductType.getSensorCodes());
            device.setActuatorCodes(deviceProductType.getActuatorCodes());
            device.setPersistence(deviceProductType.isPersistence());
            device.setProtocol(deviceProductType.getProtocol());
            device.setConnectivity(deviceProductType.getConnectivity());
        }
        return device;
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

    public String updatePersistence(String kitId, Boolean persistence) {
        LOGGER.debug("Persistence request found Kit : {}, Persistence : {}", kitId, persistence);

        if (persistence == null) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit db = kitRepository.findById(kitId).orElse(null);
        if (db == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }

        db.setPersistence(persistence);

        //TODO: Can update via MongoTemplate
        List<Device> devices = deviceRepository.findByIdIn(db.getDevices());
        devices.stream().forEach(device -> device.setPersistence(persistence));
        deviceRepository.saveAll(devices);

        kitRepository.save(db);

        return "Successfully Updated";
    }

    public String deleteDevice(String deviceId) {
        LOGGER.debug("Delete request found DeviceId : {}", deviceId);
        deviceRepository.deleteById(deviceId);
        return "Successfully Updated";
    }


    public List<Sensor> getAllSensorDetailsByDeviceId(String deviceId) {
        Device requestedDevice = deviceRepository.findById(deviceId).orElse(null);
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

        deviceRepository.save(db);

        return "Successfully Updated";
    }

    public List<String> findFavouriteDeviceIds(String userId) {
        LOGGER.debug("Find Ids of Favourite devices - For User:{}", userId);
        return userFavouriteService.getUserFavouriteDevices(userId);
    }
}
