package com.magma.core.service;


import com.magma.core.data.entity.Action;
import com.magma.core.data.entity.DeviceMaintenance;
import com.magma.core.data.entity.Property;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DataTriggerServiceCore extends DataTriggerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataTriggerServiceCore.class);

    @Override
    public void triggerForAction(Action action) {
        LOGGER.debug("Trigger for Action : {}", action);
    }

    @Override
    public void triggerForProperty(Property property) {
        LOGGER.debug("Trigger for Property : {}", property);
    }

    @Override
    public void triggerForPropertyList(ArrayList<Property> propertyLists) {
        LOGGER.debug("Trigger for property list : {}", propertyLists);
    }

    @Override
    public void triggerForDeviceMaintenanceMap(Map<Integer, DeviceMaintenance> deviceMaintenanceMap) {
        LOGGER.debug("Trigger for deviceMaintenance map : {}", deviceMaintenanceMap );
    }
}
