package com.magma.core.api;

import com.magma.core.service.DataProcessorService;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.MagmaCodec;
import com.magma.core.data.dto.MagmaCodecDTO;
import com.magma.core.service.MagmaCodecService;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import com.magma.core.validation.BadRequestException;

import java.util.List;
import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class CodecApi {
    @Autowired
    MagmaCodecService magmaCodecService;

    @Autowired
    DataProcessorService dataProcessorService;

    @RequestMapping(value = "core/codec", method = RequestMethod.GET)
    public MagmaResponse<List<MagmaCodec>> getAllCodecs() {
        return new MagmaResponse<>(magmaCodecService.findAllCodec());
    }

    @RequestMapping(value= "/core/codec/{codecId}", method = RequestMethod.GET)
    public MagmaResponse<MagmaCodec> getCodec(@PathVariable("codecId") String codecId) {
        return new MagmaResponse<>(magmaCodecService.getCodec(codecId));
    }

    @RequestMapping(value = "/core/codec", method = RequestMethod.POST)
    public MagmaResponse<MagmaCodec> createMagmaCodec(@ModelAttribute @Valid MagmaCodecDTO magmaCodecDTO,
                                                      BindingResult result) {
        if(result.hasErrors()){
            throw new BadRequestException(result.getAllErrors());
        }

        MagmaCodec magmaCodec = magmaCodecService.createMagmaCodec(magmaCodecDTO);
        return new MagmaResponse<>(magmaCodec);
    }

    @RequestMapping(value = "/core/codec/{codecId}", method = RequestMethod.PUT)
    public MagmaResponse<MagmaCodec> updateMagmaCodec(@PathVariable("codecId") String codecId,
                                                      @RequestParam(value = "decoderFile", required = false) MultipartFile decoderFile,
                                                      @RequestParam(value = "encoderFile", required = false) MultipartFile encoderFile,
                                                      @RequestParam("scriptFormat") String scriptFormat,
                                                      @RequestParam("codecName") String codecName) {

        MagmaCodec magmaCodec = magmaCodecService.updateMagmaCodec(codecId, decoderFile, encoderFile, codecName, scriptFormat);
        return new MagmaResponse<>(magmaCodec);
    }

    @RequestMapping(value = "/core/codec/{codecId}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteCodec(@PathVariable("codecId") String codecId) {
        return new MagmaResponse<>(magmaCodecService.deleteCodec(codecId));
    }

    @RequestMapping(value = "/core/codec/{codecId}/connected-devices", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> connectedDevices(@PathVariable("codecId") String codecId) {
        return new MagmaResponse<>(magmaCodecService.viewConnectedDevice(codecId));
    }

    @RequestMapping(value = "/core/codec/{codecId}/device", method = RequestMethod.POST)
    public MagmaResponse<List<Device>> connectDevice(@PathVariable("codecId") String codecId,
                                                     @RequestBody List<String> deviceIds) {
        return new MagmaResponse<>(magmaCodecService.connectDeviceWithCodec(codecId, deviceIds));
    }

    @RequestMapping(value = "/core/codec/{codecId}/device/{deviceId}", method = RequestMethod.PUT)
    public MagmaResponse<Device> updateConnectedCodec(@PathVariable("deviceId") String deviceId,
                                                      @RequestParam String updateCodecId) {
        Device updatedDevice = magmaCodecService.updateCodec(deviceId, updateCodecId);
        return new MagmaResponse<>(updatedDevice);
    }

    @RequestMapping(value = "/core/codec/{codecId}/device/{deviceId}", method = RequestMethod.DELETE)
    public MagmaResponse<Device> disconnectDeviceAndDecoder(@PathVariable("deviceId") String deviceId) {
        Device device = magmaCodecService.disconnectDeviceAndCodec(deviceId);
        return new MagmaResponse<>(device);
    }

    @RequestMapping(value = "/core/codec/disconnect-devices", method = RequestMethod.PUT)
    public MagmaResponse<List<Device>> disconnectDeviceAndDecoder(@RequestBody List<String> deviceIds) {
        List<Device> devices = magmaCodecService.bulkDisconnectDeviceAndCodec(deviceIds);
        return new MagmaResponse<>(devices);
    }

    @RequestMapping(value = "/core/codec/{codecId}/test", method = RequestMethod.POST)
    public MagmaResponse<String> testCodec(@PathVariable("codecId") String codecId,@RequestBody String payLoad) {
        return new MagmaResponse<>(dataProcessorService.handleCodec(codecId,payLoad));
    }
}
