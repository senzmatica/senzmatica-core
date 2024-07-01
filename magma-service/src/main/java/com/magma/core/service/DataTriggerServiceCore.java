package com.magma.core.service;


import com.magma.core.data.entity.Action;
import com.magma.core.data.entity.Property;
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
}
