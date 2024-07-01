package com.magma.test.service;

import com.magma.core.data.entity.Geo;
import com.magma.core.data.entity.Kit;
import com.magma.core.service.KitLbsService;
import org.springframework.stereotype.Service;

@Service("kitLbsService")
public class LbsServiceImpl extends KitLbsService {

    @Override
    public Geo findLbs(Kit kit) {
        return null;
    }
}
