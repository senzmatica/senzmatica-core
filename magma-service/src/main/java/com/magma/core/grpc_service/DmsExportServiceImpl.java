package com.magma.core.grpc_service;

import com.google.protobuf.Empty;
import com.magma.core.grpc_service.helper.ProtoEntityPopulatingHelper;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Geo;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Property;
import com.magma.core.data.entity.Action;
import com.magma.core.data.entity.Alert;
import com.magma.core.data.entity.Offline;
import com.magma.core.data.entity.Error;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DmsExportServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(DmsExportServiceImpl.class);

    @Value("${senzagro.grpc.host}")
    private String dmsHost;

    @Value("${senzagro.grpc.port}")
    private int dmsPort;

    ProtoEntityPopulatingHelper DmsEntityPopulatingHelper;

    @Autowired
    public DmsExportServiceImpl(ProtoEntityPopulatingHelper DmsEntityPopulatingHelper) {
        this.DmsEntityPopulatingHelper = DmsEntityPopulatingHelper;
    }

    public void validateCorporate(String corporateId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.validateCorporate(ValidateCorporateRequest.newBuilder()
                .setCorporateId(corporateId)
                .build());

        channel.shutdown();
    }

    public void sendSMSBulk(String userId, List<String> toList, String message) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.sendSMSBulk(SendSMSBulkRequest.newBuilder()
                .setUserId(userId)
                .addAllToList(toList)
                .setMessage(message)
                .build());

        channel.shutdown();
    }

    public List<Kit> findKitsInCorporate(String corporateId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        FindKitsInCorporateResponse response = stub.findKitsInCorporate(FindKitsInCorporateRequest.newBuilder()
                .setCorporateId(corporateId)
                .build());

        channel.shutdown();

        return DmsEntityPopulatingHelper.populateKitListFromProto(response.getKitsList());
    }

    public String referenceName(String kitId) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        StringResponse response = stub.referenceName(ReferenceNameRequest.newBuilder()
                .setKitId(kitId)
                .build());

        channel.shutdown();

        return response.getResponse();
    }

    public void doSend(String kitId, Integer actuatorNumber, String message, Boolean sendWhenLive) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.doSend(DoSendRequest.newBuilder()
                .setKitId(kitId)
                .setActuatorNumber(actuatorNumber)
                .setMessage(message)
                .setSendWhenLive(sendWhenLive)
                .build());

        channel.shutdown();
    }


    public void triggerForAction(Action action) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.triggerForAction(TriggerForActionRequest.newBuilder()
                .setAction(DmsEntityPopulatingHelper.populateProtoActionEntity(action))
                .build());

        channel.shutdown();
    }

    public void triggerForProperty(Property property) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.triggerForProperty(TriggerForPropertyRequest.newBuilder()
                .setProperty(DmsEntityPopulatingHelper.populateProtoPropertyEntity(property))
                .build());

        channel.shutdown();
    }


    public void sendOfflineAlerts(List<Device> deviceList) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.sendOfflineAlerts(SendOfflineAlertsRequest.newBuilder()
                .addAllDeviceList(DmsEntityPopulatingHelper.populateProtoDeviceEntityList(deviceList))
                .build());

        channel.shutdown();
    }

    public void sendHighPriorityOfflineAlerts(List<Device> longOfflineDeviceList) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.sendHighPriorityOfflineAlerts(SendHighPriorityOfflineAlertsRequest.newBuilder()
                .addAllLongOfflineDeviceList(DmsEntityPopulatingHelper.populateProtoDeviceEntityList(longOfflineDeviceList))
                .build());

        channel.shutdown();
    }

    public void sendPeriodicOfflineAlerts(List<Offline> deviceList) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.sendPeriodicOfflineAlerts(SendPeriodicOfflineAlertsRequest.newBuilder()
                .addAllDeviceList(DmsEntityPopulatingHelper.populateProtoOfflineEntityList(deviceList))
                .build());

        channel.shutdown();
    }

    public void sendMqttFailureAlerts() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        stub.sendMqttFailureAlerts(Empty.newBuilder().build());

        channel.shutdown();
    }

    public void sendOfflineDevicesWeekWiseAlert(Map<String, List<Device>> weekWiseOfflineMap) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        for (Map.Entry<String, List<Device>> entry : weekWiseOfflineMap.entrySet()) {
            String week = entry.getKey();
            List<Device> devices = entry.getValue();

            List<com.magma.core.grpc_service.Device> protoDeviceEntities = DmsEntityPopulatingHelper.populateProtoDeviceEntityList(devices);

            stub.sendOfflineDevicesWeekWiseAlert(SendOfflineDevicesWeekWiseAlertRequest.newBuilder()
                    .putWeekWiseOfflineMap(week, com.magma.core.grpc_service.DeviceList.newBuilder().addAllDeviceList(protoDeviceEntities).build())
                    .build());
        }

        channel.shutdown();
    }

    public Geo findLbs(Kit kit) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        GeoResponse response = stub.findLbs(FindLbsRequest.newBuilder()
                .setKit(DmsEntityPopulatingHelper.populateProtoKitEntity(kit))
                .build());

        channel.shutdown();

        return DmsEntityPopulatingHelper.populateGeoFromProto(response.getGeo());
    }

    public String sendAlert(Kit kit, Alert alert) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        StringResponse response = stub.sendAlert(SendAlertRequest.newBuilder()
                .setKit(DmsEntityPopulatingHelper.populateProtoKitEntity(kit))
                .setAlert(DmsEntityPopulatingHelper.populateProtoAlertEntity(alert))
                .build());

        channel.shutdown();

        return response.getResponse();
    }

    public String sendAlertOff(Kit kit, Alert alert) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        StringResponse response = stub.sendAlertOff(SendAlertOffRequest.newBuilder()
                .setKit(DmsEntityPopulatingHelper.populateProtoKitEntity(kit))
                .setAlert(DmsEntityPopulatingHelper.populateProtoAlertEntity(alert))
                .build());

        channel.shutdown();

        return response.getResponse();
    }

    public String sendOffline(Kit kit, Offline offline) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        StringResponse response = stub.sendOffline(SendOfflineRequest.newBuilder()
                .setKit(DmsEntityPopulatingHelper.populateProtoKitEntity(kit))
                .setOffline(DmsEntityPopulatingHelper.populateProtoOfflineEntity(offline))
                .build());

        channel.shutdown();

        return response.getResponse();
    }

    public String sendError(Kit kit, Error error) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(dmsHost, dmsPort)
                .usePlaintext()
                .build();

        DmsExportServiceGrpc.DmsExportServiceBlockingStub stub
                = DmsExportServiceGrpc.newBlockingStub(channel);

        StringResponse response = stub.sendError(SendErrorRequest.newBuilder()
                .setKit(DmsEntityPopulatingHelper.populateProtoKitEntity(kit))
                .setError(DmsEntityPopulatingHelper.populateProtoErrorEntity(error))
                .build());

        channel.shutdown();

        return response.getResponse();
    }
}
