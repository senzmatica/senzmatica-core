package com.magma.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.configuration.MQTTConfiguration;
import com.magma.core.data.dto.ProductDTO;
import com.magma.core.data.dto.ProductTypeDTO;
import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.*;
import com.magma.core.util.*;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
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
    ProductDataRepository productDataRepository;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceParameterConfigurationUtil deviceParameterConfigurationUtil;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    KitRepository kitRepository;

    @Autowired
    MagmaCodecRepository magmaCodecRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductType addNewProductVersion(String productId, String versionString, MultipartFile file, String user) {
        logger.debug("Add new Product Version to existing product type Request");

        // Validate the bin file
        validateBinFile(file);

        // Parse the version string into a ProductVersion object
        ProductVersion versionFromRequest = parseProductVersion(versionString);

        // Validation of the product
        ProductType productDB = productTypeRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }
        else if (productDB.getVersions().stream().anyMatch(version -> version.getVersionNum().equals(versionFromRequest.getVersionNum()))) {
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_ALREADY_EXISTS);
        }

        try {
            // Update attributes of the product
            ProductVersion newVersion = new ProductVersion(versionFromRequest.getVersionNum());

            if (versionFromRequest.getFileName() != null) {
                newVersion.setFileName(versionFromRequest.getFileName());
            }

            if (versionFromRequest.getMajorVersionUpgrade() != null) {
                newVersion.setMajorVersionUpgrade(versionFromRequest.getMajorVersionUpgrade());
            }
            if (versionFromRequest.getFlowChartURL() != null) {
                newVersion.setFlowChartURL(versionFromRequest.getFlowChartURL());
            }
            //newVersion.setStatus(versionFromRequest.getStatus() != null ? versionFromRequest.getStatus() : ProductStatus.NOT_APPROVED);
            if (versionFromRequest.getDevices() != null && !versionFromRequest.getDevices().isEmpty()) {
                validateDeviceIds(versionFromRequest.getDevices());
                newVersion.setDevices(versionFromRequest.getDevices());
            }
            if (versionFromRequest.getJoinParameters() != null) {
                newVersion.setJoinParameters(versionFromRequest.getJoinParameters());
            }
            if (versionFromRequest.getFlowChartFileName() != null) {
                newVersion.setFlowChartFileName(versionFromRequest.getFlowChartFileName());
            }
            deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(versionFromRequest.getRemoteConfigurations());
            newVersion.setRemoteConfigurations(versionFromRequest.getRemoteConfigurations());

            saveFile(file,newVersion);
            //newVersion.setDeviceTestResults(createNewDeviceTestResults(newVersion.getDevices()));
            newVersion.setStatus(ProductStatus.APPROVED);
            newVersion.setStatusChangedBy(user);
            productDB.getVersions().add(newVersion);
            productTypeRepository.save(productDB);
            //update productData for devices with the product type
            List<Device> devicesWithProductType=deviceRepository.findByProductType(productDB.getProductName());
            List<String> availableVersionsInProduct = new ArrayList<>();
            for (ProductVersion productVersion : productDB.getVersions()) {
                if (productVersion.getStatus().equals(ProductStatus.APPROVED)&& !Objects.equals(productVersion.getVersionNum(), "0.0.0")) {
                    availableVersionsInProduct.add(productVersion.getVersionNum());
                }
            }
            for (Device device :devicesWithProductType){

                if (device != null) {

                    if (device.getProduct() == null) {
                        device.setProduct(new ProductData());
                    }
                    ProductData productData = device.getProduct();
                    if (productData.getDeviceId() == null) {
                        productData.setDeviceId(device.getId());
                    }

                    if (productData.getPreviousVersion() == null) {
                        productData.setPreviousVersion("");
                    }

                    if (productData.getProductId() == null) {
                        productData.setProductId(productId);
                    }

                    if (productData.getProductType() == null) {
                        productData.setProductType(productDB.getProductName());
                    }

                    productData.setAvailableProductVersions(availableVersionsInProduct.toString());
                    productDataRepository.save(productData);
                    device.setProduct(productData);
                    deviceRepository.save(device);
                }
            }
            productTypeRepository.save(productDB);
            return productDB;

        }  catch (Exception e) {
            logger.error("Error while adding product version:{}",productId);
            throw new MagmaException(MagmaStatus.PRODUCT_UPDATE_FAILED);
        }

    }

    private void validateBinFile(MultipartFile binFile) {
        if (binFile == null || binFile.isEmpty()) {
            throw new MagmaException(MagmaStatus.BIN_FILE_NOT_UPLOADED);
        }

        if (!binFile.getContentType().equals("application/octet-stream") && !binFile.getOriginalFilename().endsWith(".bin")) {
            throw new MagmaException(MagmaStatus.INVALID_FILE_TYPE);
        }
    }

    private ProductVersion parseProductVersion(String version) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(version, ProductVersion.class);
        } catch (IOException e) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
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

    private void saveFile(MultipartFile file, ProductVersion version) {
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(binDir, file.getOriginalFilename());
            java.nio.file.Files.write(path, bytes);
            version.setBinURL(binUrl.trim() + file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Exception in storing file in VM");
            throw new MagmaException(MagmaStatus.BIN_FILE_SAVE_ERROR);
        }
    }

    public ProductType editProductVersion(String productId, String versionString, MultipartFile file, String user) {
        logger.debug("Edit Product Request for product Id: {}", productId);

        // Parse the version JSON into a ProductVersion object
        ProductVersion versionFromRequest = parseProductVersion(versionString);

        // Validation of the product
        ProductType productDB = productTypeRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        try {
            // Find or create the version
            ProductVersion versionFromDB = productDB.getVersions().stream()
                    .filter(productVersion -> productVersion.getVersionNum().equals(versionFromRequest.getVersionNum()))
                    .findFirst().orElseGet(() -> {
                        // Create a new version if not found
                        ProductVersion newVersion = new ProductVersion(versionFromRequest.getVersionNum());
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

            logger.debug("Remote Configuration From Db and Request are Different: {}", areRemoteConfigurationsEqual(versionFromDB.getRemoteConfigurations(), versionFromRequest.getRemoteConfigurations()));

            // Validate and reset Test results To pending when edit parameters
            if (!areRemoteConfigurationsEqual(versionFromDB.getRemoteConfigurations(), versionFromRequest.getRemoteConfigurations())) {
                // Code to handle differences in remote configurations
                deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(versionFromRequest.getRemoteConfigurations());
                versionFromDB.setRemoteConfigurations(versionFromRequest.getRemoteConfigurations());

            }
            // Update attributes of the version
            if (file != null) {
                saveFile(file, versionFromDB);
            }
            //versionFromDB.setDeviceTestResults(createNewDeviceTestResults(versionFromDB.getDevices()));
            productDB.getVersions().add(versionFromDB);
            productTypeRepository.save(productDB);
            updateDeviceProductVersion(versionFromDB.getDevices(), productId, versionFromDB.getVersionNum(), versionFromDB.getMajorVersionUpgrade(), user);
            return productDB;

        } catch (Exception e) {
            logger.error("Exception while updating product:{}",productId);
            throw new MagmaException(MagmaStatus.PRODUCT_UPDATE_FAILED);
        }

    }

    private boolean areRemoteConfigurationsEqual(List<RemoteConfigField> list1, List<RemoteConfigField> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }
        // Compare each element in the lists
        for (int i = 0; i < list1.size(); i++) {
            RemoteConfigField param1 = list1.get(i);
            RemoteConfigField param2 = list2.get(i);
            if (!param1.isEqual(param2)) {
                return false;
            }
        }
        return true;
    }

    public List<ProductType> findAllProduct() {
        logger.debug("GET Request to get all products");
        return productTypeRepository.findAll();
    }

    public ProductType findProduct(String productId) {
        logger.debug("GET Request to find a product, product id:{}", productId);

        ProductType productDB = productTypeRepository.findOne(productId);
        if (productDB == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        return productDB;
    }

    //todo: need to check other fields need to be updated
    public Map<String, List<Object>> getConfigurationDetails() {
        logger.debug("Get Valid Details of Existing products and versions");

        List<ProductType> allProduct = productTypeRepository.findAll();
        Map<String, List<Object>> basicDetails = new HashMap<>();
        List<Map<String ,String>> availableFlowCharts = new ArrayList<>();
        List<String> availableBinFiles = new ArrayList<>();
        List<String> availableVersions = new ArrayList<>();

        // Collect flowChart & Bin Url from already existing versions of Product
        for (ProductType product : allProduct) {
            List<ProductVersion> versions = product.getVersions();
            if (versions != null) {
                versions.forEach(productVersion -> {
                    String fileName=productVersion.getFlowChartFileName();
                    if(fileName!=null){
                        boolean fileNameExists = availableFlowCharts.stream()
                                .anyMatch(flowChart -> flowChart.get("fileName").equals(fileName));
                        if (productVersion.getFlowChartURL() != null && !fileNameExists) {
                            Map<String, String> flowChartURL = new HashMap<>();
                            flowChartURL.put("URL",productVersion.getFlowChartURL());
                            flowChartURL.put("fileName",fileName);
                            availableFlowCharts.add(flowChartURL);
                        }}
                    if (productVersion.getVersionNum() != null && !availableVersions.contains(productVersion.getVersionNum())) {
                        availableVersions.add(productVersion.getVersionNum());
                    }
                    if (productVersion.getBinURL() != null && !availableBinFiles.contains(productVersion.getFileName())) {
                        availableBinFiles.add(productVersion.getFileName());
                    }
                });
            }
        }

        // Prepare and return a Map that contains all available details
        List<ProductType> productTypes = productTypeRepository.findAll();
        basicDetails.put("productTypes", Arrays.asList(productTypes));
        basicDetails.put("available flowCharts", Collections.singletonList(availableFlowCharts));
        basicDetails.put("available versions", Collections.singletonList(availableVersions));
        basicDetails.put("availableBinFiles", Collections.singletonList(availableBinFiles));
        basicDetails.put("remoteConfigurations", Arrays.asList("Network & Communication", "Topic Format & Interval", "Message Format"));

        return basicDetails;
    }

    public ProductType changeStatusOfTheVersion(String productId, String versionNum, ProductStatus status, String changedBy) {
        logger.debug("Change Version Of the Product for Product:{} Version:{} New Status:{} ChangedBy:{}", productId, versionNum, status, changedBy);

        DateTime dateTimeNow = MagmaTime.now();

        List<Device> devicesWithProduct = deviceRepository.findByProductProductId(productId);
        if (devicesWithProduct == null || devicesWithProduct.isEmpty()) {
            logger.debug("No devices found with the specified product ID: {}", productId);
            return null;
        }

        for (Device device : devicesWithProduct) {
            ProductData product = device.getProduct();
            if (product != null) {
                String availableProductVersionsString = product.getAvailableProductVersions();
                if (availableProductVersionsString != null) {
                    Set<String> uniqueVersions = new HashSet<>(Arrays.asList(availableProductVersionsString.substring(1, availableProductVersionsString.length() - 1).split(", ")));
                    if (!uniqueVersions.contains(versionNum)) {
                        uniqueVersions.add(versionNum);
                        product.setAvailableProductVersions(uniqueVersions.toString());
                    }
                }

                if (Objects.equals(product.getCurrentProductVersion(), versionNum)) {
                    product.setCurrentVersionStatus(String.valueOf(status));
                }
                device.setProduct(product);
                deviceRepository.save(device);
            }
        }

        ProductType product = productTypeRepository.findOne(productId);
        if (product == null) {
            logger.error("Invalid product ID: {}", productId);
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        List<ProductVersion> matchVersions = product.getVersions() != null ? product.getVersions().stream().filter(productVersion -> productVersion.getVersionNum().equals(versionNum)).collect(Collectors.toList()) : Collections.emptyList();
        if (matchVersions.isEmpty()) {
            logger.error("Product version not found: {}", versionNum);
            throw new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND);
        }

        ProductVersion versionToUpdate = matchVersions.get(0);
        versionToUpdate.setStatus(status);
        versionToUpdate.setStatusChangedBy(changedBy);

        List<Device> allDevices = deviceRepository.findAll();
        if (allDevices != null) {
            for (Device d : allDevices) {
                ProductData productData = d.getProduct();
                if (productData != null && productId.equals(productData.getProductId()) && versionNum.equals(productData.getCurrentProductVersion())) {
                    productData.setActionBy(changedBy);
                    productData.setDate(dateTimeNow.toString());
                    productData.setCurrentVersionStatus(status.toString());
                    d.setProduct(productData);
                }
            }
            deviceRepository.save(allDevices);
        }
        return productTypeRepository.save(product);
    }


    private void sendMessageToDevice(String deviceId, String versionNumber, String productId, String dataFormat, String firstIdentifier, String firstMessage,String secondaryIdentifier, String secondaryMessage) {
        Device device = deviceRepository.findOne(deviceId);
        boolean retained = !"JSON".equalsIgnoreCase(dataFormat);

        if (device != null) {
            String otaRequestTopic = device.getOtaRequestTopic();
            List<Message> messages = messageRepository.findMessagesByDevice(deviceId);
            int maxTopicNumber = messages.stream()
                    .map(Message::getTopicNumber)
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(1);
            logger.debug("maxTopicNumber:{} ", maxTopicNumber);
            String topic = otaRequestTopic.replace("#", String.valueOf(maxTopicNumber + 1));
            logger.debug("topic:{} ", topic);
            String messageToSend;

            if ("json".equalsIgnoreCase(dataFormat)) {
                if (secondaryIdentifier != null && secondaryMessage != null) {
                    messageToSend = String.format("{\"RM\": {\"%s\": \"%s\", \"%s\": \"%s\"}}", firstIdentifier, firstMessage, secondaryIdentifier, secondaryMessage);
                } else {
                    messageToSend = String.format("{\"RM\": {\"%s\": \"%s\"}}", firstIdentifier, firstMessage);
                }
            } else {
                if (secondaryIdentifier != null && secondaryMessage != null) {
                    messageToSend = String.format("RM|%s-%s;%s-%s;|END", firstIdentifier, firstMessage,secondaryIdentifier, secondaryMessage);
                } else {
                    messageToSend = String.format("RM|%s-%s;|END", firstIdentifier, firstMessage);
                }
            }
            logger.debug("Generated Message: {}", messageToSend);

            try {
                mqttGateway.send(topic, retained, messageToSend);
                changeTestResultsOfDeviceInsideVersion(productId, versionNumber, deviceId, TestResult.PENDING);
                Message existingMessage = messageRepository.findOne(deviceId + "-" + (maxTopicNumber + 1));
                if (existingMessage != null) {
                    existingMessage.setPayload(messageToSend);
                    messageRepository.save(existingMessage);
                } else {
                    DeviceParameterConfigurationHistory his = null;
                    Message newMessage = new Message(deviceId, his, String.valueOf(maxTopicNumber + 1), messageToSend);
                    messageRepository.save(newMessage);
                }
            } catch (Exception e) {
                logger.error("Exception While Sending MQTT Message To Device :{}",deviceId);
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
        ProductType productDB = productTypeRepository.findOne(productId);
        String dataFormat = productDB.getDataFormat();
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

                sendMessageToDevice(deviceId, previousVersionNum, productId, dataFormat, "98", "1","97",previousVersion.get(0).getBinURL());

                // Update product data for the device and save changes
                productData.setAllProductVersionsOfDevice(allProductVersionsOfDevice);
                productData.setPreviousVersion(versionNumber);
                productData.setCurrentProductVersion(previousVersionNum);
                changeTestResultsOfDeviceInsideVersion(productId, versionNumber, deviceId, TestResult.PENDING);
                productData.setCurrentVersionStatus(TestResult.PENDING.toString());
                productDataRepository.save(productData);

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

        ProductType productDB = productTypeRepository.findOne(productId);
        String dataFormat = productDB.getDataFormat();

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
                    productData.setProductType(productDB.getProductName());
                }

                if (productData.getAvailableProductVersions() == null) {
                    productData.setAvailableProductVersions(availableVersionsInProduct.toString());
                }


                productData.setCurrentVersionStatus(UpdateStatus.PENDING.toString());



                int comparison = compareVersions(versionNumber, productData.getCurrentProductVersion());

                if (comparison == 0) {
                    logger.debug("Same Version Number in Request, Not Proceeding");
                    continue;
                } else if (comparison < 0) {
                    sendMessageToDevice(deviceId, versionNumber, productId, dataFormat, "98", "1", "97", version.get(0).getBinURL());
                }
                else {
                    sendMessageToDevice(deviceId, versionNumber, productId, dataFormat, "97", version.get(0).getBinURL(), null, null);
                }
                if (majorVersionUpgrade) {
                    sendMessageToDevice(deviceId, versionNumber, productId, dataFormat, "99", "1", null, null);
                }

                String allProductVersionsOfDevice = productData.getAllProductVersionsOfDevice();
                String currentVersion = productData.getCurrentProductVersion();

                if (allProductVersionsOfDevice == null || allProductVersionsOfDevice.isEmpty()) {
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

                if (otaHistory.size() > 1) {
                    productData.setOtaHistory(otaHistory);
                }
                productData.setProductId(productId);
                productData.setPreviousVersion(productData.getCurrentProductVersion());
                productData.setCurrentProductVersion(versionNumber);
                productData.setAvailableProductVersions(availableVersionsInProduct.toString());
                productDataRepository.save(productData);
                device.setProduct(productData);
                deviceRepository.save(device);

            }

        }
    }

    public String updateDeviceProductVersionBulk(List<ProductDTO> productDTOS, String actionBy) {
        logger.debug("update Device's Product version Bulk request");

        //Validation of Product Ids and Validation of versions
        for (ProductDTO productDTO : productDTOS) {
            ProductType productDB = findProduct(productDTO.getProductId());
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
                logger.error("Error updating product version for productId: {}, versionNum: {}", productDTO.getProductId(), productDTO.getVersion().getVersionNum());
                throw new MagmaException(MagmaStatus.EXCEPTION_IN_PRODUCT_VERSION_UPGRADE);
            }
        }

        // If the loop completes without any errors, return success message
        return "Product Versions are Updated!!";
    }

    public void changeTestResultsOfDeviceInsideVersion(String productId, String version, String deviceId, TestResult result) {
        ProductType product = productTypeRepository.findOne(productId);
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
                productTypeRepository.save(product);
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
    public void doHandleProductVersionUpdates(String deviceIdOrName,Boolean upgradeSuccess) {
        logger.debug("Device -{} To System Message to Update the product version of device", deviceIdOrName);

        Device device =  deviceRepository.findByIdOrName(deviceIdOrName);

        if (device == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        String IMEIId=device.getId();
        ProductData product = productDataRepository.findOne(IMEIId);
        if(!upgradeSuccess){
            product.setCurrentVersionStatus(UpdateStatus.FAILED.toString());
            productDataRepository.save(product);
            device.setProduct(product);
            deviceRepository.save(device);
            return;
        }
        product.setCurrentVersionStatus(UpdateStatus.UPDATED.toString());

        logger.debug("New current version OTA download Success");
        changeTestResultsOfDeviceInsideVersion(device.getProduct().getProductId(), product.getCurrentProductVersion(), IMEIId, TestResult.SUCCESS);
        device.setProduct(product);
        DeviceParameterConfiguration currentConfiguration = device.getDeviceParameterConfiguration();
        logger.debug("Current Version Number:{} ", product.getCurrentProductVersion());

        // Retrieve the list of versions associated with the product
        List<ProductVersion> versions = productTypeRepository.findOne(product.getProductId()).getVersions();
        ProductVersion currentVersion = versions.stream()
                .filter(version -> version.getVersionNum().equals(product.getCurrentProductVersion()))
                .collect(Collectors.toList()).get(0);

        // Handle adjustments for major version upgrades
        if (product.getMajorVersionUpgrade()) {
            List<RemoteConfigField> currentRemoteConfigurations = currentConfiguration.getRemoteConfigurations();
            List<RemoteConfigField> newRemoteConfigurations = currentVersion.getRemoteConfigurations();
            List<String> idsToReplace = Arrays.asList("9", "10", "11");

            // Iterate through the current remote configurations and replace specific parameters if found in the new version
            for (RemoteConfigField currentParameter : currentRemoteConfigurations) {
                String parameterId = currentParameter.getParameterId();
                if (idsToReplace.contains(parameterId)) {
                    int index = -1;
                    for (int i = 0; i < newRemoteConfigurations.size(); i++) {
                        if (Objects.equals(newRemoteConfigurations.get(i).getParameterId(), parameterId)) {
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

        }
        currentConfiguration.setVersionNum(product.getCurrentProductVersion());
        device.setDeviceParameterConfiguration(currentConfiguration);
        // Save the updated device information
        deviceRepository.save(device);
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
    public List<RemoteConfigField> getRemoteConfigurationsByProductTypeVersion(String productTypeName, String version) {
        ProductType product = productTypeRepository.findByProductName(productTypeName);
        if (product == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NOT_FOUND);
        }

        ProductVersion versionFromDB = product.getVersions().stream()
                .filter(productVersion -> productVersion.getVersionNum().equals(version))
                .findFirst()
                .orElseThrow(() -> new MagmaException(MagmaStatus.PRODUCT_VERSION_NOT_FOUND));


        List<RemoteConfigField> remoteConfigurations = versionFromDB.getRemoteConfigurations();
        if (remoteConfigurations == null) {
            throw new MagmaException(MagmaStatus.REMOTE_CONFIGURATIONS_NOT_FOUND);
        }

        return remoteConfigurations;
    }



    // -------------------------------------- SETUP SENZMATICA IMPLEMENTATION ----------------------------------------

    public ProductType addOneProductType(ProductTypeDTO productTypeDto) {
        if(productTypeDto.getActuatorCodes().length == 0 && productTypeDto.getSensorCodes().length == 0){
            throw new MagmaException(MagmaStatus.Sensor_Actuator_Not_Exist);
        }
        if (!productTypeDto.addValidate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }
        ProductType existingProductType = productTypeRepository.findByProductName(productTypeDto.getProductName());
        if (existingProductType != null) {
            throw new MagmaException(MagmaStatus.PRODUCT_NAME_ALREADY_EXIST);
        }
        ProductVersion version=productTypeDto.getVersions().get(0);
        deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(version.getRemoteConfigurations());

        ProductType productType = new ProductType(productTypeDto.getProductName());
        BeanUtils.copyProperties(productTypeDto, productType);
        return productTypeRepository.save(productType);
    }

    public ProductType updateProductType(String productTypeId, ProductTypeDTO productTypeDTO) {
        ProductType requestedProductType = productTypeRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }
        if (!productTypeDTO.validate()) {
            throw new MagmaException(MagmaStatus.MISSING_REQUIRED_PARAMS);
        }


        ProductType existingProductType = productTypeRepository.findByProductName(productTypeDTO.getProductName());
        if (existingProductType != null && !existingProductType.getId().equals(productTypeId)) {
            throw new MagmaException(MagmaStatus.PRODUCT_NAME_ALREADY_EXIST);
        }
        if (productTypeDTO.getProductName() != null) {

            List<Device> deviceswithProductType=deviceRepository.findByProductType(requestedProductType.getProductName());
            deviceswithProductType.forEach(device->{
                device.setProductType(productTypeDTO.getProductName());
                ProductData product =device.getProduct();
                product.setProductType(productTypeDTO.getProductName());
            });
            deviceRepository.save(deviceswithProductType);
            requestedProductType.setProductName(productTypeDTO.getProductName());
        }
        if(productTypeDTO.isPersistence()!=null){
            requestedProductType.setPersistence(productTypeDTO.isPersistence());
        }
        if (productTypeDTO.getSensorCodes() != null) {
            requestedProductType.setSensorCodes(productTypeDTO.getSensorCodes());
        }
        if (productTypeDTO.getActuatorCodes() != null) {
            requestedProductType.setActuatorCodes(productTypeDTO.getActuatorCodes());
        }
        if (productTypeDTO.getProtocol() != null) {
            requestedProductType.setProtocol(productTypeDTO.getProtocol());
        }
        if (productTypeDTO.getConnectivity() != null) {
            requestedProductType.setConnectivity(productTypeDTO.getConnectivity());
        }

        if (!requestedProductType.isRemotelyConfigurable()&&productTypeDTO.isRemotelyConfigurable()&&productTypeDTO.getVersions() != null && !productTypeDTO.getVersions().isEmpty()) {
            // add deviceParameter configuration of associated devices
            ProductVersion version = (ProductVersion) productTypeDTO.getVersions().get(0);
            this.deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(version.getRemoteConfigurations());
            requestedProductType.setVersions(productTypeDTO.getVersions());
            List<Device> deviceswithProductType = deviceRepository.findByProductProductId(productTypeId);
            deviceswithProductType.forEach(device->{
                DeviceParameterConfiguration deviceParameterConfiguration=device.getDeviceParameterConfiguration();
                if(deviceParameterConfiguration!=null){
                    deviceParameterConfiguration.setRemoteConfigurations(version.getRemoteConfigurations());
                    deviceParameterConfiguration.setJoinParameters(version.getJoinParameters());
                    device.setDeviceParameterConfiguration(deviceParameterConfiguration);
                }
                deviceRepository.save(device);
            });
        }
        // edit existing params
        else if (requestedProductType.isRemotelyConfigurable() && productTypeDTO.isRemotelyConfigurable() && productTypeDTO.getVersions() != null && !productTypeDTO.getVersions().isEmpty()) {
            //update products initial version params
            ProductVersion versionDetailstoUpdate = (ProductVersion) productTypeDTO.getVersions().get(0);
            List<RemoteConfigField> remoteParametertoUpdate = (List<RemoteConfigField>) versionDetailstoUpdate.getRemoteConfigurations();
            this.deviceParameterConfigurationUtil.validateParametersInRemoteConfiguration(remoteParametertoUpdate);
            ProductVersion defaultVersion = requestedProductType.getVersions().get(0);
            defaultVersion.setRemoteConfigurations(remoteParametertoUpdate);
            List<Device> devices = deviceRepository.findByProductType(requestedProductType.getProductName());
            //update parameters for devices which are not upgraded(@version 0.0.0)
            devices.forEach(device -> {
                DeviceParameterConfiguration deviceParameterConfiguration = device.getDeviceParameterConfiguration();
                if (deviceParameterConfiguration != null && Objects.equals(deviceParameterConfiguration.getVersionNum(), "0.0.0")) {
                    deviceParameterConfiguration.setRemoteConfigurations(remoteParametertoUpdate);
                    deviceParameterConfiguration.setJoinParameters(versionDetailstoUpdate.getJoinParameters());
                    device.setDeviceParameterConfiguration(deviceParameterConfiguration);
                }
                deviceRepository.save(device);
            });
        }

        requestedProductType.setRemotelyConfigurable(productTypeDTO.isRemotelyConfigurable());
        requestedProductType.setOtaUpgradable(productTypeDTO.isOtaUpgradable());
        if (productTypeDTO.getDataFormat() != null) {
            requestedProductType.setDataFormat(productTypeDTO.getDataFormat());
        }

        //update transcoder
        if (productTypeDTO.isTranscoder() && (!requestedProductType.isTranscoder() ||
                (!requestedProductType.getCodecName().equals( productTypeDTO.getCodecName())))) {
            MagmaCodec magmaCodec = magmaCodecRepository.findByCodecName(productTypeDTO.getCodecName());
            if (magmaCodec == null) {
                throw new MagmaException(MagmaStatus.INVALID_INPUT);
            }
            requestedProductType.setCodecName(productTypeDTO.getCodecName());
            List<Device> deviceswithProductType = deviceRepository.findByProductType(requestedProductType.getProductName());
            String codecId = magmaCodec.getId();
            deviceswithProductType.forEach(device -> {
                device.setMagmaCodecId(codecId);
                device.setCodec(magmaCodec);
                deviceRepository.save(device);
            });
        }
        else if (requestedProductType.isTranscoder() && !productTypeDTO.isTranscoder()){
            requestedProductType.setCodecName(null);
            List<Device> deviceswithProductType = deviceRepository.findByProductType(requestedProductType.getProductName());
            deviceswithProductType.forEach(device -> {
                device.setMagmaCodecId(null);
                device.setCodec(null);
                deviceRepository.save(device);
            });
        }
        requestedProductType.setTranscoder(productTypeDTO.isTranscoder());

        return productTypeRepository.save(requestedProductType);
    }

    public ProductType getOneProductTypeById(String productTypeId) {
        ProductType requestedProductType = productTypeRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }
        return requestedProductType;
    }

    public String deleteOneProductType(String productTypeId) {
        ProductType requestedProductType = productTypeRepository.findOne(productTypeId);
        if (requestedProductType == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_TYPE_NOT_FOUND);
        }

        productTypeRepository.delete(productTypeId);
        return "Success";
    }

    public SetupSenzmatica getSetupSenzmaticaStatus() {
        logger.debug("Setup Senzmatica");

        SetupSenzmatica setupSenzmatica = new SetupSenzmatica();

        List<ProductType> productTypes = productTypeRepository.findAll();
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




