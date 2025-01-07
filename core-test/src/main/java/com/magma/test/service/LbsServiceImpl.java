package com.magma.test.service;

import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.Kit;
import com.magma.core.service.KitLbsService;
import org.springframework.stereotype.Service;

@Service("kitLbsService")
public class LbsServiceImpl extends KitLbsService {

    @Override
    public Geo findLbs(Kit kit) {
        return null;
    }
}
