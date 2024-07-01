package com.magma.core.api;

import com.magma.core.data.dto.KitModelDTO;
import com.magma.core.data.entity.KitModel;
import com.magma.core.service.CoreService;
import com.magma.core.service.KitModelService;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class KitModelApi {

    @Autowired
    CoreService coreService;

    @Autowired
    KitModelService kitModelService;

    @RequestMapping(value = "/core/kit-model", method = RequestMethod.POST)
    public MagmaResponse<KitModel> createKitModel(@RequestBody KitModelDTO kitModelDTO) {
        return new MagmaResponse<>(kitModelService.create(kitModelDTO));
    }

    @RequestMapping(value = "/core/kit-model/{kitModel}", method = RequestMethod.GET)
    public MagmaResponse<KitModel> getKitModel(@PathVariable("kitModel") String kitModelId) {
        return new MagmaResponse<>(kitModelService.findKitModelById(kitModelId));
    }

    @RequestMapping(value = "/core/kit-model/{kitModel}", method = RequestMethod.PUT)
    public MagmaResponse<KitModel> updateKitModel(@PathVariable("kitModel") String kitModelId,
                                                  @RequestBody KitModel kitModel) {
        return new MagmaResponse<>(kitModelService.update(kitModelId, kitModel));
    }

    @RequestMapping(value = "/core/kit-model/{kitModel}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteKitModel(@PathVariable("kitModel") String kitModelId) {
        return new MagmaResponse<>(kitModelService.deleteKitModel(kitModelId));
    }

    @RequestMapping(value = "/core/kit-model", method = RequestMethod.GET)
    public MagmaResponse<List<KitModel>> getKitModels() {
        return new MagmaResponse<>(kitModelService.findKitModels());
    }
}
