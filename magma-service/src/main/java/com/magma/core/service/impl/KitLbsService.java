package com.magma.core.service.impl;

import com.magma.core.data.entity.Kit;
import com.magma.core.data.entity.Geo;

public abstract class KitLbsService {

    public abstract Geo findLbs(Kit kit);

}
