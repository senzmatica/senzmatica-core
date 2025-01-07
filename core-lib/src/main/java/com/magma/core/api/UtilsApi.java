package com.magma.core.api;

import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600) //TODO: Need to specify Domain
public class UtilsApi {

    @Value("${project.version}")
    private String version;

    @RequestMapping(value = "/open/info", method = RequestMethod.GET)
    public MagmaResponse<String> loadPrivilege() {

        return new MagmaResponse<>("DMS-Service: V-" + version);
    }


}
