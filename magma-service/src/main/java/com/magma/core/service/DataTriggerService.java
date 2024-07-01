package com.magma.core.service;


import com.magma.core.data.entity.Action;
import com.magma.core.data.entity.Property;

public abstract class DataTriggerService {
    public abstract void triggerForAction(Action action);

    public abstract void triggerForProperty(Property property);
}
