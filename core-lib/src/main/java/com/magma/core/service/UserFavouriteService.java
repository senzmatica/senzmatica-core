package com.magma.core.service;

import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.entity.ProductType;
import com.magma.dmsdata.data.entity.UserFavourite;
import com.magma.core.data.repository.DeviceRepository;
import com.magma.core.data.repository.UserFavouriteRepository;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserFavouriteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserFavouriteService.class);

    @Autowired
    private UserFavouriteRepository userFavouriteRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ProductService productService;

    public List<Device> getAllDevicesWithFavouriteOrder(String userId) {
        LOGGER.debug("Get All  Devices With Favourite Order Request For User:{}", userId);
        List<Device> allDevicesWithFavouriteOrder = new ArrayList<>();

        List<String> favouriteDevicesIds = new ArrayList<>();

        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        //First add Favourite Devices if exists
        if (userFavourite != null) {
            favouriteDevicesIds = userFavouriteRepository.findByUserId(userId).getFavouriteDevices();
            allDevicesWithFavouriteOrder.addAll(deviceRepository.findByIdIn(favouriteDevicesIds));
        }
        allDevicesWithFavouriteOrder.addAll(deviceRepository.findByIdNotIn(favouriteDevicesIds));
        return allDevicesWithFavouriteOrder;
    }

    public void updateDeviceAsFavourite(String userId, String deviceId) {
        LOGGER.debug("Update Devices as favourite  Request For User:{}, Device:{}", userId, deviceId);
        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            userFavourite = new UserFavourite();
            userFavourite.setUserId(userId);
        }
        if (userFavourite.getFavouriteProducts() == null) {
            userFavourite.setFavouriteProducts(new ArrayList<>());
        }
        if (userFavourite.getFavouriteDevices() == null) {
            userFavourite.setFavouriteDevices(new ArrayList<>());
        }
        //Check Device ID Validation
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        List<String> existFavouriteDevices = userFavourite.getFavouriteDevices();

        if (existFavouriteDevices.contains(deviceId)) {
            throw new MagmaException(MagmaStatus.ALREADY_MARKED_AS_FAVOURITE);
        } else {
            userFavourite.addFavouriteDevice(deviceId);
            userFavouriteRepository.save(userFavourite);
        }
    }

    public String addProductsAsFavouriteBulk(String userId, List<String> productIds) {
        LOGGER.debug("Add Products as User's Favourite Request For User:{}, Products:{}", userId, productIds);
        if (productIds.isEmpty()) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            userFavourite = new UserFavourite();
            userFavourite.setUserId(userId);
        }

        if (userFavourite.getFavouriteProducts() == null) {
            userFavourite.setFavouriteProducts(new ArrayList<>());
        }
        if (userFavourite.getFavouriteDevices() == null) {
            userFavourite.setFavouriteDevices(new ArrayList<>());
        }

        //Check Product IDs Validation
        for (String productId : productIds) {
            ProductType product = productService.findProduct(productId);
        }

        List<String> existFavouriteProducts = userFavourite.getFavouriteProducts();
        for (String productId : productIds) {
            if (!existFavouriteProducts.contains(productId)) {
                userFavourite.addFavouriteProduct(productId);
            }
        }
        userFavouriteRepository.save(userFavourite);
        return "Products added as Favourite!";
    }

    public void RemoveDeviceAsFavourite(String userId, String deviceId) {
        //Validate DeviceId:
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            throw new MagmaException(MagmaStatus.FAVOURITE_DEVICES_NOT_EXISTS);
        }

        List<String> favouriteDevicesIds = userFavourite.getFavouriteDevices();

        if (!favouriteDevicesIds.contains(deviceId)) {
            throw new MagmaException(MagmaStatus.NOT_MARKED_AS_FAVOURITE);
        } else {
            userFavourite.removeFavouriteDevice(deviceId);
            userFavouriteRepository.save(userFavourite);
        }
    }

    public String removeProductsFromFavouriteListBulk(String userId, List<String> productIds) {
        LOGGER.debug("Remove Products from favourite  Request For User:{}, Product:{}", userId, productIds);
        if (productIds.isEmpty()) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        //Check Product IDs Validation
        for (String productId : productIds) {
            ProductType product = productService.findProduct(productId);

        }

        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            throw new MagmaException(MagmaStatus.FAVOURITE_PRODUCTS_NOT_EXISTS);
        }

        List<String> favouriteProductsIds = userFavourite.getFavouriteProducts();

        for (String productId : productIds) {
            if (favouriteProductsIds.contains(productId)) {
                userFavourite.removeFavouriteProduct(productId);
            }
        }
        userFavouriteRepository.save(userFavourite);
        return "Products removed from favoriteProducts!";
    }

    public List<String> getUserFavouriteProducts(String userId) {
        LOGGER.debug("Get Favourite Products Request for user:{}", userId);
        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            return new ArrayList<>();
        }
        return userFavourite.getFavouriteProducts();
    }

    public List<String> getUserFavouriteDevices(String userId) {
        LOGGER.debug("Get Favourite Devices Request for user:{}", userId);

        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            return new ArrayList<>();
        }
        return userFavourite.getFavouriteDevices();
    }

    public List<String> addBulkDevicesAsFavourite(String userId, List<String> deviceIds) {
        LOGGER.debug("Add Bulk favourite devices request for user {}, devices:{}", userId, deviceIds);

        //Validate DeviceIds
        for (String device : deviceIds) {
            Device deviceDb = deviceRepository.findById(device).orElse(null);
            if (deviceDb == null) {
                throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
            }
        }
        //Create UserFavourite If Not Exists and other arrays
        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null) {
            userFavourite = new UserFavourite();
            userFavourite.setUserId(userId);
        }
        if (userFavourite.getFavouriteProducts() == null) {
            userFavourite.setFavouriteProducts(new ArrayList<>());
        }
        if (userFavourite.getFavouriteDevices() == null) {
            userFavourite.setFavouriteDevices(new ArrayList<>());
        }

        //Add devices as Favourite if Not already marked as favourite
        List<String> existFavouriteDevices = userFavourite.getFavouriteDevices();
        for (String deviceId : deviceIds) {
            if (!existFavouriteDevices.contains(deviceId)) {
                userFavourite.addFavouriteDevice(deviceId);
            }
        }

        UserFavourite newSetOfDevices = userFavouriteRepository.save(userFavourite);
        return newSetOfDevices.getFavouriteDevices();
    }

    public List<String> removeDevicesFromFavourite(String userId, List<String> deviceIds) {

        //Validate Favourite Devices
        UserFavourite userFavourite = userFavouriteRepository.findByUserId(userId);

        if (userFavourite == null || userFavourite.getFavouriteDevices() == null || userFavourite.getFavouriteDevices().isEmpty()) {
            throw new MagmaException(MagmaStatus.FAVOURITE_DEVICES_NOT_EXISTS);
        }

        //Validate Each ID whether is favourite Or Not
        List<String> favouriteDevicesIds = userFavourite.getFavouriteDevices();

        for (String deviceId : deviceIds) {
            if (!favouriteDevicesIds.contains(deviceId)) {
                throw new MagmaException(MagmaStatus.NOT_MARKED_AS_FAVOURITE);
            }
        }

        //End Of Validation Remove DeviceId From Favourite

        for (String deviceId : deviceIds) {
            userFavourite.removeFavouriteDevice(deviceId);
        }
        UserFavourite newSetOfDevices = userFavouriteRepository.save(userFavourite);
        return newSetOfDevices.getFavouriteDevices();
    }
}
