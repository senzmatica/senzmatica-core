package com.magma.core.api;

import com.magma.core.data.dto.KitDTO;
import com.magma.core.data.entity.*;
import com.magma.core.data.support.GeoType;
import com.magma.core.service.CoreService;
import com.magma.core.service.DeviceService;
import com.magma.core.service.KitCoreService;
import com.magma.core.util.Aggregation;
import com.magma.core.util.Granularity;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class KitCoreApi {

    @Autowired
    CoreService coreService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    KitCoreService kitService;

    @RequestMapping(value = "/core/kit-model/{kitModel}/kit", method = RequestMethod.POST)
    public MagmaResponse<Kit> createKit(@PathVariable("kitModel") String kitModelId,
                                        @RequestBody KitDTO kitDTO) {
        return new MagmaResponse<>(kitService.create(kitModelId, kitDTO));
    }

    @RequestMapping(value = "/core/user/{userId}/kit-model/{kitModel}/kit", method = RequestMethod.POST)
    public MagmaResponse<Kit> createKit(@PathVariable("kitModel") String kitModelId,
                                        @PathVariable("userId") String userId,
                                        @RequestBody KitDTO kitDTO) {
        return new MagmaResponse<>(kitService.createKit(kitModelId, kitDTO, userId));
    }

    @RequestMapping(value = "/core/kit/{kit}", method = RequestMethod.GET)
    public MagmaResponse<Kit> getKit(@PathVariable("kit") String kitId) {
        return new MagmaResponse<>(kitService.findKitById(kitId));
    }

    @RequestMapping(value = "/core/kit/{kit}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteKit(@PathVariable("kit") String kitId) {
        return new MagmaResponse<>(kitService.deleteKit(kitId));
    }

    @RequestMapping(value = "/core/kit", method = RequestMethod.GET)
    public MagmaResponse<List<Kit>> getKits() {
        return new MagmaResponse<>(kitService.findKits());
    }

    @RequestMapping(value = "/core/kit/{kit}/maintain", method = RequestMethod.PUT)
    public MagmaResponse<String> setMaintain(@PathVariable("kit") String kitId,
                                             @RequestBody Kit kit) {
        return new MagmaResponse<>(kitService.updateMaintain(kitId, kit.getMaintain()));
    }

    @RequestMapping(value = "/core/kit/{kit}/persistence", method = RequestMethod.PUT)
    public MagmaResponse<String> setPersistence(@PathVariable("kit") String kitId,
                                                @RequestBody Kit kit) {
        return new MagmaResponse<>(deviceService.updatePersistence(kitId, kit.getPersistence()));
    }

    @RequestMapping(value = "/core/kit/{kit}/alert", method = RequestMethod.PUT)
    public MagmaResponse<String> setAlert(@PathVariable("kit") String kitId,
                                          @RequestBody Kit kit) {
        return new MagmaResponse<>(kitService.toggleAlertToKit(kitId, kit));
    }

    @RequestMapping(value = "/core/kit/{kit}", method = RequestMethod.PUT)
    public MagmaResponse<Kit> update(@PathVariable("kit") String kitId,
                                     @RequestBody Kit kit) {
        return new MagmaResponse<>(kitService.updateKit(kitId, kit));
    }

    @RequestMapping(value = "/core/user/{userId}/kit/{kit}", method = RequestMethod.PUT)
    public MagmaResponse<Kit> update(@PathVariable("kit") String kitId,
                                     @PathVariable("userId") String userId,
                                     @RequestBody Kit kit) {
        return new MagmaResponse<>(kitService.editKit(kitId, kit, userId));
    }

    @RequestMapping(value = "/core/kit/{kit}/property/{propertyNumber}", method = RequestMethod.GET)
    public MagmaResponse<List<Property>> getPropertiesHistory(@PathVariable("kit") String kitId,
                                                              @PathVariable("propertyNumber") Integer propertyNumber,
                                                              @RequestParam(value = "from", required = false) String from,
                                                              @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(kitService.findPropertyHistoryByKitAndNumber(kitId, propertyNumber, from, to));
    }

    @RequestMapping(value = "/core/kit/{kit}/geo", method = RequestMethod.GET)
    public MagmaResponse<List<Geo>> getLocationsHistory(@PathVariable("kit") String kitId,
                                                        @RequestParam(value = "from", required = false) String from,
                                                        @RequestParam(value = "to", required = false) String to,
                                                        @RequestParam(value = "type", required = false) GeoType type) {
        return new MagmaResponse<>(coreService.findGeoHistoryByKit(kitId, from, to, type));
    }

    @RequestMapping(value = "/core/kit/{kit}/property/{propertyNumber}/alert-limit", method = RequestMethod.GET)
    public MagmaResponse<List<AlertLimit>> getCurrentPropertyAlertLimit(@PathVariable("kit") String kitId,
                                                                        @PathVariable("propertyNumber") Integer propertyNumber) {
        return new MagmaResponse<>(kitService.findCurrentPropertyAlertLimitByKitAndNumber(kitId, propertyNumber));
    }

    @RequestMapping(value = "/core/kit/{kit}/graph-kit-history/{number}", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, Object>> getGraphDataOfKitByNumber(@PathVariable("kit") String kitId,
                                                                            @PathVariable("number") int number,
                                                                            @RequestParam(value = "from", required = false) String from,
                                                                            @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.getGraphDataOfKitByNumber(kitId, from, to, number));
    }

    @RequestMapping(value = "/core/kit/{kit}/graph-kit-history/{number}/summary", method = RequestMethod.GET)
    public MagmaResponse<HashMap<String, Object>> getGraphDataOfKitByNumberSummary(@PathVariable("kit") String kitId,
                                                                                   @PathVariable("number") int number,
                                                                                   @RequestParam(value = "from", required = false) String from,
                                                                                   @RequestParam(value = "enableStat", required = false) boolean enableStat,
                                                                                   @RequestParam(value = "to", required = false) String to,
                                                                                   @RequestParam(value = "aggregation", required = false) Aggregation aggregation,
                                                                                   @RequestParam(value = "interval", required = false) Integer interval,
                                                                                   @RequestParam(value = "timeOfDay", required = false) Integer hourOfDay,
                                                                                   @RequestParam(value = "format", required = false) String format,
                                                                                   @RequestParam(value = "granularity", required = false) Granularity granularity) {
        if (interval == null) {
            interval = 1;
        }
        return new MagmaResponse<>(coreService.getGraphDataOfKitByNumber(format, kitId, from, to, number, aggregation, interval, granularity, enableStat, hourOfDay));
    }

    @RequestMapping(value = "/core/kit/{kit}/trend-line", method = RequestMethod.GET)
    public MagmaResponse<LinkedList<HashMap<String, Object>>> getTrendLineOfKit(@PathVariable("kit") String kitId,
                                                                                @RequestParam(value = "from", required = false) String from,
                                                                                @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.getTrendLineOfKit(kitId, from, to));
    }

    @RequestMapping(value = "/core/kit/{kit}/offline-alerts", method = RequestMethod.GET)
    public MagmaResponse<List<Offline>> getOfflineAlertsOfKit(@PathVariable("kit") String kitId,
                                                              @RequestParam(value = "from", required = false) String from,
                                                              @RequestParam(value = "to", required = false) String to) {
        return new MagmaResponse<>(coreService.getOfflineAlertsOfKit(kitId, from, to));
    }

}
