package com.magma.core.service;

import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.ProductCore;
import com.magma.core.data.entity.UserFavourite;
import com.magma.core.data.repository.DeviceRepository;
import com.magma.core.data.repository.UserFavouriteRepository;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
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
            ProductCore product = productService.findProduct(productId);
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

    public String removeProductsFromFavouriteListBulk(String userId, List<String> productIds) {
        LOGGER.debug("Remove Products from favourite  Request For User:{}, Product:{}", userId, productIds);
        if (productIds.isEmpty()) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        //Check Product IDs Validation
        for (String productId : productIds) {
            ProductCore product = productService.findProduct(productId);

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
            return new ArrayList<String>();
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
}
