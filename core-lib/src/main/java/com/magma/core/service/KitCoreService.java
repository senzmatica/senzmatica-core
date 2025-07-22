package com.magma.core.service;

import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.DataHTTP;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KitCoreService {

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


        if (direction != null && direction.isDescending()) {
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

    public List<Sensor> findSensorHistoryByKitAndNumberNew(String deviceId, Integer number, Sort.Direction direction, String from, String to) {
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


        if (direction != null && direction.isDescending()) {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeDescNew(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        } else {
            return sensorRepository.findByDeviceIdAndNumberAndTimeBetweenOrderByTimeAscNew(
                    deviceId,
                    number,
                    MagmaTime.parse(from),
                    MagmaTime.parse(to));
        }

    }

    public Sensor postSensorsData(String deviceId, Integer number, DataHTTP dataHTTP) {
        LOGGER.debug("Find Sensor request found Device : {}, Number : {}", deviceId, number);

        Device device = deviceService.findDeviceById(deviceId);

        if (device.getSensorCodes().length < number) {
            throw new MagmaException(MagmaStatus.SENSOR_NOT_FOUND);
        }

        List<Sensor> sensors = deviceService.getAllSensorDetailsByDeviceId(deviceId);

        Sensor desiredSensor = sensors.stream()
                .filter(sensor -> sensor.getNumber() == number)
                .findFirst()
                .orElse(null);

        desiredSensor.setValue(dataHTTP.getData());
        if (dataHTTP.getTime() == null) {
            desiredSensor.setTime(new DateTime());
        } else {
            desiredSensor.setTime(MagmaTime.parse(dataHTTP.getTime()));
        }

        return sensorRepository.save(desiredSensor);
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

}
