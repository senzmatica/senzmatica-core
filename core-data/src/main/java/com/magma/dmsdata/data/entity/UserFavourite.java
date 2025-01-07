package com.magma.dmsdata.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class UserFavourite {

    @Id
    private String id;

    private String userId;

    private List<String> favouriteDevices;

    private List<String> favouriteProducts;

    public UserFavourite(String id, String userId, List<String> favouriteDevices, List<String> favouriteProducts) {
        this.id = id;
        this.userId = userId;
        this.favouriteDevices = favouriteDevices;
        this.favouriteProducts = favouriteProducts;
    }

    public UserFavourite() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFavouriteDevices() {
        return favouriteDevices;
    }

    public void setFavouriteDevices(List<String> favouriteDevices) {
        this.favouriteDevices = favouriteDevices;
    }

    public void addFavouriteDevice(String deviceId) {
        this.favouriteDevices.add(deviceId);
    }

    public void removeFavouriteDevice(String deviceId) {
        this.favouriteDevices.remove(deviceId);
    }

    public List<String> getFavouriteProducts() {
        return favouriteProducts;
    }

    public void setFavouriteProducts(List<String> favouriteProducts) {
        this.favouriteProducts = favouriteProducts;
    }

    public void addFavouriteProduct(String productId) {
        this.favouriteProducts.add(productId);
    }

    public void removeFavouriteProduct(String productId) {
        this.favouriteProducts.remove(productId);
    }

}
