package com.magma.core.service;

import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.Kit;
import org.springframework.stereotype.Service;

@Service("kitLbsService")
public class KitLbsServiceCore extends KitLbsService {

    @Override
    public Geo findLbs(Kit kit) {
        return null;
    }
}