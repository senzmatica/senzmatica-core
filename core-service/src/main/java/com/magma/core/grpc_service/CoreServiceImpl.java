package com.magma.core.grpc_service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.Empty;
import com.magma.core.grpc_service.helper.ProtoEntityPopulatingHelper;
import com.magma.dmsdata.data.entity.AlertLimit;
import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.Kit;
import com.magma.dmsdata.data.entity.Property;
import com.magma.core.service.CoreService;
import com.magma.core.service.DeviceService;
import com.magma.core.service.KitCoreService;

import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

@GrpcService(CoreServiceImpl.class)
public class CoreServiceImpl extends CoreServiceGrpc.CoreServiceImplBase {

    CoreService coreService;
    KitCoreService kitCoreService;
    DeviceService deviceService;
    ProtoEntityPopulatingHelper protoEntityPopulatingHelper;

    @Autowired
    public CoreServiceImpl(CoreService coreService, ProtoEntityPopulatingHelper protoEntityPopulatingHelper) {
        this.coreService = coreService;
        this.protoEntityPopulatingHelper = protoEntityPopulatingHelper;
    }

    public void findKits(Empty request, StreamObserver<KitsListResponse> responseObserver) {
        List<Kit> kits = kitCoreService.findKits();
        KitsListResponse response = KitsListResponse.newBuilder()
                .addAllKitList(protoEntityPopulatingHelper.populateProtoKitEntityList(kits))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateAlertLevel(UpdateAlertLevelRequest request, StreamObserver<StringResponse> responseObserver) {
        String response = coreService.updateAlertLevel(request.getKitId(), request.getLevel());
        StringResponse stringResponse = StringResponse.newBuilder()
                .setResponse(response)
                .build();
        responseObserver.onNext(stringResponse);
        responseObserver.onCompleted();
    }

    public void updateInterval(UpdateIntervalRequest request, StreamObserver<StringResponse> responseObserver) {
        String response = coreService.updateInterval(request.getKitId(), request.getInterval());
        StringResponse stringResponse = StringResponse.newBuilder()
                .setResponse(response)
                .build();
        responseObserver.onNext(stringResponse);
        responseObserver.onCompleted();
    }

    public void findKitsByIdInAndOfflineIsTrue(StringListRequest request, StreamObserver<KitsListResponse> responseObserver) {
        List<Kit> kits = kitCoreService.findKitsByIdInAndOfflineIsTrue(request.getStringListList());
        KitsListResponse response = KitsListResponse.newBuilder()
                .addAllKitList(protoEntityPopulatingHelper.populateProtoKitEntityList(kits))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findOnlineKits(StringListRequest request, StreamObserver<KitsListResponse> responseObserver) {
        List<Kit> kits = kitCoreService.findOnlineKits(request.getStringListList());
        KitsListResponse response = KitsListResponse.newBuilder()
                .addAllKitList(protoEntityPopulatingHelper.populateProtoKitEntityList(kits))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findKitById(KitIdRequest request, StreamObserver<KitResponse> responseObserver) {
        Kit kit = kitCoreService.findKitById(request.getId());
        KitResponse response = KitResponse.newBuilder()
                .setKit(protoEntityPopulatingHelper.populateProtoKitEntity(kit))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateKit(UpdateKitRequest request, StreamObserver<KitResponse> responseObserver) {
        Kit kit = kitCoreService.updateKit(request.getKitId(), protoEntityPopulatingHelper.populateKitFromProto(request.getKit()));
        KitResponse response = KitResponse.newBuilder()
                .setKit(protoEntityPopulatingHelper.populateProtoKitEntity(kit))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findKitsByIdIn(StringListRequest request, StreamObserver<KitsListResponse> responseObserver) {
        List<Kit> kits = kitCoreService.findKitsByIdIn(request.getStringListList());
        KitsListResponse response = KitsListResponse.newBuilder()
                .addAllKitList(protoEntityPopulatingHelper.populateProtoKitEntityList(kits))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateMaintain(UpdateMaintainRequest request, StreamObserver<StringResponse> responseObserver) {
        String response = kitCoreService.updateMaintain(request.getKitId(), request.getMaintain());
        StringResponse stringResponse = StringResponse.newBuilder()
                .setResponse(response)
                .build();
        responseObserver.onNext(stringResponse);
        responseObserver.onCompleted();
    }

    public void updatePersistence(UpdatePersistenceRequest request, StreamObserver<StringResponse> responseObserver) {
        String response = deviceService.updatePersistence(request.getKitId(), request.getPersistence());
        StringResponse stringResponse = StringResponse.newBuilder()
                .setResponse(response)
                .build();
        responseObserver.onNext(stringResponse);
        responseObserver.onCompleted();
    }

    public void toggleAlertToKit(ToggleAlertToKitRequest request, StreamObserver<StringResponse> responseObserver) {
        String response = kitCoreService.toggleAlertToKit(request.getKitId(), protoEntityPopulatingHelper.populateKitFromProto(request.getKit()));
        StringResponse stringResponse = StringResponse.newBuilder()
                .setResponse(response)
                .build();
        responseObserver.onNext(stringResponse);
        responseObserver.onCompleted();
    }

    public void createOrUpdateAlertLimit(CreateOrUpdateAlertLimitRequest request, StreamObserver<CreateOrUpdateAlertLimitResponse> responseObserver) {
        AlertLimit alertLimit = coreService.createOrUpdateAlertLimit(request.getKitId(), request.getNumber(),
                protoEntityPopulatingHelper.populateAlertLimitFromProto(request.getAlertLimit()));
        CreateOrUpdateAlertLimitResponse response = CreateOrUpdateAlertLimitResponse.newBuilder()
                .setAlertLimit(protoEntityPopulatingHelper.populateProtoAlertLimitEntity(alertLimit))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findCurrentPropertyAlertLimitByKitAndNumber(FindCurrentPropertyAlertLimitByKitAndNumberRequest request,
                                                            StreamObserver<AlertLimitListResponse> responseObserver) {
        List<AlertLimit> alertLimits = kitCoreService.findCurrentPropertyAlertLimitByKitAndNumber(request.getKitId(), request.getNumber());
        AlertLimitListResponse response = AlertLimitListResponse.newBuilder()
                .addAllAlertLimitList(protoEntityPopulatingHelper.populateProtoAlertLimitEntityList(alertLimits))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findPropertyHistoryByKitAndNumber(FindPropertyHistoryByKitAndNumberRequest request,
                                                  StreamObserver<PropertyListResponse> responseObserver) {
        List<Property> properties = kitCoreService.findPropertyHistoryByKitAndNumber(request.getKitId(), request.getNumber(),
                request.getFrom().toString(),
                request.getTo().toString());
        PropertyListResponse response = PropertyListResponse.newBuilder()
                .addAllPropertyList(protoEntityPopulatingHelper.populateProtoPropertyEntityList(properties))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findGeoHistoryByKit(FindGeoHistoryByKitRequest request,
                                    StreamObserver<GeoListResponse> responseObserver) {

        List<Geo> geos = coreService.findGeoHistoryByKit(request.getKitId(), request.getFrom().toString(), request.getTo().toString(),
                protoEntityPopulatingHelper.populateGeoTypeFromProto(request.getGeoType()));
        GeoListResponse response = GeoListResponse.newBuilder()
                .addAllGeoList(protoEntityPopulatingHelper.populateProtoGeoEntityList(geos))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findKitByDeviceExists(FindKitByDeviceExistsRequest request,
                                      StreamObserver<KitResponse> responseObserver) {
        Kit kit = kitCoreService.findKitByDeviceExists(request.getDeviceId());
        KitResponse response = KitResponse.newBuilder()
                .setKit(protoEntityPopulatingHelper.populateProtoKitEntity(kit))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void findDeviceById(FindDeviceByIdRequest request,
                               StreamObserver<FindDeviceByIdResponse> responseObserver) {
        Device device = deviceService.findDeviceById(request.getDeviceId());
        FindDeviceByIdResponse response = FindDeviceByIdResponse.newBuilder()
                .setDevice(protoEntityPopulatingHelper.populateProtoDeviceEntity(device))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
