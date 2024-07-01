package com.magma.core.api;

import com.magma.core.data.support.DataHTTP;
import com.magma.core.gateway.HTTPMessageHandler;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class OpenApi {

    @Autowired
    HTTPMessageHandler httpMessageHandler;

    @RequestMapping(value = "/open/device/{device}/data", method = RequestMethod.POST)
    public MagmaResponse<String> postData(@PathVariable("device") String deviceId,
                                          @RequestBody DataHTTP dataHTTP) {
        return new MagmaResponse<>(httpMessageHandler.handleMessage(deviceId, dataHTTP));
    }

}
