package com.magma.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.data.entity.Http_acl_auth;
import com.magma.core.data.repository.Http_acl_authRepository;
import com.magma.core.data.entity.ClientDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomClientDetailsService implements ClientDetailsService {

    @Autowired
    private Http_acl_authRepository http_acl_authRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomClientDetailsService.class);

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        try {
            LOGGER.debug(" Request 1 clientDetail from http_acl_auth by user: {}", clientId);

            Http_acl_auth http_acl_auth = http_acl_authRepository.findClientId(clientId);

            LOGGER.debug(" Request 2 clientDetail from http_acl_auth : {}  by user: {}", http_acl_auth, clientId);
            ObjectMapper objectMapper = new ObjectMapper();
            String http_acl_authJson = null;

            try {
                http_acl_authJson = objectMapper.writeValueAsString(http_acl_auth);
            } catch (JsonProcessingException e) {
                throw new ClientRegistrationException("Client not found in json format change: " + clientId);
            }
            LOGGER.debug("Request to clientDetail from http_acl_auth : {} by user: {}", http_acl_authJson, clientId);

            if (http_acl_auth == null) {
                throw new ClientRegistrationException("Client not found: " + clientId);
            }

            if (http_acl_auth.isStatus() == false) {
                throw new ClientRegistrationException("Client is currently disabled. Kindly enable the client to access the login functionality.");
            }

            ClientDetail clientDetails = new ClientDetail();
            clientDetails.setClientId(http_acl_auth.getClient_id());
            clientDetails.setClientSecret(http_acl_auth.getClient_secret());
            clientDetails.setScope(http_acl_auth.getScope() != null ?
                    Arrays.asList(http_acl_auth.getScope().split(",")).stream()
                            .collect(Collectors.toSet()) : Collections.emptySet());

            clientDetails.setAuthorities(http_acl_auth.getAuthorities() != null ?
                    http_acl_auth.getAuthorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet()) : Collections.emptySet());

            Set<String> authorizedGrantTypes = new HashSet<>(Arrays.asList("refresh_token"));
            authorizedGrantTypes.addAll(http_acl_auth.getGrant_type() != null ?
                    Arrays.asList(http_acl_auth.getGrant_type().split(",")).stream()
                            .collect(Collectors.toSet()) : Collections.emptySet());
            clientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);

            clientDetails.setAccessTokenValiditySeconds(31556952);
            clientDetails.setRefreshTokenValiditySeconds(63113904);

            return clientDetails;
        } catch (Exception e) {
            LOGGER.error("Error in CustomClientDetailsService: {}", e.getMessage());
            e.printStackTrace();
            throw new ClientRegistrationException(e.getMessage());
        }
    }
}