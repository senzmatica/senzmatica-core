package com.magma.core.service;

import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.dto.ProductDTO;
import com.magma.core.data.dto.ProductTypeDTO;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.*;
import com.magma.core.util.*;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Value("${bin.directory}")
    private String binDir;

    @Value("${bin.url}")
    private String binUrl;

    @Autowired
    MQTTConfiguration.MqttGateway mqttGateway;

    @Value("${mqtt.pub.topic}")
    private String mqttPubTopic;

    @Autowired
    ProductCoreRepository productRepository;

    @Autowired
    ProductDataRepository productDataRepository;

    @Autowired
    ProductTypesRepository productTypesRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceParameterConfigurationUtil deviceParameterConfigurationUtil;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    KitRepository kitRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductCore addNewProduct(ProductDTO productDTO, MultipartFile file) {
        logger.debug("Add new Product/ Add new Version to existing product Request");

        // Validation of Product
        if (productDTO.getProductType() == null || productDTO.getDeviceCategory() == null ||
                productDTO.getVersion() == null || productDTO.getVersion().getDevices() == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        ProductVersion version = productDTO.getVersion();
        if (file != null) {
            saveFile(file, version);
        }

        validateDeviceIds(productDTO.getVersion().getDevices());

        ProductCore product = validateProductVersion(productDTO.getProductType(), productDTO.getDeviceCategory(), version.getVersionNum());

        // Validation of the remote configurations in the request
        List<ProductParameter> remoteConfigurations = version.getRemoteConfigurations();
        deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(remoteConfigurations);

        // When adding a New Product / new Version to an existing product, set test results to pending & version status to Not_approved
        Map<String, TestResult> versionTestResults = new HashMap<>();
        for (String deviceId : version.getDevices()) {
            versionTestResults.put(deviceId, TestResult.PENDING);
        }
        version.setStatus(ProductStatus.NOT_APPROVED);
        version.setDeviceTestResults(versionTestResults);

        // Create a new product if not exists OR add version to already existing product

        if (product == null) {
            product = new ProductCore();
            BeanUtils.copyProperties(productDTO, product);
            List<ProductVersion> versions = new ArrayList<>();
            versions.add(version);
            product.setVersions(versions);
        } else {
            product.getVersions().add(version);
        }
        ProductCore savedProduct;
        savedProduct = productRepository.save(product);
        return savedProduct;
    }

    // Validate device IDs in the request
    private void validateDeviceIds(List<String> deviceIds) {
        for (String deviceId : deviceIds) {
            Device device = deviceRepository.findOne(deviceId);
            if (device == null) {
                throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
            }
        }
    }

    // Validate version number for existing product
    private ProductCore validateProductVersion(ProductType productType, DeviceCategory deviceCategory, String versionNum) {
        ProductCore product = productRepository.findByProductTypeAndDeviceCategory(productType, deviceCategory);
        if (product != null && product.getVersions().stream().anyMatch(version -> version.getVersionNum().equals(versionNum))) {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_ALREADY_EXISTS);
        }
        return product;
    }

    private void saveFile(MultipartFile file, ProductVersion version) {
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(binDir, file.getOriginalFilename());
            java.nio.file.Files.write(path, bytes);

            version.setBinURL(binUrl + file.getOriginalFilename());
        } catch (IOException e) {
            logger.debug("Exception in storing file in VM:", e);
        }
    }

    public ProductCore editProduct(String productId, ProductDTO productDTO, MultipartFile file, String user) {
        logger.debug("Edit Product Request for product Id: {}", productId);

        // Validation of the product
        ProductCore productDB = productRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        try {
            // Update attributes of the product
            if (productDTO.getProductType() != null) {
                productDB.setProductType(productDTO.getProductType());
            }
            if (productDTO.getDeviceCategory() != null) {
                productDB.setDeviceCategory(productDTO.getDeviceCategory());
            }

            ProductVersion versionFromRequest = productDTO.getVersion();
            // Find or create the version
            ProductVersion versionFromDB = productDB.getVersions().stream()
                    .filter(productVersion -> productVersion.getVersionNum().equals(versionFromRequest.getVersionNum()))
                    .findFirst().orElseGet(() -> {
                        // Create a new version if not found
                        ProductVersion newVersion = new ProductVersion();
                        newVersion.setVersionNum(versionFromRequest.getVersionNum());
                        productDB.getVersions().add(newVersion);
                        return newVersion;
                    });


            if (versionFromRequest.getFileName() != null) {
                versionFromDB.setFileName(versionFromRequest.getFileName());
            }

            if (versionFromRequest.getMajorVersionUpgrade() != null) {
                versionFromDB.setMajorVersionUpgrade(versionFromRequest.getMajorVersionUpgrade());
            }
            if (versionFromRequest.getFlowChartURL() != null) {
                versionFromDB.setFlowChartURL(versionFromRequest.getFlowChartURL());
            }
            versionFromDB.setStatus(versionFromRequest.getStatus() != null ? versionFromRequest.getStatus() : ProductStatus.NOT_APPROVED);
            if (versionFromRequest.getDevices() != null && !versionFromRequest.getDevices().isEmpty()) {
                validateDeviceIds(versionFromRequest.getDevices());
                versionFromDB.setDevices(versionFromRequest.getDevices());
            }
            if (versionFromRequest.getJoinParameters() != null) {
                versionFromDB.setJoinParameters(versionFromRequest.getJoinParameters());
            }

            logger.debug("Difference: {}", areRemoteConfigurationsEqual(versionFromDB.getRemoteConfigurations(), versionFromRequest.getRemoteConfigurations()));

            // Validate and reset Test results To pending when edit parameters
            if (!areRemoteConfigurationsEqual(versionFromDB.getRemoteConfigurations(), versionFromRequest.getRemoteConfigurations())) {
                // Code to handle differences in remote configurations
                deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(versionFromRequest.getRemoteConfigurations());
                versionFromDB.setRemoteConfigurations(versionFromRequest.getRemoteConfigurations());

                List<ProductParameter> filteredList = versionFromDB.getRemoteConfigurations().stream()
                        .filter(config -> "1".equals(config.getId()))
                        .collect(Collectors.toList());

                if (!filteredList.isEmpty()) {
                    versionFromDB.setServerIpAddress(deviceParameterConfigurationUtil.extractIpAddress(filteredList.get(0).getDefaultValue()));
                }
            }
            // Update attributes of the version
            if (file != null) {
                saveFile(file, versionFromDB);
                versionFromDB.setDeviceTestResults(createNewDeviceTestResults(versionFromDB.getDevices()));
                productRepository.save(productDB);
                updateDeviceProductVersion(versionFromDB.getDevices(), productId, versionFromDB.getVersionNum(), versionFromDB.getMajorVersionUpgrade(), user);
                return productDB;
            }
        } catch (MagmaException e) {
            logger.error("Error while updating product: {}", e.getMessage());
            if (e.getStatus() != MagmaStatus.MQTT_EXCEPTION_IN_VERSION_UPGRADE) {
                throw e;
            }
        } catch (Exception e) {
            logger.error("Error while updating product:", e);
            throw new MagmaException(MagmaStatus.PRODUCT_UPDATE_FAILED);
        }

        productRepository.save(productDB);
        return productDB;
    }

    private Map<String, TestResult> createNewDeviceTestResults(List<String> deviceIds) {
        Map<String, TestResult> newDeviceTestResults = new HashMap<>();
        for (String deviceId : deviceIds) {
            newDeviceTestResults.put(deviceId, TestResult.PENDING);
        }
        return newDeviceTestResults;
    }

    private boolean areRemoteConfigurationsEqual(List<ProductParameter> list1, List<ProductParameter> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }
        // Compare each element in the lists
        for (int i = 0; i < list1.size(); i++) {
            ProductParameter param1 = list1.get(i);
            ProductParameter param2 = list2.get(i);
            if (!param1.isEqual(param2)) {
                return false;
            }
        }
        return true;
    }

    public List<ProductCore> findAllProduct() {
        logger.debug("GET Request to get all products");
        return productRepository.findAll();
    }

    public ProductCore findProduct(String productId) {
        logger.debug("GET Request to find a product, product id:{}", productId);

        ProductCore productDB = productRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        return productDB;
    }

    //todo: need to check other fields need to be updated
    public Map<String, List<Object>> getConfigurationDetails() {
        logger.debug("Get Valid Details of Existing products and versions");

        List<ProductCore> allProduct = productRepository.findAll();
        Map<String, List<Object>> basicDetails = new HashMap<>();
        List<String> availableFlowCharts = new ArrayList<>();
        List<String> availableBinFiles = new ArrayList<>();
        List<String> availableVersions = new ArrayList<>();

        //Collect flowChart & Bin Url from already existing versions of Product
        for (ProductCore product : allProduct) {
            product.getVersions().forEach(productVersion -> {
                if (!availableFlowCharts.contains(productVersion.getFlowChartURL())) {
                    availableFlowCharts.add(productVersion.getFlowChartURL());
                }
                if (!availableVersions.contains(productVersion.getVersionNum())) {
                    availableVersions.add(productVersion.getVersionNum());
                }
                if (!availableBinFiles.contains(productVersion.getBinURL())) {
                    availableBinFiles.add(productVersion.getBinURL());
                }
            });
        }

        List<ProductTypes> productTypes = productTypesRepository.findAll();
        //Prepare and return a Map that contains all available details
        basicDetails.put("productTypes", Arrays.asList(productTypes));
        basicDetails.put("deviceCategory", Arrays.asList(DeviceCategory.values()));
        basicDetails.put("available flowCharts", Collections.singletonList(availableFlowCharts));
        basicDetails.put("available versions", Collections.singletonList(availableVersions));
        basicDetails.put("available binFiles", Collections.singletonList(availableBinFiles));
        basicDetails.put("remoteConfigurations", Arrays.asList(new String[]{"Network & Communication", "Topic Format & Interval", "Message Format"}));
        return basicDetails;
    }

    public List<ProductTypes> addNewProductTypes(List<ProductTypes> productTypes) {
        List<ProductTypes> savedProducts = productTypesRepository.save(productTypes);
        return savedProducts;
    }

    public List<ProductTypes> getAllProductTypes() {
        return productTypesRepository.findAll();
    }

    public String deleteProductTypes(String productTypesId) {
        productTypesRepository.delete(productTypesId);
        return "Successfully Deleted";
    }

    public ProductTypes updateProductTypes(String productTypesId, ProductTypes productTypes) {
        ProductTypes requestedProduct = productTypesRepository.findOne(productTypesId);
        if (requestedProduct == null) {
            throw new MagmaException(MagmaStatus.NOT_FOUND);
        } else {
            requestedProduct.setDeviceCategory(productTypes.getDeviceCategory());
            productTypesRepository.save(requestedProduct);
        }
        return requestedProduct;
    }

    public ProductCore changeStatusOfTheVersion(String productId, String versionNum, ProductStatus status, String changedBy) {
        logger.debug("Change Version Of the Product for Product:{} Version:{} New Status:{} ChangedBy:{}", productId, versionNum, status, changedBy);

        DateTime dateTimeNow = MagmaTime.now();
        List<Device> devicesWithProduct = deviceRepository.findByProductProductId(productId);

        for (Device device : devicesWithProduct) {


            ProductData product = device.getProduct();
            if (product != null) {
                String availableProductVersionsString = product.getAvailableProductVersions();
                Set<String> uniqueVersions = new HashSet<>(Arrays.asList(availableProductVersionsString.substring(1, availableProductVersionsString.length() - 1).split(", ")));
                // Check if the new version is not already in the set
                if (!uniqueVersions.contains(versionNum)) {
                    // Add the new version to the set
                    uniqueVersions.add(versionNum);
                    product.setAvailableProductVersions(uniqueVersions.toString());
                }

                if (Objects.equals(product.getCurrentProductVersion(), versionNum)) {
                    product.setCurrentVersionStatus(String.valueOf(status));
                }
                device.setProduct(product);
                deviceRepository.save(device);
            }
        }

        //Validation of Product
        ProductCore product = productRepository.findOne(productId);
        if (product == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        //Validation of Version Number
        List<ProductVersion> matchVersions = product.getVersions().stream().filter(productVersion -> productVersion.getVersionNum().equals(versionNum)).collect(Collectors.toList());
        if (matchVersions.isEmpty()) {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
        }
        matchVersions.get(0).setStatus(status);
        matchVersions.get(0).setStatusChangedBy(changedBy);

        //Change In  Associated devices
        List<Device> allDevices = deviceRepository.findAll();
        for (Device d : allDevices) {
            ProductData productData = d.getProduct();

            if (productData != null && productData.getProductId().equals(productId)
                    && productData.getCurrentProductVersion().equals(versionNum)) {
                productData.setActionBy(changedBy);
                productData.setDate(dateTimeNow.toString());
                productData.setCurrentVersionStatus(status.toString());
                d.setProduct(productData);
            }
        }
        deviceRepository.save(allDevices);
        return productRepository.save(product);
    }

    private void sendMessageToDevice(String deviceId, String message, String versionNumber, DeviceParameterConfiguration deviceParameterConfigurationDb, String productId) {
        List<ProductParameter> remoteConfigurations = deviceParameterConfigurationDb.getRemoteConfigurations();
        String productParameterIdToFind = "11"; // remote config topic parameter
        Optional<String> remoteConfigTopicOptional = remoteConfigurations.stream()
                .filter(param -> productParameterIdToFind.equals(param.getId()))
                .map(ProductParameter::getDefaultValue)
                .findFirst();
        if (remoteConfigTopicOptional.isPresent()) {
            String remoteConfigTopic = remoteConfigTopicOptional.get();
            List<Message> messages = messageRepository.findMessagesByDevice(deviceId);

            int maxTopicNumber = messages.stream()
                    .map(Message::getTopicNumber)
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0);
            logger.debug("maxTopicNumber:{} ", maxTopicNumber);
            String topic = remoteConfigTopic.replace("#", String.valueOf(maxTopicNumber + 1));
            logger.debug("topic:{} ", topic);
            try {
                mqttGateway.send(topic, true, message);
                changeTestResultsOfDeviceInsideVersion(productId, versionNumber, deviceId, TestResult.PENDING);
                Message existingMessage = messageRepository.findById(deviceId + "-" + (maxTopicNumber + 1));
                if (existingMessage != null) {
                    existingMessage.setMessage(message);
                    messageRepository.save(existingMessage);
                } else {
                    DeviceParameterConfigurationHistory his = null;
                    Message newMessage = new Message(deviceId, his, String.valueOf(maxTopicNumber + 1), message);
                    messageRepository.save(newMessage);
                }
            } catch (Exception e) {
                logger.debug("Exception happened", e);
                throw new MagmaException(MagmaStatus.MQTT_EXCEPTION_IN_VERSION_UPGRADE);
            }
        }
    }

    /**
     * Rejects the specified product version for a list of devices, updating their product data and triggering
     * a message to revert devices to the previous version .
     */
    public String rejectDeviceProductVersion(ProductDTO productDTO) {
        // Extract information from the provided ProductDTO
        List<String> deviceIds = productDTO.getVersion().getDevices();
        String productId = productDTO.getProductId();
        String versionNumber = productDTO.getVersion().getVersionNum();

        // Validate input parameters
        if (deviceIds.isEmpty() || productId == null || versionNumber == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        // Retrieve the product information from the database
        ProductCore productDB = productRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        // Retrieve the list of product versions associated with the product
        List<ProductVersion> versionsDB = productDB.getVersions();
        if (versionsDB == null || versionsDB.isEmpty()) {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
        }

        // Iterate through the list of affected devices and perform necessary updates
        for (String deviceId : deviceIds) {
            Device device = deviceRepository.findOne(deviceId);
            if (device != null) {
                // Retrieve product data for the device
                ProductData productData = device.getProduct();
                String previousVersionNum = productData.getPreviousVersion();
                String[] versionsArray = productData.getAllProductVersionsOfDevice().split(",");
                Set<String> uniqueVersions = new HashSet<>();

                // Filter out the current product version from the list of versions
                for (String value : versionsArray) {
                    if (!value.equals(productData.getCurrentProductVersion())) {
                        uniqueVersions.add(value);
                    }
                }

                // Concatenate the unique versions into a comma-separated string
                String allProductVersionsOfDevice = String.join(",", uniqueVersions);

                // Retrieve the previous version details from the database
                List<ProductVersion> previousVersion = versionsDB.stream()
                        .filter(productVersion -> productVersion.getVersionNum().equals(previousVersionNum))
                        .collect(Collectors.toList());

                // Throw an exception if the previous version is not found
                if (previousVersion.isEmpty()) {
                    throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
                }

                // Retrieve the device parameter configuration for the device
                DeviceParameterConfiguration deviceParameterConfigurationDb = device.getDeviceParameterConfiguration();

                // Throw an exception if the device parameter configuration is not found
                if (deviceParameterConfigurationDb == null) {
                    throw new MagmaException(MagmaStatus.DEVICE_PARAMETER_CONFIGURATION_NOT_FOUND);
                } else {
                    // Generate and send a rejection message to the device with the previous version
                    String messageToDevice = "RM|98-1;97-" + previousVersion.get(0).getBinURL() + ";|END";
                    logger.debug("Generated Message: {}", messageToDevice);
                    sendMessageToDevice(deviceId, messageToDevice, previousVersionNum, deviceParameterConfigurationDb, productId);

                    // Update product data for the device and save changes
                    productData.setAllProductVersionsOfDevice(allProductVersionsOfDevice);
                    productData.setPreviousVersion(versionNumber);
                    productData.setCurrentProductVersion(previousVersionNum);
                    changeTestResultsOfDeviceInsideVersion(productId, versionNumber, deviceId, TestResult.PENDING);
                    productData.setCurrentVersionStatus(TestResult.PENDING.toString());
                    productDataRepository.save(productData);
                }
            }
        }

        // Return a success message indicating that the product version has been rejected successfully
        return "Product Version Rejected Successfully";
    }

    /**
     * Compares two version strings in a numeric-aware manner.
     *
     * @return An integer representing the comparison result: 0 if equal, negative if version1 < version2, positive if version1 > version2.
     */
    private static int compareVersions(String version1, String version2) {
        // Split the version strings into parts based on dot separator
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        // Determine the minimum length of the version parts
        int minLength = Math.min(parts1.length, parts2.length);

        // Iterate through the version parts and compare them numerically
        for (int i = 0; i < minLength; i++) {
            if (!parts1[i].equals(parts2[i])) {
                try {
                    int num1 = Integer.parseInt(parts1[i]);
                    int num2 = Integer.parseInt(parts2[i]);
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    // If parsing as integers fails, compare as strings
                    return parts1[i].compareTo(parts2[i]);
                }
            }
        }
        // If all common parts are equal, compare based on the remaining parts
        return Integer.compare(parts1.length, parts2.length);
    }

    public void updateDeviceProductVersion(List<String> deviceIds, String productId, String versionNumber, Boolean majorVersionUpgrade, String actionBy) {
        logger.debug("update Device's Product version request Devices:{} , Product:{} , Version:{}", deviceIds.toString(), productId, versionNumber);
        if (deviceIds.isEmpty() || productId == null || versionNumber == null) {
            logger.debug("update Device's Product version request, Invalid DeviceId OR Product Id OR Version");
            return;
        }

        ProductCore productDB = productRepository.findOne(productId);
        if (productDB == null) {
            logger.debug("update Device's Product version request, Product Not Found");
            return;
        }

        List<ProductVersion> versionsDB = productDB.getVersions();
        if (versionsDB == null || versionsDB.isEmpty()) {
            logger.debug("update Device's Product version request, Versions Not Found In product");
            return;
        }
        List<ProductVersion> version = versionsDB.stream().filter(productVersion -> productVersion.getVersionNum().equals(versionNumber)).collect(Collectors.toList());
        if (version.isEmpty()) {
            logger.debug("update Device's Product version request, Version Not Found");
            return;
        }
        //Collect all available versions of the product
        List<String> availableVersionsInProduct = new ArrayList<>();
        for (ProductVersion productVersion : productDB.getVersions()) {
            if (productVersion.getStatus().equals(ProductStatus.APPROVED)) {
                availableVersionsInProduct.add(productVersion.getVersionNum());
            }
        }

        //Update Device's Product details and send Message
        for (String deviceId : deviceIds) {

            Device device = deviceRepository.findOne(deviceId);
            if (device != null) {

                if (device.getProduct() == null) {
                    device.setProduct(new ProductData());
                }
                ProductData productData = device.getProduct();
                productData.setMajorVersionUpgrade(majorVersionUpgrade);
                //Prepare ProductData object to hold product related details for new devices
                if (productData.getDeviceId() == null) {
                    productData.setDeviceId(deviceId);
                }

                if (productData.getPreviousVersion() == null) {
                    productData.setPreviousVersion("");
                }

                if (productData.getProductId() == null) {
                    productData.setProductId(productId);
                }

                if (productData.getProductType() == null) {
                    productData.setProductType(productDB.getProductType().toString());
                }

                if (productData.getAvailableProductVersions() == null) {
                    productData.setAvailableProductVersions(availableVersionsInProduct.toString());
                }

                if (productData.getCurrentVersionStatus() == null) {
                    productData.setCurrentVersionStatus(TestResult.PENDING.toString());
                }

                DeviceParameterConfiguration deviceParameterConfigurationDb = device.getDeviceParameterConfiguration();
                if (deviceParameterConfigurationDb == null) {
                    // Handle the case where deviceParameterConfigurationDb is null
                    logger.error("Device Parameter Configuration not found for device: {}", deviceId);
                    throw new MagmaException(MagmaStatus.DEVICE_PARAMETER_CONFIGURATION_NOT_FOUND);
                } else {
                    int comparison = compareVersions(versionNumber, productData.getCurrentProductVersion());
                    String messageToDevice;

                    if (comparison > 0) {
                        String message1ToDevice = "RM|97-" + version.get(0).getBinURL() + ";|END";
                        if (majorVersionUpgrade) {
                            String message2ToDevice = "RM|99-1;|END";
                            sendMessageToDevice(deviceId, message1ToDevice, versionNumber, deviceParameterConfigurationDb, productId);
                            sendMessageToDevice(deviceId, message2ToDevice, versionNumber, deviceParameterConfigurationDb, productId);
                            logger.debug("Generated Messages: {}, {}", message1ToDevice, message2ToDevice);
                        } else {
                            sendMessageToDevice(deviceId, message1ToDevice, versionNumber, deviceParameterConfigurationDb, productId);
                            logger.debug("Generated Message: {}", message1ToDevice);
                        }
                    } else {
                        messageToDevice = "RM|98-1;97-" + version.get(0).getBinURL() + ";|END";
                        sendMessageToDevice(deviceId, messageToDevice, versionNumber, deviceParameterConfigurationDb, productId);
                        logger.debug("Generated Message: {}", messageToDevice);
                    }

                    String allProductVersionsOfDevice = productData.getAllProductVersionsOfDevice();
                    String currentVersion = productData.getCurrentProductVersion();

                    if (allProductVersionsOfDevice.isEmpty()) {
                        productData.setAllProductVersionsOfDevice(currentVersion);
                    } else {
                        Set<String> allAlreadyExistingVersionsOfDevice = new HashSet<>(Arrays.asList(allProductVersionsOfDevice.split(",")));

                        if (!allAlreadyExistingVersionsOfDevice.contains(currentVersion)) {
                            allAlreadyExistingVersionsOfDevice.add(currentVersion);
                            String updatedProductVersions = String.join(",", allAlreadyExistingVersionsOfDevice);
                            productData.setAllProductVersionsOfDevice(updatedProductVersions);
                        }
                    }

                    List<OTAUpgradeHistory> otaHistory = productData.getOtaHistory();

                    if (otaHistory == null) {
                        otaHistory = new ArrayList<>();
                    }

                    OTAUpgradeHistory ota = new OTAUpgradeHistory();
                    ota.setCurrentVersion(versionNumber);
                    ota.setPreviousVersion(productData.getCurrentProductVersion());
                    ota.setUpdatedDate(DateTime.now());
                    ota.setActionBy(actionBy);
                    otaHistory.add(ota);

                    if (otaHistory.size() == 1) {
                        productData.setOtaHistory(otaHistory);
                    }

                    if (productData.getCurrentProductVersion() == null) {
                        productData.setCurrentProductVersion(versionNumber);
                    }

                    productData.setProductId(productId);
                    productData.setPreviousVersion(productData.getCurrentProductVersion());
                    productData.setCurrentProductVersion(versionNumber);
                    productData.setAvailableProductVersions(availableVersionsInProduct.toString());
                    productData.setCurrentVersionStatus(ProductStatus.APPROVED.toString());
                    productDataRepository.save(productData);

                }
            }

        }
    }

    public String updateDeviceProductVersionBulk(List<ProductDTO> productDTOS, String actionBy) {
        logger.debug("update Device's Product version Bulk request");

        //Validation of Product Ids and Validation of versions
        for (ProductDTO productDTO : productDTOS) {
            ProductCore productDB = findProduct(productDTO.getProductId());
            List<ProductVersion> version = productDB.getVersions().stream().filter(productVersion -> productVersion.getVersionNum().equals(productDTO.getVersion().getVersionNum())).collect(Collectors.toList());
            if (version.isEmpty()) {
                throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
            }
        }

        // Update Version
        for (ProductDTO productDTO : productDTOS) {
            try {
                updateDeviceProductVersion(
                        productDTO.getVersion().getDevices(),
                        productDTO.getProductId(),
                        productDTO.getVersion().getVersionNum(),
                        productDTO.getVersion().getMajorVersionUpgrade(),
                        actionBy
                );
            } catch (Exception e) {
                logger.error("Error updating product version for productId: {}, versionNum: {}", productDTO.getProductId(), productDTO.getVersion().getVersionNum(), e);
                throw new MagmaException(MagmaStatus.EXCEPTION_IN_PRODUCT_VERSION_UPGRADE);
            }
        }

        // If the loop completes without any errors, return success message
        return "Product Versions are Updated!!";
    }

    public void changeTestResultsOfDeviceInsideVersion(String productId, String version, String deviceId, TestResult result) {
        ProductCore product = productRepository.findOne(productId);
        if (product == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        List<ProductVersion> versions = product.getVersions().stream()
                .filter(productVersion -> version.equals(productVersion.getVersionNum()))
                .collect(Collectors.toList());

        if (versions.isEmpty()) {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
        }

        // Check if the device exists in the version
        ProductVersion versionToUpdate = versions.get(0);
        if (versionToUpdate != null) {
            Map<String, TestResult> deviceTestResults = versionToUpdate.getDeviceTestResults();

            if (deviceTestResults != null && deviceTestResults.containsKey(deviceId)) {
                // Update the result for the specified device
                deviceTestResults.put(deviceId, result);
                // Save changes back to the repository
                productRepository.save(product);
            }

        } else {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
        }
    }

    /**
     * Handles the device message to update the product version of a device. This method is triggered by a successful OTA
     * download of a new current version. It updates the device's product information, changes the test result of the device
     * inside the version, and adjusts the device's remote configurations if it is a major version upgrade.
     */
    public void doHandleProductVersionUpdates(String tempDeviceId) {
        logger.debug("Device -{} To System Message to Update the product version of device", tempDeviceId);

        // Retrieve the current device information
        Device currentDevice = Optional.ofNullable(deviceRepository.findOne(tempDeviceId))
                .orElseGet(() -> deviceRepository.findByCustomPublishTopicOrCustomRemoteTopic(tempDeviceId));

        // Check if the device is found
        if (currentDevice == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }

        // Extract relevant information from the current device
        String deviceId = currentDevice.getId();
        ProductData product = productDataRepository.findOne(deviceId);

        logger.debug("New current version OTA download Success");
        changeTestResultsOfDeviceInsideVersion(currentDevice.getProduct().getProductId(), product.getCurrentProductVersion(), deviceId, TestResult.SUCCESS);

        // Update the device's product information and device parameter configuration
        currentDevice.setProduct(product);
        DeviceParameterConfiguration currentConfiguration = currentDevice.getDeviceParameterConfiguration();
        logger.debug("Current Version Number:{} ", product.getCurrentProductVersion());

        // Retrieve the list of versions associated with the product
        List<ProductVersion> versions = productRepository.findOne(product.getProductId()).getVersions();
        ProductVersion currentVersion = versions.stream()
                .filter(version -> version.getVersionNum().equals(product.getCurrentProductVersion()))
                .collect(Collectors.toList()).get(0);

        // Handle adjustments for major version upgrades
        if (product.getMajorVersionUpgrade()) {
            List<ProductParameter> currentRemoteConfigurations = currentConfiguration.getRemoteConfigurations();
            List<ProductParameter> newRemoteConfigurations = currentVersion.getRemoteConfigurations();
            List<String> idsToReplace = Arrays.asList("9", "10", "11");

            // Iterate through the current remote configurations and replace specific parameters if found in the new version
            for (ProductParameter currentParameter : currentRemoteConfigurations) {
                String parameterId = currentParameter.getId();
                if (idsToReplace.contains(parameterId)) {
                    int index = -1;
                    for (int i = 0; i < newRemoteConfigurations.size(); i++) {
                        if (Objects.equals(newRemoteConfigurations.get(i).getId(), parameterId)) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        newRemoteConfigurations.set(index, currentParameter);
                    }
                }
            }

            // Update the device parameter configuration with adjusted remote configurations
            currentConfiguration.setRemoteConfigurations(newRemoteConfigurations);
            currentDevice.setDeviceParameterConfiguration(currentConfiguration);
        }

        // Save the updated device information
        deviceRepository.save(currentDevice);
    }


    //Get Devices According to the product related filters
    public List<Device> devicesWithRequiredFilters(List<String> deviceIds, String productType,
                                                   String versionStatus, String currentVersion,
                                                   List<String> previousDeviceVersion,
                                                   String clientName) {
        logger.debug("Get devices with required product filters deviceIds:{},productType:{},versionStatus:{}," +
                "currentVersion:{},previousVersions:{},client:{}", deviceIds, productType, versionStatus, currentVersion, previousDeviceVersion, clientName);


        List<Device> devicesToReturn = new ArrayList<>();

        //Fetch Only Required Devices
        if (deviceIds == null || deviceIds.isEmpty() || (deviceIds.size() == 1 && deviceIds.get(0).equals("[]"))) {
            devicesToReturn = deviceRepository.findAll();
        } else {
            devicesToReturn = deviceRepository.findByIdIn(deviceIds);
        }

        //Ignore Other filters
        if (productType == null && versionStatus == null && currentVersion == null && previousDeviceVersion == null && clientName == null) {
            return devicesToReturn;
        }

        List<Device> devicesToRemove = new ArrayList<>();

        for (Device device : devicesToReturn) {
            ProductData product = device.getProduct();

            //Any filter Not applicable
            if (product == null) {
                devicesToRemove.add(device);
                continue;
            }

            // Product type filter
            if (productType != null && product.getProductType() == null) {
                devicesToRemove.add(device);
            }
            if (productType != null && product.getProductType() != null && !product.getProductType().equals(productType)) {
                devicesToRemove.add(device);
            }


            //Client Filter
            if (clientName != null && (device.getReferences() == null || !device.getReferences().containsKey("client"))) {
                devicesToRemove.add(device);
            }
            if (clientName != null && device.getReferences() != null && device.getReferences().containsKey("client") && !device.getReferences().get("client").equals(clientName)) {
                devicesToRemove.add(device);
            }

            // Product version filter
            if (currentVersion != null && product.getCurrentProductVersion() == null) {
                devicesToRemove.add(device);
            }
            if (currentVersion != null && product.getCurrentProductVersion() != null && !product.getCurrentProductVersion().equals(currentVersion)) {
                devicesToRemove.add(device);
            }

// Product version status filter
            if (versionStatus != null && product.getCurrentVersionStatus() == null) {
                devicesToRemove.add(device);
            }
            if (versionStatus != null && product.getCurrentVersionStatus() != null && !product.getCurrentVersionStatus().equals(versionStatus)) {
                devicesToRemove.add(device);
            }

// Previous versions filter
            if (previousDeviceVersion != null && !previousDeviceVersion.isEmpty() && product.getAllProductVersionsOfDevice() == null) {
                devicesToRemove.add(device);
            }
            if (previousDeviceVersion != null && !previousDeviceVersion.isEmpty() && product.getAllProductVersionsOfDevice() != null) {
                List<String> existingVersionsInDevice = Arrays.asList(product.getAllProductVersionsOfDevice().split(","));
                for (String filterVersion : previousDeviceVersion) {
                    if (!existingVersionsInDevice.contains(filterVersion)) {
                        devicesToRemove.add(device);
                    }
                }
            }

        }
        devicesToReturn.removeAll(devicesToRemove);
        return devicesToReturn;
    }

    // Get remote configurations of the  for a given productId
    public List<ProductParameter> getRemoteConfigurationsByProductTypeCategoryVersion(ProductType productType, DeviceCategory deviceCategory, String version) {
        ProductCore product = productRepository.findByProductTypeAndDeviceCategory(productType, deviceCategory);
        if (product == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        try {

            ProductVersion versionFromDB = product.getVersions().stream()
                    .filter(productVersion -> productVersion.getVersionNum().equals(version))
                    .findFirst()
                    .orElseThrow(() -> new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND));

            return versionFromDB.getRemoteConfigurations();

        } catch (NullPointerException e) {

            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }
    }


    // -------------------------------------- SETUP SENZMATICA IMPLEMENTATION ----------------------------------------

    public ProductTypes addOneProductType(ProductTypeDTO productTypes) {
        if (!productTypes.addValidate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        ProductTypes existingProductType = productTypesRepository.findByProductName(productTypes.getProductName());
        if (existingProductType != null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_ALREADY_EXIST);
        }
        ProductTypes productType = new ProductTypes();
        BeanUtils.copyProperties(productTypes, productType);
        return productTypesRepository.save(productType);
    }

    public ProductTypes updateProductType(String productTypeId, ProductTypeDTO productTypeDTO) {
        ProductTypes requestedProductType = productTypesRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }

        if (!productTypeDTO.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        ProductTypes existingProductType = productTypesRepository.findByProductName(productTypeDTO.getProductName());
        if (existingProductType != null && !existingProductType.getId().equals(productTypeDTO.getId())) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_ALREADY_EXIST);
        }

        ProductTypes productTypes = new ProductTypes();
        BeanUtils.copyProperties(productTypeDTO, productTypes);

        return productTypesRepository.save(productTypes);
    }

    public ProductTypes getOneProductTypeById(String productTypeId) {
        ProductTypes requestedProductType = productTypesRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }
        return requestedProductType;
    }

    public String deleteOneProductType(String productTypeId) {
        ProductTypes requestedProductType = productTypesRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }

        productTypesRepository.delete(productTypeId);
        return "Success";
    }

    public SetupSenzmatica getSetupSenzmaticaStatus() {
        logger.debug("Setup Senzmatica");

        SetupSenzmatica setupSenzmatica = new SetupSenzmatica();

        List<ProductTypes> productTypes = productTypesRepository.findAll();
        List<Device> devices = deviceRepository.findAll();
        List<Kit> kits = kitRepository.findAll();
        if (!productTypes.isEmpty()) {
            setupSenzmatica.setStep1Completed(true);
        }
        if (setupSenzmatica.getStep1Completed() && !devices.isEmpty()) {
            setupSenzmatica.setStep2Completed(true);
        }
        if (setupSenzmatica.getStep2Completed() && !kits.isEmpty()) {
            setupSenzmatica.setStep3Completed(true);
        }

        logger.debug("Setup Senzmatica : {}", setupSenzmatica);
        return setupSenzmatica;
    }
}




