package com.magma.core.service;


import java.util.ArrayList;
import java.util.Map;

import com.magma.core.data.entity.Action;
import com.magma.core.data.entity.DeviceMaintenance;
import com.magma.core.data.entity.Property;

public abstract class DataTriggerService {
    public abstract void triggerForAction(Action action);

    public abstract void triggerForProperty(Property property);

    public abstract void triggerForPropertyList(ArrayList<Property> var1);

    public abstract void triggerForDeviceMaintenanceMap(Map<Integer, DeviceMaintenance> var1);
}
