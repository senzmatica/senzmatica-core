package com.magma.core.api;

import com.magma.core.data.dto.HttpAclAuthDTO;
import com.magma.core.data.entity.Http_acl_auth;
import com.magma.core.data.entity.Vmq_acl_auth;
import com.magma.core.service.HttpService;
import com.magma.core.service.MagmaCodecService;
import com.magma.core.service.VerneMqService;
import com.magma.core.validation.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600) // TODO: Need To Specify Domain
public class ConnectivityApi {

    @Autowired
    VerneMqService verneMqService;

    @Autowired
    HttpService httpService;

    @Autowired
    MagmaCodecService magmaCodecService;

    @RequestMapping(value = "/core/all-connectivity", method = RequestMethod.GET)
    public MagmaResponse<List<Map<String, Object>>> getClientsWithProtocolAndDeviceCount() {
        return new MagmaResponse<>(verneMqService.getClientDeviceInfo());
    }

    @RequestMapping(value = "/core/vernemq-acl", method = RequestMethod.GET)
    public MagmaResponse<List<Vmq_acl_auth>> getAllVmqAcl() {
        return new MagmaResponse<>(verneMqService.findAll());
    }

    @RequestMapping(value = "/core/vernemq-acl/{vernemqId}", method = RequestMethod.GET)
    public MagmaResponse<Vmq_acl_auth> getVmqAclOneClient(@PathVariable("vernemqId") String vernemqId) {
        return new MagmaResponse<>(verneMqService.getVmqAclOneClient(vernemqId));
    }

    @RequestMapping(value = "/core/vernemq-acl", method = RequestMethod.POST)
    public MagmaResponse<Vmq_acl_auth> createVmqAcl(@RequestBody @Valid Vmq_acl_auth vmqAclAuth,
                                                    BindingResult result) {
        if(result.hasErrors()){
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(verneMqService.createVmqAclAuth(vmqAclAuth));
    }

    @RequestMapping(value = "/core/vernemq-acl/{vernemqId}", method = RequestMethod.PUT)
    public MagmaResponse<Vmq_acl_auth> updateVernemq(@PathVariable("vernemqId") String vernemqId,
                                                     @RequestBody Vmq_acl_auth updatedVernemq) {
        Vmq_acl_auth vmqAclAuth = verneMqService.updateVernemq(vernemqId, updatedVernemq);
        return new MagmaResponse<>(vmqAclAuth);
    }

    @RequestMapping(value = "/core/vernemq-acl/{vernemq-aclId}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteVernemq(@PathVariable("vernemq-aclId") String vernemqId) {
        verneMqService.deleteVernemq(vernemqId);
        return new MagmaResponse<>("Vernemq-aclID deleted successfully");
    }

    @RequestMapping(value = "/core/connectivity/mqtt-status/{client_id}", method = RequestMethod.PUT)
    public MagmaResponse<Vmq_acl_auth> updateActionKey(@PathVariable String client_id,
                                                       @RequestParam boolean status) {
        Vmq_acl_auth vmqAclAuth = verneMqService.updateActionKey(client_id, status);
        return new MagmaResponse<>(vmqAclAuth);
    }

    @RequestMapping(value = "/core/connectivity/mqtt-protect/{clientId}", method = RequestMethod.PUT)
    public MagmaResponse<Vmq_acl_auth> updateMqttProtection(@PathVariable String clientId,
                                                            @RequestParam boolean isProtect) {
        Vmq_acl_auth vmq_acl_auth = verneMqService.updateMqttProtection(clientId, isProtect);
        return new MagmaResponse<>(vmq_acl_auth);
    }

    @RequestMapping(value = "/core/http-acl", method = RequestMethod.GET)
    public MagmaResponse<List<Http_acl_auth>> getAllHttpAcl() {
        return new MagmaResponse<>(httpService.findAll());
    }

    @RequestMapping(value = "/core/http-acl/{http-acl}", method = RequestMethod.GET)
    public MagmaResponse< Http_acl_auth> getOneClient(@PathVariable("http-acl") String httpAclId) {
        return new MagmaResponse<>(httpService.getOneClient(httpAclId));
    }

    @RequestMapping(value = "/core/http-acl", method = RequestMethod.POST)
    public MagmaResponse<Http_acl_auth> createHttpAclAuth(@RequestBody @Valid HttpAclAuthDTO httpAclAuthDTO,
                                                          BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(httpService.createHttpAclAuth(httpAclAuthDTO));
    }

    @RequestMapping(value = "/core/http-acl/{http-acl}", method = RequestMethod.PUT)
    public MagmaResponse<Http_acl_auth> updateClient(@PathVariable("http-acl") String httpAclId,
                                                     @RequestBody @Valid HttpAclAuthDTO updatedClientDto,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(httpService.updateClient(httpAclId, updatedClientDto));
    }

    @RequestMapping(value = "/core/http-acl/{http-acl}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteClient(@PathVariable("http-acl") String httpAclId) {
        return new MagmaResponse<>(httpService.deleteClient(httpAclId));
    }

    @RequestMapping(value = "/core/http-acl/{http-acl}/access-token", method = RequestMethod.POST)
    public MagmaResponse<Object> getAccessToken(@PathVariable("http-acl") String httpAclId) {
        return new MagmaResponse<>(httpService.getAccessToken(httpAclId));
    }

    @RequestMapping(value = "/core/connectivity/httpclient-status/{http-acl}", method = RequestMethod.PUT)
    public MagmaResponse<Http_acl_auth> updateActionKeyHttp(@PathVariable("http-acl") String httpAclId,
                                                            @RequestParam boolean status) {
        Http_acl_auth httAcl_auth = httpService.updateActionKeyHttp(httpAclId, status);
        return new MagmaResponse<>(httAcl_auth);
    }

    @RequestMapping(value = "/core/connectivity/http-protect/{clientId}", method = RequestMethod.PUT)
    public MagmaResponse<Http_acl_auth> updateHttpProtection(@PathVariable String clientId, @RequestParam boolean isProtect) {
        Http_acl_auth http_acl_auth = httpService.updateProtection(clientId, isProtect);
        return new MagmaResponse<>(http_acl_auth);
    }


}
