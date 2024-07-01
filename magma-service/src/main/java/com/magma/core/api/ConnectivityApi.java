package com.magma.core.api;

import com.magma.core.data.dto.HttpAclAuthDTO;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.Http_acl_auth;
import com.magma.core.data.entity.MagmaCodec;
import com.magma.core.data.entity.Vmq_acl_auth;
import com.magma.core.service.HttpService;
import com.magma.core.service.MagmaCodecService;
import com.magma.core.service.VerneMqService;
import com.magma.util.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public MagmaResponse<Map<String, Object>> getClientsWithProtocolAndDeviceCount() {
        return new MagmaResponse(verneMqService.getClientDeviceInfo());
    }

    // VerneMq ACL
    @RequestMapping(value = "/core/vernemq-acl", method = RequestMethod.GET)
    public MagmaResponse<List<Vmq_acl_auth>> getAllVmqAcl() {
        return new MagmaResponse<>(verneMqService.findAll());
    }

    @RequestMapping(value = "/core/vernemq-acl", method = RequestMethod.POST)
    public MagmaResponse<Vmq_acl_auth> createVmqAcl(@RequestBody Vmq_acl_auth vmqAclAuth) {
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

    // HTTP ACL
    @RequestMapping(value = "/core/http-acl", method = RequestMethod.GET)
    public MagmaResponse<List<Http_acl_auth>> getAllHttpAcl() {
        return new MagmaResponse<>(httpService.findAll());
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

    // Magma Codec
    @RequestMapping(value = "core/codec", method = RequestMethod.GET)
    public MagmaResponse<List<MagmaCodec>> getAllCodecs() {
        return new MagmaResponse<>(magmaCodecService.findAllCodec());
    }

    @RequestMapping(value = "/core/codec", method = RequestMethod.POST)
    public MagmaResponse<MagmaCodec> createMagmaCodec(@RequestParam(value = "decoderfile", required = false) MultipartFile decoderFile,
                                                      @RequestParam(value = "encoderfile", required = false) MultipartFile encoderFile,
                                                      @RequestParam("scriptformat") String scriptFormat,
                                                      @RequestParam("codecname") String codecName) {

        MagmaCodec magmaCodec = magmaCodecService.createMagmaCodec(decoderFile, encoderFile, codecName, scriptFormat);
        return new MagmaResponse<>(magmaCodec);
    }

    @RequestMapping(value = "/core/codec/{codecId}", method = RequestMethod.PUT)
    public MagmaResponse<MagmaCodec> updateMagmaCodec(@PathVariable("codecId") String codecId,
                                                      @RequestParam(value = "decoderfile", required = false) MultipartFile decoderFile,
                                                      @RequestParam(value = "encoderfile", required = false) MultipartFile encoderFile,
                                                      @RequestParam("scriptformat") String scriptFormat,
                                                      @RequestParam("codecname") String codecName) {

        MagmaCodec magmaCodec = magmaCodecService.updateMagmaCodec(codecId, decoderFile, encoderFile, codecName, scriptFormat);
        return new MagmaResponse<>(magmaCodec);
    }

    @RequestMapping(value = "/core/codec/{codecId}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deletecodec(@PathVariable("codecId") String codecId) {
        return new MagmaResponse<>(magmaCodecService.deleteCodec(codecId));
    }

    @RequestMapping(value = "/core/codec/{codecId}", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> connectedDevices(@PathVariable("codecId") String codecId) {
        return new MagmaResponse<>(magmaCodecService.viewConnectedDevice(codecId));
    }

    @RequestMapping(value = "/core/codec/{codecId}/device", method = RequestMethod.POST)
    public MagmaResponse<List<Device>> connectDevice(@PathVariable("codecId") String codecId,
                                                     @RequestBody List<String> deviceIds) {
        return new MagmaResponse<>(magmaCodecService.connectDeviceWithCodec(codecId, deviceIds));
    }

    @RequestMapping(value = "/core/codec/{codecId}/device/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<Device> updateConnectedcodec(@PathVariable("deviceId") String deviceId,
                                                      @PathVariable("codecId") String codecId,
                                                      @RequestParam String updateCodecId) {
        Device updatedDevice = magmaCodecService.updateCodec(deviceId, updateCodecId);
        return new MagmaResponse<>(updatedDevice);
    }

    @RequestMapping(value = "/core/codec/{codecId}/device/{deviceId}", method = RequestMethod.DELETE)
    public MagmaResponse<Device> disconnectDeviceAndDecoder(@PathVariable("codecId") String decoderId,
                                                            @PathVariable("deviceId") String deviceId) {
        Device device = magmaCodecService.disconnectDeviceAndCodec(deviceId);
        return new MagmaResponse<>(device);
    }

    @RequestMapping(value = "/core/codec/disconnect-devices", method = RequestMethod.PUT)
    public MagmaResponse<List<Device>> disconnectDeviceAndDecoder(@RequestBody List<String> deviceIds) {
        List<Device> devices = magmaCodecService.bulkDisconnectDeviceAndCodec(deviceIds);
        return new MagmaResponse<>(devices);
    }

}
