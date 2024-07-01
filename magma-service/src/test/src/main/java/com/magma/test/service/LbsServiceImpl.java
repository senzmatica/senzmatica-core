package com.magma.test.service;

import com.magma.service.data.entity.Geo;
import com.magma.service.data.entity.Kit;
import com.magma.service.service.KitLbsService;
import org.springframework.stereotype.Service;

@Service("kitLbsService")
public class LbsServiceImpl extends KitLbsService {

    @Override
    public Geo findLbs(Kit kit) {
        return null;
    }
}
