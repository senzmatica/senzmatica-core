package com.magma.core.api;

import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.support.CorporateDeviceSummary;
import com.magma.dmsdata.data.support.CorporateSensorSummary;
import com.magma.core.service.ReferenceService;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class CorporateApi {

    @Autowired
    ReferenceService corporateService;

    @RequestMapping(value = "/user/{user}/corporate/{corporate}/deviceSummary", method = RequestMethod.GET)
    public MagmaResponse<CorporateDeviceSummary> corporateWiseDeviceSummary(@PathVariable("user") String user,
                                                                            @PathVariable("corporate") String corporateId,
                                                                            @RequestParam("favouriteFilter") boolean favouriteFilter) {
        return new MagmaResponse<>(corporateService.getCorporateWiseDeviceSummary(user, corporateId, favouriteFilter));
    }

    @RequestMapping(value = "/user/{user}/corporate/{corporate}/sensorSummary", method = RequestMethod.GET)
    public MagmaResponse<CorporateSensorSummary> getCorporateAlerts(@PathVariable("corporate") String corporateId,
                                                                    @PathVariable("user") String userId) {
        return new MagmaResponse<>(corporateService.getCorporateWiseSensorSummary(corporateId));
    }

    @RequestMapping(value = "/user/{user}/corporate/{corporateId}/corporateDevices", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesCorporateWise(@PathVariable("user") String userId,
                                                               @PathVariable("corporateId") String corporateId,
                                                               @RequestParam(value = "favouriteFilter", required = false) boolean favouriteFilter,
                                                               @RequestParam(value = "dataFilter", required = false) String dataFilter,
                                                               @RequestParam(value = "sensor", required = false) String sensor) {
        return new MagmaResponse<>(corporateService.getCorporateWiseDevices(userId, corporateId, favouriteFilter, dataFilter, sensor));
    }

}
