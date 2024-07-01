package com.magma.core.service.impl;

public abstract class DataTransmittingService {

    public abstract void doSend(String kitId, Integer actuatorNumber, String message, Boolean sendWhenLive);

}
