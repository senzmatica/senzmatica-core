package com.magma.core.service;

import com.magma.core.data.dto.KitDTO;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.Offset;
import com.magma.core.data.support.UserInfo;
import com.magma.core.util.DataInputMethod;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KitCoreService {
    @Autowired
    KitRepository kitRepository;

    @Autowired
    KitModelService kitModelService;

    @Autowired
    UserConnectorService userConnectorService;

    @Autowired
    CorporateConnectorService corporateConnectorService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    AlertLimitRepository alertLimitRepository;

    @Autowired
    AlertRepository alertRepository;

    @Value("${device.data.interval}")
    private Integer interval;

    private final MongoTemplate mongoTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);

    public KitCoreService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public Kit create(String kitModelId, KitDTO kitDTO) {
        LOGGER.debug("Create Kit request found : {}", kitDTO);

        KitModel kitModel = kitModelService.findKitModelById(kitModelId);
        Kit kit = new Kit();

        BeanUtils.copyProperties(kitDTO, kit);
        return kitCreateLogic(kit, kitModel);
    }


    public Kit createKit(String kitModelId, KitDTO kitDTO, String userId) {
        LOGGER.debug("Create Kit request found : {}", kitDTO);

        KitModel kitModel = kitModelService.findKitModelById(kitModelId);
        Kit kit = new Kit();

        UserInfo createdBy = new UserInfo();
        UserInfo user = userConnectorService.findUser(userId);

        BeanUtils.copyProperties(kitDTO, kit);
        BeanUtils.copyProperties(user, createdBy);
        kit.setCreatedBy(createdBy);

        return kitCreateLogic(kit, kitModel);
    }

    private Kit kitCreateLogic(Kit kit, KitModel kitModel) {
        kit.setModel(kitModel);

        if (!kit.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (kitRepository.findOne(kit.getId()) != null) {
            throw new MagmaException(MagmaStatus.KIT_ALREADY_EXISTS);
        }

        int sensors = 0;
        int actuators = 0;

        if (kit.getDevices() != null) {
            for (String deviceId : kit.getDevices()) {
                Device device = deviceRepository.findOne(deviceId);

                if (device == null) {
                    throw new MagmaException(MagmaStatus.DEVICE_NOT_EXISTS);
                }

                if (kitRepository.findByDevices(deviceId) != null) {
                    throw new MagmaException(MagmaStatus.DEVICE_ALREADY_ASSIGNED);
                }

                kit.getOffsetMap().put(deviceId, new Offset(sensors, actuators));

                if (device.getNoOfSensors() > 0) {
                    for (int x = 0; x < device.getNoOfSensors(); x++, sensors++) {
                        if (kitModel.getNoOfSensors() <= sensors) {
                            throw new MagmaException(MagmaStatus.NUMBER_OF_SENSORS_EXCEED);
                        }
                        if (!kitModel.getSensors()[sensors].equals(device.getSensorCodes()[x])) {
                            throw new MagmaException(MagmaStatus.KIT_MODEL_SENSOR_CODE_VALIDATE_ERROR);
                        }
                    }
                }
                if (device.getNoOfActuators() > 0) {
                    for (int y = 0; y < device.getNoOfActuators(); y++, actuators++) {
                        if (kitModel.getNoOfActuators() <= actuators) {
                            throw new MagmaException(MagmaStatus.NUMBER_OF_ACTUATORS_EXCEED);
                        }
                        if (!kitModel.getActuators()[actuators].equals(device.getActuatorCodes()[y])) {
                            throw new MagmaException(MagmaStatus.KIT_MODEL_ACTUATOR_CODE_VALIDATE_ERROR);
                        }
                    }
                }
            }
        }

        if (kit.getInterval() == null) {
            kit.setInterval(interval);
        }

        if (kit.getStatus() == null) {
            kit.setStatus(Status.ACTIVE);
        }

        if (kit.getMaintain() == null) {
            kit.setMaintain(false);
        }

        if (kit.getInputMethod() == null) {
            kit.setInputMethod(DataInputMethod.DEVICE);
        }

        if (!kit.getShiftMap().isEmpty()) {
            kit.getShiftMap().forEach((key, value) -> {
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
        }

        if (kitModel.getBatteryEnabled() != null &&
                kitModel.getBatteryEnabled() &&
                kit.getBattery() == null) {
            kit.setBattery(new Battery(240.0, 300.0));
        }

        return kitRepository.save(kit);
    }

    public List<Kit> findKits() {
        LOGGER.debug("Find all Kits request found");

        List<Kit> allKits = new ArrayList<>();
        List<Kit> devices = kitRepository.findAll();
        devices.forEach((d) -> {
            d.setReferenceName(corporateConnectorService.referenceName(d.getId()));
            allKits.add(d);
        });

        return allKits;
    }

    public Kit findKitById(String kitId) {
        LOGGER.debug("Find Kit request found : {}", kitId);
        Kit kit = kitRepository.findOne(kitId);
        if (kit == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }
        return kit;
    }

    public Kit findKitByDevice(String deviceId) {
        LOGGER.debug("Find Kit request found by Device : {}", deviceId);
        Kit kit = kitRepository.findByDevices(deviceId);

        if (kit == null) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }
        return kit;
    }

    public Kit findKitByDeviceExists(String deviceId) {
        LOGGER.debug("Find Kit request found by Device : {}", deviceId);
        return kitRepository.findByDevices(deviceId);
    }

    public String deleteKit(String kitId) {
        LOGGER.debug("Delete request found KitId : {}", kitId);

        //TODO: Have to Think about Past Properties Data.
        kitRepository.delete(kitId);
        return "Successfully Updated";
    }

    public List<Kit> findKitsByIdIn(List<String> ids) {
        LOGGER.debug("Find Kits request found : {}", ids);
        return kitRepository.findByIdIn(ids);
    }

    public List<Kit> findKitsByIdInAndOfflineIsTrue(List<String> ids) {
        LOGGER.debug("Find offline Kits request found : {}", ids);
        return kitRepository.findByIdInAndOfflineIsTrue(ids);
    }

    public List<Kit> findOnlineKits(List<String> ids) {
        LOGGER.debug("Find online Kits request found : {}", ids);
        return kitRepository.findByIdInAndOfflineIsFalse(ids);
    }

    public List<Kit> findKitsByIdInAndOfflineIsTrueAndPersistence(List<String> ids) {
        LOGGER.debug("Find offline and persistence Kits request found : {}", ids);
        return kitRepository.findByIdInAndOfflineIsTrueAndPersistenceIsTrue(ids);
    }


    public String updateMaintain(String kitId, Boolean maintain) {
        LOGGER.debug("Maintain request found Kit : {}, Maintain : {}", kitId, maintain);

        if (maintain == null) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit db = findKitById(kitId);
        db.setMaintain(maintain);
        kitRepository.save(db);

        return "Successfully Updated";
    }

    public String toggleAlertToKit(String kitId, Kit kit) {
        LOGGER.debug("Toggle Alert request found Kit : {}, Maintain : {}", kitId, kit);

        if (kit.getAlerts() == null || kit.getAlerts().isEmpty()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        Kit db = findKitById(kitId);

        kit.getAlerts().forEach((key, value) -> {
            if (key < 1 || key > db.getAlertLevel()) {
                throw new MagmaException(MagmaStatus.INVALID_ALERT_LEVEL);
            }
            db.getAlerts().put(key, value);
        });
        kitRepository.save(db);

        return "Successfully Updated";
    }

    public Kit updateKit(String kitId, Kit kit) {
        LOGGER.debug("Update request found KitId : {}, Kit : {}", kitId, kit);

        kit.setId(kitId);

        Kit db = findKitById(kitId);

        return kitUpdateLogic(db, kit);
    }

    public Kit editKit(String kitId, Kit kit, String userId) {
        LOGGER.debug("Update request found KitId : {}, Kit : {}", kitId, kit);

        kit.setId(kitId);

        Kit db = findKitById(kitId);
        UserInfo user = userConnectorService.findUser(userId);
        LOGGER.debug("User Information: {}", user);
        UserInfo modifiedBy = new UserInfo();

        BeanUtils.copyProperties(user, modifiedBy);

        db.setModifiedBy(modifiedBy);

        return kitUpdateLogic(db, kit);
    }

    private Kit kitUpdateLogic(Kit db, Kit kit) {
        if (MagmaUtil.validate(kit.getName())) {
            db.setName(kit.getName());
        }

        if (MagmaUtil.validate(kit.getDescription())) {
            db.setDescription(kit.getDescription());
        }

        if (kit.getStatus() != null) {
            db.setStatus(kit.getStatus());
        }
        if (kit.getInputMethod() != null) {
            db.setInputMethod(kit.getInputMethod());
        }
        if (kit.getSensorSort() != null) {
            db.setSensorSort(kit.getSensorSort());
        }

        if (kit.getMetaData() != null) {
            db.setMetaData(kit.getMetaData());
        }
        if (kit.getGeo() != null) {
            db.setGeo(kit.getGeo());
        }

        if (kit.getPersistence() != null) {
            db.setPersistence(kit.getPersistence());
        }

        if (kit.getKitModelId() != null) {
            KitModel kitModel = kitModelService.findKitModelById(kit.getKitModelId());
            db.setModel(kitModel);
        }

        int sensors = 0;
        int actuators = 0;
        if (kit.getDevices() != null) {
            db.getOffsetMap().clear();
            db.setDevices(kit.getDevices());

            for (String deviceId : kit.getDevices()) {
                Device device = deviceRepository.findOne(deviceId);

                if (device == null) {
                    throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
                }

                Kit deviceAssignKit = kitRepository.findByDevices(deviceId);

                if (deviceAssignKit != null && !db.getId().equals(deviceAssignKit.getId())) {
                    throw new MagmaException(MagmaStatus.DEVICE_ALREADY_ASSIGNED);
                }

                db.getOffsetMap().put(deviceId, new Offset(sensors, actuators));

                if (device.getNoOfSensors() > 0) {
                    for (int x = 0; x < device.getNoOfSensors(); x++, sensors++) {
                        if (kit.getModel() != null && kit.getModel().getNoOfSensors() <= sensors) {
                            throw new MagmaException(MagmaStatus.NUMBER_OF_SENSORS_EXCEED);
                        }
                        if (kit.getModel() != null && !kit.getModel().getSensors()[sensors].equals(device.getSensorCodes()[x])) {
                            throw new MagmaException(MagmaStatus.KIT_MODEL_SENSOR_CODE_VALIDATE_ERROR);
                        }
                    }
                }

                if (device.getNoOfActuators() > 0) {
                    for (int y = 0; y < device.getNoOfActuators(); y++, actuators++) {
                        if (kit.getModel() != null && kit.getModel().getNoOfActuators() <= actuators) {
                            throw new MagmaException(MagmaStatus.NUMBER_OF_ACTUATORS_EXCEED);
                        }
                        if (kit.getModel() != null && !kit.getModel().getActuators()[actuators].equals(device.getActuatorCodes()[y])) {
                            throw new MagmaException(MagmaStatus.KIT_MODEL_ACTUATOR_CODE_VALIDATE_ERROR);
                        }
                    }
                }
            }
        }

        if (kit.getInterval() != null) {
            db.setInterval(kit.getInterval());
        }

        if (kit.getMaintain() != null) {
            db.setMaintain(kit.getMaintain());
        }

        if (kit.getModel() != null && kit.getModel().getBatteryEnabled() != null &&
                kit.getModel().getBatteryEnabled()) {
            if (kit.getBattery() == null) {
                db.setBattery(new Battery(240.0, 300.0));
            }
            if (kit.getBattery() != null) {
                db.setBattery(kit.getBattery());
            }
        }

        if (kit.getAlertLevel() != null) {
            db.setAlertLevel(kit.getAlertLevel());
        }

        if (!kit.getShiftMap().isEmpty()) {
            kit.getShiftMap().forEach((key, value) -> {
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
            db.setShiftMap(kit.getShiftMap());
        }

        return kitRepository.save(db);
    }

    public List<Sensor> findSensorHistoryByKitAndNumber(String deviceId, Integer number, Sort.Direction direction, String from, String to) {
        LOGGER.debug("Find Sensor request found Device : {}, Number : {}", deviceId, number);

        Device device = deviceService.findDeviceById(deviceId);

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


        if (direction.isDescending()) {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        } else {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

    }

    public List<Actuator> findActuatorHistoryByKitAndNumber(String deviceId, Integer number, Sort.Direction direction, String from, String to) {
        LOGGER.debug("Find Actuator history request found Device : {}, Number : {}", deviceId, number);

        Device device = deviceService.findDeviceById(deviceId);

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


        if (direction.isDescending()) {
            return actuatorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDesc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        } else {
            return actuatorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAsc(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

    }


    public List<Property> findPropertyHistoryByKitAndNumber(String kitId, Integer number, String from, String to) {
        LOGGER.debug("Find Sensor request found Kit : {}, Number : {}", kitId, number);

        Kit kit = findKitById(kitId);

        if (!kit.getPropertyMap().containsKey(number) && (number != -10)) {
            throw new MagmaException(MagmaStatus.PROPERTY_NOT_FOUND);
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


        return propertyRepository.findByKitIdAndNumberAndTimeBetween(
                kitId,
                number,
                MagmaTime.parse(from),
                MagmaTime.parse(to));
    }

    public List<Property> findPropertyHistoryByKitAndNumber(String kitId, Integer number, DateTime from, DateTime to) {
        LOGGER.debug("Find Sensor request found Kit : {}, Number : {}, Between : {} {}", kitId, number, from, to);

        Kit kit = findKitById(kitId);

        if (!kit.getPropertyMap().containsKey(number) && (number != -10)) {
            throw new MagmaException(MagmaStatus.PROPERTY_NOT_FOUND);
        }

        return propertyRepository.findByKitIdAndNumberAndTimeBetween(kitId, number, from, to);
    }


    public List<Alert> findCurrentPropertyAlertByKitAndNumber(String kitId, Integer number) {
        LOGGER.debug("Find Sensor request found Kit : {}, Number : {}", kitId, number);

        Kit kit = findKitById(kitId);

        if (number < 0 || kit.getModel().getNoOfProperties() < (number + 1)) {
            throw new MagmaException(MagmaStatus.PROPERTY_NOT_FOUND);
        }

        return alertRepository.findByAlertLimitKitIdAndAlertLimitPropertyNumberAndEndTimeGreaterThanOrderByStartTimeDesc(
                kitId,
                number,
                MagmaTime.now());
    }

    public List<AlertLimit> findCurrentPropertyAlertLimitByKitAndNumber(String kitId, Integer number) {
        LOGGER.debug("Find Sensor request found Kit : {}, Number : {}", kitId, number);

        Kit kit = findKitById(kitId);

        if (number < 0 || kit.getModel().getNoOfProperties() < (number + 1)) {
            throw new MagmaException(MagmaStatus.PROPERTY_NOT_FOUND);
        }

        return alertLimitRepository.findByKitIdAndPropertyNumberOrderByLevelAsc(
                kitId,
                number);
    }


    public String updateKitOfflineStatus(String kitId, boolean offlineStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(kitId));
        Update update = new Update();
        update.set("offline", offlineStatus);

        if (!mongoTemplate.updateFirst(query, update, Kit.class).isUpdateOfExisting()) {
            throw new MagmaException(MagmaStatus.KIT_NOT_FOUND);
        }

        return "Successfully Updated";
    }
}
