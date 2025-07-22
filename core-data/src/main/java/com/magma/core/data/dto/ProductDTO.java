package com.magma.core.data.dto;

import com.magma.core.data.entity.ProductType;
import com.magma.core.data.support.ProductVersion;


public class ProductDTO {

    private String productId;

    private ProductType productType;

    private ProductVersion version;

    private Boolean majorVersionUpgrade;

    public ProductDTO() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public Boolean getMajorVersionUpgrade() {
        return majorVersionUpgrade;
    }

    public void setMajorVersionUpgrade(Boolean majorVersionUpgrade) {
        this.majorVersionUpgrade = majorVersionUpgrade;
    }
}
