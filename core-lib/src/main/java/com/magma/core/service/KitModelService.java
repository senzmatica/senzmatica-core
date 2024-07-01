package com.magma.core.service;

import com.magma.core.data.dto.KitModelDTO;
import com.magma.core.data.entity.KitModel;
import com.magma.core.data.repository.KitModelRepository;
import com.magma.core.util.KitUtil;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KitModelService {
    @Autowired
    KitModelRepository kitModelRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreService.class);

    public KitModel create(KitModelDTO kitModelDTO) {
        LOGGER.debug("Create KitModel request found : {}", kitModelDTO);

        KitModel kitModel = new KitModel();
        BeanUtils.copyProperties(kitModelDTO, kitModel);


        if (!kitModel.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (!KitUtil.validateModel(kitModel)) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        if (StringUtils.isEmpty(kitModel.getId())) {
            String id = KitUtil.createId(kitModel);
            int x = 0;
            String id_;

            do {
                id_ = id + x;
                x++;
            } while (kitModelRepository.findOne(id_) != null);

            kitModel.setId(id_);
        } else {
            if (kitModelRepository.findOne(kitModel.getId()) != null) {
                throw new MagmaException(MagmaStatus.KIT_MODEL_ALREADY_EXISTS);
            }
        }
        KitUtil.distributeOperations(kitModel);

        return kitModelRepository.save(kitModel);
    }

    public KitModel update(String kitModelId, KitModel kitModel) {
        LOGGER.debug("Update KitModel request found Id : {}, Model : {}", kitModelId, kitModel);

        if (kitModelRepository.findOne(kitModelId) == null) {
            throw new MagmaException(MagmaStatus.KIT_MODEL_NOT_FOUND);
        }

        if (!kitModel.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }

        if (!KitUtil.validateModel(kitModel)) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        kitModel.setId(kitModelId);
        KitUtil.distributeOperations(kitModel);

        return kitModelRepository.save(kitModel);
    }

    public String deleteKitModel(String kitModelId) {
        LOGGER.debug("Delete request found kitModelId : {}", kitModelId);
        kitModelRepository.delete(kitModelId);
        return "Successfully Updated";
    }

    public List<KitModel> findKitModels() {
        LOGGER.debug("Find all KitModels request found");

        return kitModelRepository.findAll();
    }

    public KitModel findKitModelById(String kitModelId) {
        LOGGER.debug("Find KitModel request found : {}", kitModelId);
        KitModel kitModel = kitModelRepository.findOne(kitModelId);

        if (kitModel == null) {
            throw new MagmaException(MagmaStatus.KIT_MODEL_NOT_EXISTS);
        }
        return kitModel;
    }
}
