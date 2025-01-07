package com.magma.dmsdata.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.magma.dmsdata.data.support.ProductVersion;
import com.magma.dmsdata.util.DeviceCategory;
import com.magma.dmsdata.util.ProductType;

import java.util.List;

@Document
public class ProductCore {

    @Id
    private String id;

    private ProductType productType;

    private DeviceCategory deviceCategory;

    private List<ProductVersion> versions;

    public ProductCore() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(DeviceCategory deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public List<ProductVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ProductVersion> versions) {
        this.versions = versions;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public void addVersion(ProductVersion version) {
        this.versions.add(version);
    }
}
