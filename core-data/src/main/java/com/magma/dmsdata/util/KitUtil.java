package com.magma.dmsdata.util;

import com.magma.dmsdata.data.entity.KitModel;
import com.magma.dmsdata.data.support.Operation;

import java.util.*;

public class KitUtil {

    public static boolean validateModel(KitModel km) {
        if (km.getSensors().length != km.getNoOfSensors() ||
                km.getActuators().length != km.getNoOfActuators() ||
                km.getProperties().length != km.getNoOfProperties() ||
                km.getOperations().size() < km.getNoOfProperties()) {
            return false;
        }

        for (Operation operation : km.getOperations()) {
            Integer property = operation.getPropertyNumber();
            if ((property < -11) || property > km.getNoOfProperties()) {
                return false;
            }

            if (operation.getSensorNumberList() == null || operation.getSensorNumberList().isEmpty()) {
                return false;
            }

            for (Integer sensor : operation.getSensorNumberList()) {
                if ((sensor < -11) || sensor > km.getNoOfSensors()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String createId(KitModel km) {
        List<Operation> list = new ArrayList<>(km.getOperations());
        Collections.sort(list);
        String id = "";

        for (Operation operation : list) {
            switch (operation.getPropertyNumber()) {
                case -10:
                    id += operation.getType().value() + "_B_";
                    break;
                case -11:
                    id += operation.getType().value() + "_BL_";
                    break;
                case -1:
                    id += operation.getType().value() + "_GPS_";
                    break;
                default:
                    id += operation.getType().value() + "_" + km.getProperties()[operation.getPropertyNumber()] + "_";
            }
        }

        for (ActuatorCode actuator : km.getActuators()) {
            id += actuator + "_";
        }

        return id;
    }

    public static KitModel distributeOperations(KitModel kitModel) {
        kitModel.getOperations().forEach(operation -> {
            switch (operation.getType()) {
                case REAL_TIME:
                    operation.getSensorNumberList().forEach(sNo -> {
                        Set<Operation> operationSet = kitModel.getRealTimeSet().get(sNo);
                        if (operationSet == null) {
                            operationSet = new HashSet<>();
                        }
                        operationSet.add(operation);
                        kitModel.getRealTimeSet().put(sNo, operationSet);
                    });
                    break;
                case BULK:
                    kitModel.getBulkSet().add(operation);
                    break;
            }
        });
        return kitModel;
    }

    public static KitModel addOperation(KitModel kitModel, Operation operation) {
        kitModel.getOperations().add(operation);

        switch (operation.getType()) {
            case REAL_TIME:
                operation.getSensorNumberList().forEach(sNo -> {
                    Set<Operation> operationSet = kitModel.getRealTimeSet().get(sNo);
                    if (operationSet == null) {
                        operationSet = new HashSet<>();
                    }
                    operationSet.add(operation);
                    kitModel.getRealTimeSet().put(sNo, operationSet);
                });
                break;
            case BULK:
                kitModel.getBulkSet().add(operation);
                break;
        }
        return kitModel;
    }

    public static KitModel removeOperation(KitModel kitModel, Operation operation) {
        kitModel.getOperations().remove(operation);

        switch (operation.getType()) {
            case REAL_TIME:
                operation.getSensorNumberList().forEach(sNo -> {
                    Set<Operation> operationSet = kitModel.getRealTimeSet().get(sNo);
                    if (operationSet != null) {
                        operationSet.remove(operation);
                    }
                });
                break;
            case BULK:
                kitModel.getBulkSet().remove(operation);
                break;
        }
        return kitModel;
    }
}
