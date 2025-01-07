package com.magma.core.service;


import com.magma.dmsdata.data.entity.Action;
import com.magma.dmsdata.data.entity.Property;

public abstract class DataTriggerService {
    public abstract void triggerForAction(Action action);

    public abstract void triggerForProperty(Property property);
}
