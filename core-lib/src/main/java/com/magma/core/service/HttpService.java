package com.magma.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.data.dto.HttpAclAuthDTO;
import com.magma.core.data.entity.Http_acl_auth;
import com.magma.core.data.repository.Http_acl_authRepository;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class HttpService {

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    Http_acl_authRepository http_acl_authRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

    public String getBaseUrl() {
        return "http://localhost:" + serverPort;
    }

    public Http_acl_auth updateClient(String httpAclId, HttpAclAuthDTO updatedClientDTO) {

        LOGGER.debug("Client ID : {}, for client  : {}", httpAclId, updatedClientDTO);

        if (httpAclId == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        Http_acl_auth http_acl_auth = http_acl_authRepository.findOne(httpAclId);

        if (http_acl_auth == null) {
            throw new MagmaException(MagmaStatus.CLIENT_NOT_FOUND);
        }

        if (!http_acl_auth.getClient_id().equals(updatedClientDTO.getClient_id())) {
            validateClientId(updatedClientDTO.getClient_id());
        }

        if (http_acl_auth.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        }

        http_acl_auth.setClient_id(updatedClientDTO.getClient_id());
        http_acl_auth.SetBackupClient_id(updatedClientDTO.getClient_id());
        http_acl_auth.setClient_secret(updatedClientDTO.getClient_secret());
        http_acl_auth.setGrant_type(updatedClientDTO.getGrant_type());
        http_acl_auth.setScope(updatedClientDTO.getScope());
        http_acl_auth.setAuthorities(updatedClientDTO.getAuthorities());

        http_acl_auth.validate();

        return http_acl_authRepository.save(http_acl_auth);
    }

    public List<Http_acl_auth> findAll() {
        return http_acl_authRepository.findAll();
    }

    public Http_acl_auth getOneClient(String clientId) {
        Http_acl_auth http_acl_auth = http_acl_authRepository.findOne(clientId);
        if (http_acl_auth == null) {
            throw new MagmaException(MagmaStatus.CLIENT_NOT_FOUND);
        }
        return http_acl_auth;
    }


    public String deleteClient(String clientId) {
        Http_acl_auth http_acl_auth = http_acl_authRepository.findOne(clientId);
        if (http_acl_auth.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        } else {
            http_acl_authRepository.delete(http_acl_auth);
        }

        return "The record id " + clientId + " has been successfully deleted";
    }

    public Http_acl_auth createHttpAclAuth(HttpAclAuthDTO http_acl_auth_Dto) {

        LOGGER.debug("Client ID : {}, for client  : {}", http_acl_auth_Dto.getClient_id(), http_acl_auth_Dto);

        Http_acl_auth http_acl_auth = new Http_acl_auth();
        BeanUtils.copyProperties(http_acl_auth_Dto, http_acl_auth);

        validateClientId(http_acl_auth.getClient_id());
        http_acl_auth.SetBackupClient_id(http_acl_auth.getClient_id());
        http_acl_auth.validate();

        return http_acl_authRepository.save(http_acl_auth);
    }

    private void validateClientId(String clientId) {

        Http_acl_auth existingClient = http_acl_authRepository.findClientId(clientId);

        if (existingClient != null
                && existingClient.getClient_id().equals(clientId)) {
            throw new MagmaException(MagmaStatus.DUPLICATE_CLIENT_ID);
        }

    }

    public Http_acl_auth updateActionKeyHttp(String client_id, boolean status) {
        Http_acl_auth client = http_acl_authRepository.findOne(client_id);
        LOGGER.debug("Client ID : {}, status : {}, for client  : {}", client_id, status, client);

        if (client == null) {
            LOGGER.debug("Client not available");
            throw new MagmaException(MagmaStatus.ERROR);
        }

        if (client.isProtect()) {
            throw new MagmaException(MagmaStatus.DATA_PROTECTED);
        }

        client.setStatus(status);

        if (status) {
            client.setClient_id(client.GetBackupClient_id());
        } else {
            client.setClient_id(null);
        }

        return http_acl_authRepository.save(client);
    }

    //TODO need a way to get this directly from oauth service rather than api call
    public Map<String, Object> getAccessToken(String httpAclId) {
        Http_acl_auth auth = http_acl_authRepository.findOne(httpAclId);
        if (auth == null) {
            LOGGER.error("No HTTP ACL row found for id {}", httpAclId);
            throw new MagmaException(MagmaStatus.NOT_FOUND);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Build URL with query params instead of form body
            String url = String.format("%s/oauth/token?grant_type=%s&client_id=%s&client_secret=%s",
                    getBaseUrl().replaceAll("/+$", ""),
                    auth.getGrant_type(),
                    auth.getClient_id(),
                    auth.getClient_secret()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> req = new HttpEntity<>(headers);

            LOGGER.debug("Token getBaseUrl   : {}", getBaseUrl());
            LOGGER.debug("Token endpoint URL   : {}", url);
            LOGGER.debug("Token endpoint REQ   : {}", req);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.POST, req, String.class);

            LOGGER.debug("Token endpoint status: {}", resp.getStatusCode());
            LOGGER.debug("Token endpoint body  : {}", resp.getBody());

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new MagmaException(MagmaStatus.ERROR);
            }

            // Parse JSON string -> Map
            Map<String, Object> parsed = MAPPER.readValue(
                    resp.getBody(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    }
            );

            return parsed;

        } catch (MagmaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while getting token for {}: {}", httpAclId, e.toString());
            throw new MagmaException(MagmaStatus.ERROR);
        }
    }

    public Http_acl_auth updateProtection(String clientId, boolean isProtect) {
        Http_acl_auth http_acl_auth = http_acl_authRepository.findOne(clientId);

        if (http_acl_auth == null) {
            throw new MagmaException(MagmaStatus.ERROR);
        }

        http_acl_auth.setProtect(isProtect);
        http_acl_authRepository.save(http_acl_auth);

        return http_acl_auth;
    }

}
