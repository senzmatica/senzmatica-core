package com.magma.core.grpc_service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.Empty;
import com.magma.core.data.entity.AlertLimit;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Geo;
import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Property;
import com.magma.core.grpc_service.helper.ProtoEntityPopulatingHelper;
import com.magma.core.service.CoreService;

import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

@GrpcService(CoreServiceImpl.class)
public class CoreServiceImpl extends CoreServiceGrpc.CoreServiceImplBase {

    CoreService coreService;
    ProtoEntityPopulatingHelper protoEntityPopulatingHelper;

    @Autowired
    public CoreServiceImpl(CoreService coreService, ProtoEntityPopulatingHelper protoEntityPopulatingHelper) {
        this.coreService = coreService;
        this.protoEntityPopulatingHelper = protoEntityPopulatingHelper;
    }
}
