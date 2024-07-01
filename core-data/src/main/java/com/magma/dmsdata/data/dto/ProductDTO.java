package com.magma.dmsdata.data.dto;

import com.magma.dmsdata.data.support.ProductVersion;
import com.magma.dmsdata.util.DeviceCategory;
import com.magma.dmsdata.util.ProductType;


public class ProductDTO {

    private String productId;

    private ProductType productType;

    private DeviceCategory deviceCategory;

    private ProductVersion version;


    public ProductDTO() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public DeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(DeviceCategory deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(ProductVersion version) {
        this.version = version;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
}
