package com.magma.core.util;

public enum MagmaStatus {

    SUCCESS("S1000", "Success"),

    ERROR("E1000", "Unknown error occurred in operation"),
    MISSING_REQUIRED_PARAMS("E1001", "One or more required parameters are missing"),
    DB_ERROR("E1002", "Unknown error occurred in database operation"),
    NO_ENTRY_FOUND("E1003", "Empty results from database"),
    NOT_FOUND("E1007", "Not found"),
    NOT_IMPLEMENTED("E1008", "Not Implemented"),
    NO_UPDATE_FOUND("E1010", "No Update Found"),
    PENDING_OPERATION("E1011", "Operations are Pending"),

    PAGINATION_ERROR("E2000", "Wrong Pagination Parameters"),
    INVALID_PHONE_NUMBER("E2001", "Not a Valid SL Mobile Number"),
    ALREADY_EXISTS("E2002", "Data Already Exists"),

    DUPLICATE_INPUT("E0403", "Given Data has Duplicates"),
    INVALID_INPUT("E0405", "Given Data is Invalid"),
    INVALID_ALERT_LEVEL("E405", "Alert Level Invalid or Exceeds Kit Alert Level"),

    DEVICE_NOT_FOUND("E1011", "Device Not Found"),
    CORPORATE_NOT_FOUND("EP4015", "Corporate Not found"),
    KIT_MODEL_NOT_FOUND("E4010", "Kit Model Not found"),
    KIT_NOT_FOUND("E4011", "Kit Not found"),
    SENSOR_NOT_FOUND("E4012", "Sensor Not found"),
    PROPERTY_NOT_FOUND("E4013", "Property Not found"),

    DEVICE_ALREADY_EXISTS("E4040", "Device already exists"),
    DEVICE_ALREADY_ASSIGNED("E4041", "Device already Assigned to a Kit"),
    KIT_ALREADY_EXISTS("E4050", "Kit ID Already Exists"),
    KIT_MODEL_ALREADY_EXISTS("E4051", "Kit Model already exists"),
    GPS_NOT_ENABLE("E4077", "GPS of Kit Not Enable Yet"),
    BATTERY_NOT_ENABLE("E4078", "Battery of Kit Not Enable Yet"),
    CORPORATE_ALREADY_EXISTS("EP4065", "Corporate already exists"),
    KIT_MODEL_SENSOR_CODE_VALIDATE_ERROR("E4080", "Kit Model Sensor Code not exist"),
    NUMBER_OF_SENSORS_EXCEED("E4081", "Number of Sensors in device exceeds Number of Sensors in Kit"),
    NUMBER_OF_ACTUATORS_EXCEED("E4082", "Number of Actuators in device exceeds Number of Actuators in Kit"),
    NEED_A_PIVOT_FOR_SHIFTING("E4085", "Require a Value for Arithmetic Operation"),
    KIT_MODEL_ACTUATOR_CODE_VALIDATE_ERROR("E4083", "Kit Model Actuator Code not exist"),

