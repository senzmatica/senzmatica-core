package com.magma.core.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.data.dto.ProductDTO;
import com.magma.core.data.dto.ProductTypeDTO;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.ProductCore;
import com.magma.core.data.entity.ProductTypes;
import com.magma.core.data.support.ProductParameter;
import com.magma.core.data.support.ProductVersion;
import com.magma.core.data.support.SetupSenzmatica;
import com.magma.core.service.ProductService;
import com.magma.core.service.UserFavouriteService;
import com.magma.core.util.*;
import com.magma.util.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class ProductApi {

    @Autowired
    ProductService productService;

    @Autowired
    UserFavouriteService userFavouriteService;

    @RequestMapping(value = "/core/product", method = RequestMethod.POST, consumes = "multipart/form-data")
    public MagmaResponse<ProductCore> addProduct(@RequestParam(value = "product") String product,
                                                 @RequestParam(value = "binFile", required = false) MultipartFile binFile) {
        ProductDTO productDTO;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON version string into a ProductDTO object
            productDTO = objectMapper.readValue(product, ProductDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        return new MagmaResponse<>(productService.addNewProduct(productDTO, binFile));
    }

    @RequestMapping(value = "/core/product/{productId}", method = RequestMethod.GET)
    public MagmaResponse<ProductCore> getProduct(@PathVariable("productId") String productId) {
        return new MagmaResponse<>(productService.findProduct(productId));
    }

    @RequestMapping(value = "/core/product/{productType}/{deviceCategory}/{version:.+}", method = RequestMethod.GET)
    public MagmaResponse<List<ProductParameter>> getRemoteConfigurations(@PathVariable("productType") String productType,
                                                                         @PathVariable("deviceCategory") String deviceCategory,
                                                                         @PathVariable("version") String version) {
        // Validate productType and deviceCategory against enums
        ProductType productTypeEnum;
        try {
            productTypeEnum = ProductType.valueOf(productType);
        } catch (IllegalArgumentException e) {
            throw new MagmaException(MagmaStatus.INVALID_PRODUCT_TYPE);
        }

        DeviceCategory deviceCategoryEnum;
        try {
            deviceCategoryEnum = DeviceCategory.valueOf(deviceCategory);
        } catch (IllegalArgumentException e) {
            throw new MagmaException(MagmaStatus.INVALID_DEVICE_CATEGORY);
        }

        // return remote configurations for the given product type,device category and version
        return new MagmaResponse<>(productService.getRemoteConfigurationsByProductTypeCategoryVersion(productTypeEnum, deviceCategoryEnum, version));
    }

    /**
     * Edit Product
     * Updates an existing product with new information and optional binary file upload.
     */
    @RequestMapping(value = "/core/product/{productId}/user/{user}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    public MagmaResponse<ProductCore> editProduct(@RequestParam(value = "binFile", required = false) MultipartFile binFile,
                                                  @RequestParam(value = "version") String version,
                                                  @PathVariable("productId") String productId,
                                                  @PathVariable("user") String user) {
        ProductDTO productDTO = new ProductDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        ProductVersion productVersion;

        try {
            // Parse the JSON version string into a ProductVersion object
            productVersion = objectMapper.readValue(version, ProductVersion.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        productDTO.setVersion(productVersion);
        return new MagmaResponse<>(productService.editProduct(productId, productDTO, binFile, user));
    }


    @RequestMapping(value = "/core/products", method = RequestMethod.GET)
    public MagmaResponse<List<ProductCore>> getAllProducts() {
        return new MagmaResponse<>(productService.findAllProduct());
    }

    @RequestMapping(value = "/core/product/{userId}/favourite", method = RequestMethod.GET)
    public MagmaResponse<List<String>> getAllFavouriteProducts(@PathVariable("userId") String userId) {
        return new MagmaResponse<>(userFavouriteService.getUserFavouriteProducts(userId));
    }

    @RequestMapping(value = "/core/product/{userId}/favourite", method = RequestMethod.POST)
    public MagmaResponse<String> addProductsAsUserFavourite(@PathVariable("userId") String userId,
                                                            @RequestBody List<String> productIds) {
        return new MagmaResponse<>(userFavouriteService.addProductsAsFavouriteBulk(userId, productIds));
    }

    @RequestMapping(value = "/core/product/{userId}/favourite", method = RequestMethod.DELETE)
    public MagmaResponse<String> removeProductsFromUserFavourite(@PathVariable("userId") String userId,
                                                                 @RequestBody List<String> productIds) {
        return new MagmaResponse<>(userFavouriteService.removeProductsFromFavouriteListBulk(userId, productIds));
    }

    @RequestMapping(value = "/core/product/configDetails", method = RequestMethod.GET)
    public MagmaResponse<Map<String, List<Object>>> productConfigurationDetails() {
        return new MagmaResponse<>(productService.getConfigurationDetails());
    }

    @RequestMapping(value = "/core/product/productTypes", method = RequestMethod.POST)
    public MagmaResponse<List<ProductTypes>> addNewProductTypes(@RequestBody List<ProductTypes> productTypes) {
        return new MagmaResponse<>(productService.addNewProductTypes(productTypes));
    }

    @RequestMapping(value = "/core/product/productTypes", method = RequestMethod.GET)
    public MagmaResponse<List<ProductTypes>> getAllProductTypes() {
        return new MagmaResponse<>(productService.getAllProductTypes());
    }

    @RequestMapping(value = "/core/product/productTypes/{productTypesId}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteProductTypes(@PathVariable("productTypesId") String productTypesId) {
        return new MagmaResponse<>(productService.deleteProductTypes(productTypesId));
    }

    @RequestMapping(value = "/core/product/productTypes/{productTypesId}", method = RequestMethod.PUT)
    public MagmaResponse<ProductTypes> updateProductTypes(@RequestBody ProductTypes productTypes,
                                                          @PathVariable("productTypesId") String productTypesId) {
        return new MagmaResponse<>(productService.updateProductTypes(productTypesId, productTypes));
    }

    @RequestMapping(value = "/core/product/{productId}/version/{version}/status/{status}/{changedBy}", method = RequestMethod.PUT)
    public MagmaResponse<ProductCore> changeStatusOfVersion(@PathVariable("productId") String productId,
                                                            @PathVariable("version") String version,
                                                            @PathVariable("status") ProductStatus status,
                                                            @PathVariable("changedBy") String changedBy) {
        return new MagmaResponse<>(productService.changeStatusOfTheVersion(productId, version, status, changedBy));
    }

    @RequestMapping(value = "/core/product/version/{actionBy}/update", method = RequestMethod.PUT)
    public MagmaResponse<String> updateProductVersionInDevice(@RequestBody List<ProductDTO> productDTOS,
                                                              @PathVariable String actionBy) {
        return new MagmaResponse<>(productService.updateDeviceProductVersionBulk(productDTOS, actionBy));
    }

    @RequestMapping(value = "/core/product/version/reject", method = RequestMethod.PUT)
    public MagmaResponse<String> rejectProductVersion(@RequestBody ProductDTO productDTO) {
        return new MagmaResponse<>(productService.rejectDeviceProductVersion(productDTO));
    }

    @RequestMapping(value = "/core/product/devices", method = RequestMethod.GET)
    public MagmaResponse<List<Device>> getDevicesByProductRelatedFilters(@RequestParam(value = "deviceIds", required = false) List<String> devices,
                                                                         @RequestParam(value = "productType", required = false) String productType,
                                                                         @RequestParam(value = "versionStatus", required = false) String versionStatus,
                                                                         @RequestParam(value = "currentVersion", required = false) String currentVersion,
                                                                         @RequestParam(value = "client", required = false) String clientName,
                                                                         @RequestParam(value = "previousVersions", required = false) List<String> previousVersions) {
        return new MagmaResponse<>(productService.devicesWithRequiredFilters(devices, productType, versionStatus, currentVersion, previousVersions, clientName));

    }


    // -------------------------------------- SETUP SENZMATICA IMPLEMENTATION ----------------------------------------

    @RequestMapping(value = "/core/product/productType", method = RequestMethod.POST)
    public MagmaResponse<ProductTypes> addNewProductTypes(@RequestBody @Valid ProductTypeDTO productTypeDTO,
                                                          BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(productService.addOneProductType(productTypeDTO));
    }

    @RequestMapping(value = "/core/product/productType/{productTypeId}", method = RequestMethod.GET)
    public MagmaResponse<ProductTypes> getOneProductTypes(@PathVariable("productTypeId") String productTypeId) {
        return new MagmaResponse<>(productService.getOneProductTypeById(productTypeId));
    }

    @RequestMapping(value = "/core/product/productType/{productTypeId}", method = RequestMethod.PUT)
    public MagmaResponse<ProductTypes> updateProductTypes(@PathVariable("productTypeId") String productTypeId,
                                                          @RequestBody ProductTypeDTO newProductTypeDTO) {
        return new MagmaResponse<>(productService.updateProductType(productTypeId, newProductTypeDTO));
    }

    @RequestMapping(value = "/core/product/productType/{productTypeId}", method = RequestMethod.DELETE)
    public MagmaResponse<String> deleteProductType(@PathVariable("productTypeId") String productTypeId) {
        return new MagmaResponse<>(productService.deleteOneProductType(productTypeId));
    }

    @RequestMapping(value = "/core/setupSenzmatica", method = RequestMethod.GET)
    public MagmaResponse<SetupSenzmatica> setupSenzmatica() {
        return new MagmaResponse<>(productService.getSetupSenzmaticaStatus());
    }
}
