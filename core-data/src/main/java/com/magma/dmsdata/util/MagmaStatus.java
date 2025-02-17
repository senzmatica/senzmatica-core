package com.magma.dmsdata.util;

public enum MagmaStatus {

    SUCCESS("S1000", "Success"),
    BAD_REQUEST("400", "Bad request"),

    ERROR("E1000", "Unknown error occurred in operation"),
    MISSING_REQUIRED_PARAMS("E1001", "One or more required parameters are missing"),
    MISSING_REQUIRED_CONNECTION_STRING_COMPONENTS("E1004","Connection string is missing required components"),
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
    INVALID_BASE_URL("E406", "Invalid Base URL"),
    INVALID_ACCESS_TOKEN("E407", "Invalid Access Token"),

    DEVICE_NOT_FOUND("E1011", "Device Not Found"),
    CONNECT_DEVICE_NOT_FOUND("E1012", "Connect Device Not Found"),
    DEVICE_NOT_FOUND_FOR_REFERENCE("E1013", "Device Not Found for Reference"),
    CORPORATE_NOT_FOUND("EP4015", "Corporate Not found"),
    KIT_MODEL_NOT_FOUND("E4010", "Kit Model Not found"),
    KIT_NOT_FOUND("E4011", "Kit Not found"),
    SENSOR_NOT_FOUND("E4012", "Sensor Not found"),
    PROPERTY_NOT_FOUND("E4013", "Property Not found"),

    DEVICE_ALREADY_EXISTS("E4040", "This device name is already taken"),
    DEVICE_ALREADY_ASSIGNED("E4041", "Device already Assigned to a Kit"),
    DEVICE_ID_ALREADY_EXISTS("E4040", "This device id is already taken"),
    DEVICE_NAME_ALREADY_EXISTS("E4096","This device name is already taken"),
    KIT_ALREADY_EXISTS("E4050", "Kit ID Already Exists"),
    KIT_MODEL_ALREADY_EXISTS("E4051", "Kit Model already exists"),
    KIT_MODEL_NAME_ALREADY_EXISTS("E4052", "Kit Model Name Already Exists"),
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
    BATCH_NUMBER_NOT_EXISTS("E4092", "Batch Number Does Not Exist."),
    NOT_MARKED_AS_FAVOURITE("E4087", "Mentioned Device Not in Favourite List"),
    ALREADY_MARKED_AS_FAVOURITE("E4086", "Mentioned Device Already Inserted in Favourite List"),
    ERROR_IN_IMAGE_PROCESSING("E4086", "The image processing model did not generate output"),
    BAD_IMAGE_REQUEST("E4090", "Bad Image,This image cannot be processed,so please try again with a different image."),
    TEST_CASE_NOT_FOUND("E491", "Testcase not exist"),
    SUB_TEST_CASE_NOT_FOUND("E492", "Sub Testcase not exist"),
    ROOT_CAUSE_NOT_FOUND("E493", "Root cause not exist"),
    TEST_CASE_TITLE_ALREADY_EXIST("E491", "Main Test Case Name Already Exists"),
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
    TOPIC_FORMAT_INVALID("E4093","Device's topic format invalid"),
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
    CLIENT_NOT_FOUND("E554", "Client Doesn't Exit"),
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
    PRODUCT_NAME_ALREADY_EXIST("E580", "Product name already exist."),
    CODEC_NAME_ALREADY_EXIST("E582", "Codec name already exist"),
    BATCH_NUMBER_ALREADY_EXIST("E581", "Batch number already exist"),
    DB_CONNECTION_FAILED("E582", "Cosmos DB connection failed"),
    DATA_PROTECTED("E404", "Data is protected. You can't edit or delete"),
    DEVICE_ID_ALREADY_ADDED("E583", "This device name is already taken"),
    BIN_FILE_SAVE_ERROR("E584","Bin File Was not Properly Saved in VM"),
    BIN_FILE_NOT_UPLOADED("E4094","Bin file is missing. Please upload a valid .bin file."),
    INVALID_FILE_TYPE("E4095","Invalid File Type"),
    DEVICE_IDS_NULL_ERROR("E585", "Device IDs cannot be null"),

    DUPLICATE_KIT_TYPE("E560", "Kit Type Already exists."),

    Sensor_Actuator_Not_Exist("E431","Sensor and Actuator not exist"),

    CONTINUOUS_TEST_ALREADY_EXIST("E560", "Continuous test already exist."),

    ANALYSIS_NAME_ALREADY_EXIST("E561","Analysis name already exist"),

    ROOT_CAUSE_ALREADY_CONFIGURED_FOR_BATCH("E561","Root cause is already configured for this batch"),

    ANALYSIS_NAME_NOT_FOUND("E562","Analysis name not found"),
    Encoder_Decoder_file_Not_Exist("E432","Upload Both Encoder and Decoder Files."),
    LIVE_NOTIFICATION_NOT_FOUND("E1009", "Live Notification Not Found"),
    MESSAGE_GENERATION_FAILED("E563","JSON Message Generation Failed"),

    PLEASE_UPGRADE_YOUR_AZURE_PLAN("E563","Please upgrade your azure plan"),
    AZURE_PLAN_NOT_FOUND("E564","Azure plan not found"),
    REMOTE_CONFIGURATIONS_NOT_FOUND("E564","Remote Configuration Not Found"),
    ERROR_OBTAINING_ACCESS_TOKEN("E566","Error Obtaining Access Token"),
    FAILED_TO_EMIT_USAGE_EVENT("E586","Failed Emit Usage Event"),
    INVALID_PLAN_ID("E587","Invalid Plan Id.");



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