    FAVOURITE_DEVICES_NOT_EXISTS("E4088", "User Don't Have Any Favourite devices"),
    DEVICE_NOT_EXISTS("E4089", "Device Doesn't Exist"),
    KIT_MODEL_NOT_EXISTS("E4091", "Kit Model Doesn't Exist"),
    NOT_MARKED_AS_FAVOURITE("E4087", "Mentioned Device Not in Favourite List"),
    ALREADY_MARKED_AS_FAVOURITE("E4086", "Mentioned Device Already Inserted in Favourite List"),
    ERROR_IN_IMAGE_PROCESSING("E4086", "The image processing model did not generate output"),
    BAD_IMAGE_REQUEST("E4090", "Bad Image,This image cannot be processed,so please try again with a different image."),
    TEST_CASE_NOT_FOUND("E491", "Testcase not exist"),
    FAVOURITE_PRODUCTS_NOT_EXISTS("E492", "User Don't Have Any Favourite products"),
    PRODUCT_NOT_FOUND("E493", "Product Not Found"),
    PRODUCT_VERSION_NOT_FOUND("E494", "Product Version does not exist"),
    PRODUCT_VERSION_ALREADY_EXISTS("E495", "Product Version Already exists"),
    PRODUCT_CONFIGURATIONS_INVALID("E496", "Invalid Device's Remote Configurations"),
    MQTT_EXCEPTION_IN_VERSION_UPGRADE("E497", "MQTT Exception in version upgrade!"),
    INVALID_PRODUCT_TYPE("E498", "Invalid Product Type"),
    INVALID_DEVICE_CATEGORY("E499", "Invalid Device Category"),
    MQTT_EXCEPTION_IN_TESTING("E500", "MQTT Exception in testing"),
    MQTT_EXCEPTION_IN_CONFIGURING_DEVICE("E501", "MQTT Exception in configuring devices"),
    FAVOURITE_DEVICES_NOT_FOUND("EP4078", "User didn't have favorites devices"),
    FILE_DOWNLOAD_EXCEPTION("E502", "Exception in file download"),
    DEVICE_PARAMETER_CONFIGURATION_NOT_FOUND("E503", "Device Parameter Configuration not found"),
    PRODUCT_UPDATE_FAILED("E504", "Product Update Failed"),
    CODEC_COMPILATION_FAILED("E547", "Codec compilation failed"),
    BOTH_CODEC_NOT_COPILABLE("E548", "Both encoder and decoder files are not compilable"),
    ENCODER_NOT_COPILABLE("E549", "Encoder file is not compilable"),
    DECODER_NOT_COPILABLE("E550", "Decoder file is not compilable"),
    DUPLICATE_CLIENT_ID("E551", "Client ID already exists"),
    INVALID_GRANT_TYPE("E552", "Invalid Grant Type"),
    INVALID_SCOPE("E553", "Invalid Scopes"),

    DECODER_INVALID_CLASS_NAME("E560", "Error with decoder file, class name and file name should be the same"),
    DECODER_INVALID_IMPORTS("E562", "Error with decoder file, only java.util libraries are allowed"),
    DECODER_INVALID_METHOD("E563", "Error with decoder file, method named 'convert' not found"),
    DECODER_INVALID_ARGUMENT("E564", "Error with decoder file, argument must be an object"),

    ENCODER_INVALID_CLASS_NAME("E565", "Error with encoder file, class name and file name should be the same"),
    ENCODER_INVALID_IMPORTS("E567", "Error with encoder file, only java.util libraries are allowed"),
    ENCODER_INVALID_METHOD("E568", "Error with encoder file, method named 'convert' not found"),
    ENCODER_INVALID_ARGUMENT("E569", "Error with encoder file, argument must be an object"),

    EXCEPTION_IN_PRODUCT_VERSION_UPGRADE("E570", "Exception while updating product version"),

    PRODUCT_NAME_REQUIRED("E571", "Product Name is required."),
    AT_LEAST_ONE_SENSOR_IS_REQUIRED("E572", "At least one sensor is required."),
    AT_LEAST_ONE_Actuator_IS_REQUIRED("E573", "At least one Actuator is required."),

    CONNECTIVITY_IS_REQUIRED("E574", "Connectivity is required."),

    PROTOCOL_IS_REQUIRED("E574", "Protocol is required."),

    CODEC_NOT_FOUND("E575", "Codec not found"),

    USER_ID_IS_REQUIRED("E576", "User Id is required."),

    PRODUCT_TYPE_IS_REQUIRED("E577", "Product type is required."),

    PRODUCT_TYPE_NOT_FOUND("E579", "Product type not found."),

    PRODUCT_TYPE_ALREADY_EXIST("E578", "Product type already exist."),

    DATA_PROTECTED("E404", "Data is protected. You can't edit or delete"),

    DUPLICATE_KIT_TYPE("E560", "Kit Type Already exists.");


    private final String statusCode;
    private final String statusDescription;

    MagmaStatus(String statusCode, String successDescription) {
        this.statusCode = statusCode;
        this.statusDescription = successDescription;
    }

    public static MagmaStatus getCCStatus(String statusCode) {
        for (MagmaStatus ccStatus : MagmaStatus.values()) {
            if (ccStatus.statusCode.equals(statusCode)) {
                return ccStatus;
            }
        }
        return SUCCESS;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean isSuccess() {
        return this.statusCode.equals(MagmaStatus.SUCCESS.statusCode);
    }
}
