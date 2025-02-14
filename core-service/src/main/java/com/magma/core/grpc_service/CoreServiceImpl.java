package com.magma.core.grpc_service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.Empty;
import com.magma.core.grpc_service.helper.ProtoEntityPopulatingHelper;
import com.magma.dmsdata.data.entity.AlertLimit;
import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.Property;
import com.magma.core.service.CoreService;
import com.magma.core.service.DeviceService;
import com.magma.core.service.KitCoreService;

import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

@GrpcService(CoreServiceImpl.class)
public class CoreServiceImpl extends CoreServiceGrpc.CoreServiceImplBase {

    CoreService coreService;
    DeviceService deviceService;
    ProtoEntityPopulatingHelper protoEntityPopulatingHelper;

    @Autowired
    public CoreServiceImpl(CoreService coreService, ProtoEntityPopulatingHelper protoEntityPopulatingHelper) {
        this.coreService = coreService;
        this.protoEntityPopulatingHelper = protoEntityPopulatingHelper;
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
