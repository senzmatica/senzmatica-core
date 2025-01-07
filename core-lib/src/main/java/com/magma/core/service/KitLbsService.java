package com.magma.core.service;


import com.magma.dmsdata.data.entity.Geo;
import com.magma.dmsdata.data.entity.Kit;

public abstract class KitLbsService {
    public abstract Geo findLbs(Kit kit);
}
