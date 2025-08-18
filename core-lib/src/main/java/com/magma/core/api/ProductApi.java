package com.magma.core.api;

import com.magma.core.data.dto.ProductDTO;
import com.magma.core.data.dto.ProductTypeDTO;
import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.ProductType;
import com.magma.core.data.repository.ProductTypeRepository;
import com.magma.core.data.support.RemoteConfigField;
import com.magma.core.data.support.SetupSenzmatica;
import com.magma.core.service.ProductService;
import com.magma.core.service.UserFavouriteService;
import com.magma.core.util.ProductStatus;
import com.magma.core.validation.BadRequestException;
import com.magma.util.MagmaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)  //TODO: Need To Specify Domain
public class ProductApi {

    @Autowired
    ProductService productService;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    UserFavouriteService userFavouriteService;

    /**
     * Adds a new version for a product type, including the binary file and version details.
     *
     * @param binFile The binary file representing the new version.
     * @param version The JSON string containing version details.
     * @param productId The ID of the product type to which the version will be added.
     * @param user The user who is adding the version.
     * @return A response containing the updated ProductTypes object.
     */
    @RequestMapping(value = "/core/user/{user}/product/{productId}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public MagmaResponse<ProductType> addProductVersion(@RequestParam(value = "binFile",required = false) MultipartFile binFile, @RequestParam(value = "version") String version, @PathVariable("productId") String productId,
                                                        @PathVariable("user") String user) {
        return new MagmaResponse<>(productService.addNewProductVersion(productId,version, binFile, user));
    }

    @RequestMapping(value = "/core/product/{productId}", method = RequestMethod.GET)
    public MagmaResponse<ProductType> getProduct(@PathVariable("productId") String productId) {
        return new MagmaResponse<>(productService.findProduct(productId));
    }

    @RequestMapping(value = "/core/product/{productType}/{version:.+}", method = RequestMethod.GET)
    public MagmaResponse<List<RemoteConfigField>> getRemoteConfigurations(@PathVariable("productType") String productTypeName,
                                                                          @PathVariable("version") String version) {
        // return remote configurations for the given product type and version
        return new MagmaResponse<>(productService.getRemoteConfigurationsByProductTypeVersion(productTypeName, version));
    }

    /**
     * Edits an existing version of a product type, optionally updating the binary file and version details.
     * Only Version is updated. Fields specific to the product cannot be edited
     * @param binFile The binary file representing the updated version. This parameter is optional and can be null.
     * @param version The JSON string containing the updated version details.
     * @param productId The ID of the product type whose version is being edited.
     * @param user The user who is making the edits to the product version.
     * @return A response containing the updated ProductTypes object.
     */
    @RequestMapping(value = "/core/product/{productId}/user/{user}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    public MagmaResponse<ProductType> editProductVersion(@RequestParam(value = "binFile", required = false) MultipartFile binFile,
                                                         @RequestParam(value = "version") String version,
                                                         @PathVariable("productId") String productId,
                                                         @PathVariable("user") String user) {
        return new MagmaResponse<>(productService.editProductVersion(productId, version, binFile, user));
    }


    @RequestMapping(value = "/core/products", method = RequestMethod.GET)
    public MagmaResponse<List<ProductType>> getAllProducts() {
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

    @RequestMapping(value = "/core/product/{productId}/version/{version}/status/{status}/{changedBy}", method = RequestMethod.PUT)
    public MagmaResponse<ProductType> changeStatusOfVersion(@PathVariable("productId") String productId,
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

    @RequestMapping(value = "/core/product/productType", method = RequestMethod.POST,
                    consumes = {"multipart/form-data"})
    public MagmaResponse<ProductType> addNewProductType(@RequestPart("productTypeDTO") @Valid ProductTypeDTO productTypeDTO,
                                                        @RequestPart(value = "binFile", required = false) MultipartFile binFile, BindingResult result) {

        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return new MagmaResponse<>(productService.addOneProductType(productTypeDTO));
    }

    @RequestMapping(value = "/core/product/productType/{productTypeId}", method = RequestMethod.GET)
    public MagmaResponse<ProductType> getOneProductType(@PathVariable("productTypeId") String productTypeId) {
        return new MagmaResponse<>(productService.getOneProductTypeById(productTypeId));
    }

    @RequestMapping(value = "/core/product/productType/{productTypeId}", 
                    method = RequestMethod.PUT, 
                    consumes = {"multipart/form-data"})
    public MagmaResponse<ProductType> updateProductType(@PathVariable("productTypeId") String productTypeId,
                                                        @RequestPart("productTypeDTO") @Valid ProductTypeDTO newProductTypeDTO,
                                                        @RequestPart(value = "binFile", required = false) MultipartFile binFile) {
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
