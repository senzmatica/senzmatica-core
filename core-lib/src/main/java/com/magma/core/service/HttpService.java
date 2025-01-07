package com.magma.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.dmsdata.data.dto.HttpAclAuthDTO;
import com.magma.dmsdata.data.entity.Http_acl_auth;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import com.magma.core.data.repository.Http_acl_authRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class HttpService {

    @Autowired
    Http_acl_authRepository http_acl_authRepository;

    @Autowired
    private HttpServletRequest request;

    public String getBaseUrl() {
        String baseUrl = request.getRequestURL().toString();
        return baseUrl.substring(0, baseUrl.length() - request.getRequestURI().length()) + request.getContextPath();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

    public Http_acl_auth updateClient(String httpAclId, HttpAclAuthDTO updatedClientDTO) {

        LOGGER.debug("Client ID : {}, for client  : {}", httpAclId, updatedClientDTO);

        if (httpAclId == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        Http_acl_auth http_acl_auth = http_acl_authRepository.findById(httpAclId).orElse(null);

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

    public String deleteClient(String clientId) {
        Http_acl_auth http_acl_auth = http_acl_authRepository.findById(clientId).orElse(null);
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

    public Object getAccessToken(String httpAclId) {

        Http_acl_auth auth = http_acl_authRepository.findById(httpAclId).orElse(null);

        LOGGER.debug("Client ID : {}, for client  : {}", httpAclId, auth);

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String clientId = auth.getClient_id();
            String clientSecret = auth.getClient_secret();

            String url = getBaseUrl() + "/oauth/token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", auth.getGrant_type());
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

            LOGGER.debug("loginLocal Request: {}", httpEntity);
            LOGGER.debug("loginLocal URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            LOGGER.debug("loginLocal StatusCode: {}", response.getStatusCode());
            LOGGER.debug("loginLocal Response: {}", response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonRes = objectMapper.readTree(response.getBody());

            return jsonRes;
        } catch (Exception e) {
            LOGGER.debug("Error while getting token : {}", e);
            throw new MagmaException(MagmaStatus.ERROR);
        }
    }
}
