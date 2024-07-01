package com.magma.dmsdata.data.entity;

import org.springframework.data.annotation.Id;

import java.util.List;

public class ProductTypes {

    @Id
    private String productType;

    private List<String> deviceCategory;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public List<String> getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(List<String> deviceCategory) {
        this.deviceCategory = deviceCategory;
    }
}
