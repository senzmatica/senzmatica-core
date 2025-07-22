package com.magma.core.util;

import com.magma.core.data.support.RemoteConfigField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;


@Service
public class DeviceParameterConfigurationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceParameterConfigurationUtil.class);

    public void validateParametersInRemoteConfiguration( List< RemoteConfigField> remoteConfigurations) {
        List<String> validFormats = Arrays.asList("string", "integer", "double", "float", "char");
        List<String> validInputTypes = Arrays.asList("text", "json", "html");
        List<String> validParameterCategory = Arrays.asList("network & communication", "topic format & interval", "message format");

        if (remoteConfigurations == null) {
            throw new MagmaException(MagmaStatus.PRODUCT_CONFIGURATIONS_INVALID);
        }

        for (RemoteConfigField parameter : remoteConfigurations) {
            if (parameter.getParameter() == null || parameter.getInputType() == null ||
                    parameter.getFormat() == null || parameter.getDefaultValue() == null ||
                    parameter.getParameter().isEmpty() || parameter.getDefaultValue().isEmpty() ||
                    parameter.getInputType().isEmpty() || parameter.getFormat().isEmpty() ||
                    parameter.getParameterCategory().isEmpty() || !isIntegerCheck(parameter.getParameterId())||
                    !validFormats.contains(parameter.getFormat().toLowerCase()) ||
                    !validInputTypes.contains(parameter.getInputType().toLowerCase()) || !validParameterCategory.contains(parameter.getParameterCategory().toLowerCase())) {

                throw new MagmaException(MagmaStatus.PRODUCT_CONFIGURATIONS_INVALID);
            }
            //Validate Parameter's Input Format and Default value
            validateParameterDefaultValueAndType(parameter.getFormat().toLowerCase(), parameter.getDefaultValue().toLowerCase());

        }
    }

    private static void validateParameterDefaultValueAndType(String format, String defaultValue) {
        LOGGER.debug("Validation format: {}, defaultValue: {}", format, defaultValue);
        boolean valid = true;
        switch (format) {
            case "string":
                if (isNumericCheck(defaultValue)) {
                    valid = false;
                }
                break;
            case "char":
                // any text with length 1 is accepted as char including integers, alphabets and other characters
                if(defaultValue.length()!=1){
                    valid=false;
                }
                break;
            case "double":
            case "float":
                if (!isNumericCheck(defaultValue)) {
                    valid = false;
                }
                if (isIntegerCheck(defaultValue)) {
                    valid = false;
                }
                break;
            case "integer":
                if (!isIntegerCheck(defaultValue)) {
                    valid = false;
                }
                break;
            default:
                break;
        }

        if (!valid) {
            throw new MagmaException(MagmaStatus.PRODUCT_CONFIGURATIONS_INVALID);
        }
    }

    //Check a String is Integer or Not
    public static boolean isIntegerCheck(String str) {
        try {
            int parsedValue = Integer.parseInt(str);
            LOGGER.debug("isIntegerCheck for string {}: {}", str, parsedValue);
            return true;
        } catch (NumberFormatException e) {
            LOGGER.debug("isIntegerCheck Error for string {}", str, e);
            return false;
        }
    }

    //Check a String is Number or Not
    public static boolean isNumericCheck(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String extractIpAddress(String serverAddress) {
        try {
            InetAddress address = InetAddress.getByName(serverAddress);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.debug("An UnknownHostException occurred: {}", e.getMessage());
        }
        return null;
    }

}
