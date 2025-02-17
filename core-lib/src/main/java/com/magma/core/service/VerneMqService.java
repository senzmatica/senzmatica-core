package com.magma.core.service;

import com.magma.dmsdata.data.entity.Http_acl_auth;
import com.magma.dmsdata.data.entity.Vmq_acl_auth;
import com.magma.core.data.repository.Http_acl_authRepository;
import com.magma.core.data.repository.Vmq_acl_authRepository;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import com.magma.dmsdata.util.UpdatableBCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerneMqService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerneMqService.class);

    @Autowired
    Vmq_acl_authRepository vmq_acl_authRepository;

    @Autowired
    Http_acl_authRepository http_acl_authRepository;

    @Autowired
    UpdatableBCrypt updatableBCrypt;


    public Vmq_acl_auth createVmqAclAuth(Vmq_acl_auth vmqAclAuth) {
        String passHash = updatableBCrypt.hash(vmqAclAuth.getPassword());
        vmqAclAuth.setPasshash(passHash);
        vmqAclAuth.setBackupPasshash(passHash);
        vmqAclAuth.setPassword(vmqAclAuth.getPassword());
        vmqAclAuth.setMountpoint("");
        validateClientId(vmqAclAuth.getClient_id());
        return vmq_acl_authRepository.save(vmqAclAuth);
    }

    public List<Vmq_acl_auth> findAll() {
        return vmq_acl_authRepository.findAll();
    }

    public Vmq_acl_auth getVmqAclOneClient(String vernemqId) {
        Vmq_acl_auth vmq_acl_auth = vmq_acl_authRepository.findById(vernemqId).orElse(null);
        if (vmq_acl_auth == null) {
            throw new MagmaException(MagmaStatus.CLIENT_NOT_FOUND);
        }
        return vmq_acl_auth;
    }


    public String deleteVernemq(String vernemqId) {
        Vmq_acl_auth vmq_acl_auth = vmq_acl_authRepository.findById(vernemqId).orElse(null);

        if (vmq_acl_auth.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        }
        vmq_acl_authRepository.deleteById(vernemqId);
        return "The record id " + vernemqId + " has been successfully deleted";
    }

    public Vmq_acl_auth updateVernemq(String vernemqId, Vmq_acl_auth updatedVernemq) {

        Vmq_acl_auth vmqAclAuth = vmq_acl_authRepository.findById(vernemqId).orElse(null);

        if (vmqAclAuth.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        }

        if (vmqAclAuth == null) {
            throw new MagmaException(MagmaStatus.CLIENT_NOT_FOUND);
        }

        if (!vmqAclAuth.getClient_id().equals(updatedVernemq.getClient_id())) {
            validateClientId(updatedVernemq.getClient_id());
        }

        String passHash = vmqAclAuth.getPasshash();
        if (!updatableBCrypt.verifyHash(updatedVernemq.getPassword(), vmqAclAuth.getPasshash())) {
            passHash = updatableBCrypt.hash(updatedVernemq.getPassword());
        }

        vmqAclAuth.setMountpoint("");
        vmqAclAuth.setClient_id(updatedVernemq.getClient_id());
        vmqAclAuth.setUsername(updatedVernemq.getUsername());
        vmqAclAuth.setPasshash(passHash);
        vmqAclAuth.setBackupPasshash(passHash);
        vmqAclAuth.setPassword(updatedVernemq.getPassword());
        vmqAclAuth.setPublish_acl(updatedVernemq.getPublish_acl());
        vmqAclAuth.setSubscribe_acl(updatedVernemq.getSubscribe_acl());
        vmq_acl_authRepository.save(vmqAclAuth);

        return vmqAclAuth;
    }


    public List<Map<String, Object>> getClientDeviceInfo() {
        List<Vmq_acl_auth> vmq_aclList = vmq_acl_authRepository.findAll();
        List<Http_acl_auth> http_aclList = http_acl_authRepository.findAll();
        List<Map<String, Object>> clientIdsWithProtocolAndDeviceCount = new ArrayList<>();

        for (Vmq_acl_auth acl : vmq_aclList) {
            Map<String, Object> clientIdWithProtocolAndDeviceCount = new HashMap<>();
            clientIdWithProtocolAndDeviceCount.put("clientId", acl.getClient_id());
            clientIdWithProtocolAndDeviceCount.put("protocol", "MQTT");
            clientIdWithProtocolAndDeviceCount.put("deviceCount", 1);

            clientIdWithProtocolAndDeviceCount.put("data", acl);
            clientIdsWithProtocolAndDeviceCount.add(clientIdWithProtocolAndDeviceCount);
        }

        for (Http_acl_auth acl : http_aclList) {
            Map<String, Object> clientIdWithProtocolAndDeviceCount = new HashMap<>();
            if (acl.getClient_id() == null) {
                clientIdWithProtocolAndDeviceCount.put("clientId", acl.GetBackupClient_id());
            } else {
                clientIdWithProtocolAndDeviceCount.put("clientId", acl.getClient_id());
            }
            clientIdWithProtocolAndDeviceCount.put("protocol", "HTTP/S");
            clientIdWithProtocolAndDeviceCount.put("deviceCount", 1);

            clientIdWithProtocolAndDeviceCount.put("data", acl);
            clientIdsWithProtocolAndDeviceCount.add(clientIdWithProtocolAndDeviceCount);
        }

        return clientIdsWithProtocolAndDeviceCount;
    }

    public Vmq_acl_auth updateActionKey(String client_id, boolean status) {
        Vmq_acl_auth client = vmq_acl_authRepository.findClientId(client_id);
        if (client == null) {
            throw new MagmaException(MagmaStatus.ERROR);
        }
        client.setStatus(status);

        if (status) {
            client.setPasshash(client.getBackupPasshash());
        } else {
            client.setPasshash(null);
        }

        if (client.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        }

        return vmq_acl_authRepository.save(client);
    }

    public Vmq_acl_auth updateMqttProtection(String clientId, boolean protect) {
        Vmq_acl_auth vmq_acl_auth = vmq_acl_authRepository.findById(clientId).orElse(null);
        if (vmq_acl_auth == null) {
            throw new MagmaException(MagmaStatus.ERROR);
        }

        vmq_acl_auth.setProtect(protect);
        vmq_acl_authRepository.save(vmq_acl_auth);
        return vmq_acl_auth;
    }

    private void validateClientId(String clientId) {

        Vmq_acl_auth existingClient = vmq_acl_authRepository.findClientId(clientId);

        if (existingClient != null
                && existingClient.getClient_id().toString().equals(clientId.toString())) {
            throw new MagmaException(MagmaStatus.DUPLICATE_CLIENT_ID);
        }

    }


}
