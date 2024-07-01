package com.magma.core.service;


import com.magma.core.data.entity.Geo;
import com.magma.core.data.entity.Kit;

public abstract class KitLbsService {
    public abstract Geo findLbs(Kit kit);
}
