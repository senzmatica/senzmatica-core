package com.magma.core.service;

import com.magma.dmsdata.data.dto.MagmaCodecDTO;
import com.magma.dmsdata.data.entity.Device;
import com.magma.dmsdata.data.entity.MagmaCodec;
import com.magma.core.data.repository.DeviceRepository;
import com.magma.core.data.repository.MagmaCodecRepository;
import com.magma.dmsdata.util.MagmaException;
import com.magma.dmsdata.util.MagmaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MagmaCodecService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagmaCodecService.class);
    @Autowired
    MagmaCodecRepository magmaCodecRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    private CompileCodeFileService compileCodeFileService;

    public MagmaCodec createMagmaCodec(MagmaCodecDTO magmaCodecDTO) {

        MagmaCodec existingMagmaCodec = magmaCodecRepository.findByCodecName(magmaCodecDTO.getCodecName());
        if (existingMagmaCodec != null) {
            throw new MagmaException(MagmaStatus.CODEC_NAME_ALREADY_EXIST);
        }

        MagmaCodec newCodec = new MagmaCodec();

        try {
            if (!magmaCodecDTO.getDecoderFile().isEmpty() || !magmaCodecDTO.getEncoderFile().isEmpty()) {

                newCodec.setCodecName(magmaCodecDTO.getCodecName());
                newCodec.setScriptFormat(magmaCodecDTO.getScriptFormat());

                Boolean isDecoderFileValid = false;
                Boolean isEncoderFileValid = false;

                if (!magmaCodecDTO.getDecoderFile().isEmpty()) {
                    String fileNameDecoder = StringUtils.cleanPath(magmaCodecDTO.getDecoderFile().getOriginalFilename());
                    newCodec.setDecoderFileName(fileNameDecoder);
                    byte[] fileByteDecoder = magmaCodecDTO.getDecoderFile().getBytes();
                    String fileContentDecoder = new String(fileByteDecoder, StandardCharsets.UTF_8);
                    newCodec.setDecoderFileContent(fileContentDecoder);

                    if (fileContentDecoder.contains("execute")) {
                        newCodec.setDecoderStatus(true);
                    } else {
                        newCodec.setDecoderStatus(false);
                    }

                    validateCodecContent(fileNameDecoder, fileContentDecoder, "Decoder");

                    if (compileCodeFileService.compileCodeFile(fileNameDecoder, fileContentDecoder)) {
                        LOGGER.debug("Decoder passed!!!");
                        isDecoderFileValid = true;
                    }
                }

                if (!magmaCodecDTO.getEncoderFile().isEmpty()) {
                    String fileNameEncoder = StringUtils.cleanPath(magmaCodecDTO.getEncoderFile().getOriginalFilename());
                    newCodec.setEncoderFileName(fileNameEncoder);
                    byte[] fileByteEncoder = magmaCodecDTO.getEncoderFile().getBytes();
                    String fileContentEncoder = new String(fileByteEncoder, StandardCharsets.UTF_8);
                    newCodec.setEncoderFileContent(fileContentEncoder);

                    validateCodecContent(fileNameEncoder, fileContentEncoder, "Encoder");

                    if (compileCodeFileService.compileCodeFile(fileNameEncoder, fileContentEncoder)) {
                        LOGGER.debug("Encoder passed!!!");
                        isEncoderFileValid = true;
                    }
                }

                if (!magmaCodecDTO.getDecoderFile().isEmpty() && !magmaCodecDTO.getEncoderFile().isEmpty() && !isDecoderFileValid && !isEncoderFileValid) {
                    throw new MagmaException(MagmaStatus.BOTH_CODEC_NOT_COPILABLE);
                } else if (!magmaCodecDTO.getDecoderFile().isEmpty() && !isDecoderFileValid) {
                    throw new MagmaException(MagmaStatus.DECODER_NOT_COPILABLE);
                } else if (!magmaCodecDTO.getEncoderFile().isEmpty() && !isEncoderFileValid) {
                    throw new MagmaException(MagmaStatus.ENCODER_NOT_COPILABLE);
                } else {
                    LOGGER.debug("Both files are valid");
                    MagmaCodec saveCodec = magmaCodecRepository.save(newCodec);
                    return saveCodec;
                }
            } else {
                throw new MagmaException(MagmaStatus.Encoder_Decoder_file_Not_Exist);
            }

        } catch (IOException e) {
            LOGGER.debug(e.toString());
            throw new MagmaException(MagmaStatus.CODEC_COMPILATION_FAILED);
        }
    }

    public String deleteCodec(String codecId) {
        MagmaCodec magmaCodec = magmaCodecRepository.findById(codecId).orElse(null);

        LOGGER.debug("MagmaCodec for codec id : {}, is {}", codecId, magmaCodec);

        if (magmaCodec != null) {
            List<Device> devices = deviceRepository.findByMagmaCodecId(codecId);

            for (Device device : devices) {

                device.setMagmaCodecId(null);

                deviceRepository.save(device);
            }

            magmaCodecRepository.delete(magmaCodec);

            return "Codec Deleted Successfully.";
        } else {
            return "Codec not found.";
        }
    }


    public List<MagmaCodec> findAllCodec() {
        return magmaCodecRepository.findAll();
    }

    public MagmaCodec updateMagmaCodec(String codecId, MultipartFile decoderFile, MultipartFile encoderFile, String codecName, String scriptFormat) {
        try {
            if (!decoderFile.isEmpty() || !encoderFile.isEmpty()) {

                Boolean isDecoderFileValid = false;
                Boolean isEncoderFileValid = false;

                MagmaCodec magmaCodec = magmaCodecRepository.findById(codecId).orElse(null);

                if (magmaCodec == null) {
                    throw new MagmaException(MagmaStatus.INVALID_INPUT);
                }

                magmaCodec.setScriptFormat(scriptFormat);

                if (codecName != magmaCodec.getCodecName() && codecName != null) {
                    magmaCodec.setCodecName(codecName);
                }

                if (!decoderFile.isEmpty()) {
                    byte[] updateFileByteDecoder = decoderFile.getBytes();
                    String fileContentDecoder = new String(updateFileByteDecoder, StandardCharsets.UTF_8);

                    magmaCodec.setDecoderFileName(decoderFile.getOriginalFilename());
                    magmaCodec.setDecoderFileContent(fileContentDecoder);

                    if (fileContentDecoder.contains("execute")) {
                        magmaCodec.setDecoderStatus(true);
                    } else {
                        magmaCodec.setDecoderStatus(false);
                    }

                    if (compileCodeFileService.compileCodeFile(decoderFile.getOriginalFilename(), fileContentDecoder)) {
                        LOGGER.debug("Decoder passed!!!");
                        isDecoderFileValid = true;
                    }
                } else {
                    magmaCodec.setDecoderFileName(null);
                    magmaCodec.setDecoderFileContent(null);
                }

                if (!encoderFile.isEmpty()) {
                    byte[] updateFileByteEncoder = encoderFile.getBytes();
                    String fileContentEncoder = new String(updateFileByteEncoder, StandardCharsets.UTF_8);

                    magmaCodec.setEncoderFileName(encoderFile.getOriginalFilename());
                    magmaCodec.setEncoderFileContent(fileContentEncoder);

                    if (compileCodeFileService.compileCodeFile(encoderFile.getOriginalFilename(), fileContentEncoder)) {
                        LOGGER.debug("Encoder passed!!!");
                        isEncoderFileValid = true;
                    }

                } else {
                    magmaCodec.setEncoderFileName(null);
                    magmaCodec.setEncoderFileContent(null);
                }

                if (!decoderFile.isEmpty() && !encoderFile.isEmpty() && !isDecoderFileValid && !isEncoderFileValid) {
                    throw new MagmaException(MagmaStatus.BOTH_CODEC_NOT_COPILABLE);
                } else if (!decoderFile.isEmpty() && !isDecoderFileValid) {
                    throw new MagmaException(MagmaStatus.DECODER_NOT_COPILABLE);
                } else if (!encoderFile.isEmpty() && !isEncoderFileValid) {
                    throw new MagmaException(MagmaStatus.ENCODER_NOT_COPILABLE);
                } else {
                    LOGGER.debug("Both files are valid");
                    MagmaCodec saveCodec = magmaCodecRepository.save(magmaCodec);
                    return saveCodec;
                }
            } else {
                throw new IllegalArgumentException("Upload both encoder and decoder files.");
            }

        } catch (IOException e) {
            LOGGER.debug(e.toString());
            throw new MagmaException(MagmaStatus.CODEC_COMPILATION_FAILED);
        }
    }

    public List<Device> connectDeviceWithCodec(String codecId, List<String> deviceIds) {
        MagmaCodec magmaCodec = magmaCodecRepository.findById(codecId).orElse(null);
        if (magmaCodec == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        List<Device> updatedDevices = new ArrayList<>();
        deviceIds.forEach(deviceId -> {
            Device newDevice = deviceRepository.findById(deviceId).orElse(null);
            if (newDevice != null && newDevice.getMagmaCodecId() != null) {
                LOGGER.debug(newDevice.getName() + " already connected with " + newDevice.getCodec().getCodecName());
            } else {
                newDevice.setMagmaCodecId(codecId);
                deviceRepository.save(newDevice);
                updatedDevices.add(newDevice);
            }
        });

        return updatedDevices;
    }

    public MagmaCodec getCodec(String codecId) {
        MagmaCodec magmaCodec = magmaCodecRepository.findById(codecId).orElse(null);
        if (magmaCodec == null) {
            throw new MagmaException(MagmaStatus.CODEC_NOT_FOUND);
        }
        return magmaCodec;
    }

    public List<Device> viewConnectedDevice(String codecId) {
        List<Device> connectedDevice = deviceRepository.findByMagmaCodecId(codecId);
        return connectedDevice;
    }

    public Device updateCodec(String deviceId, String updateCodecId) {
        Device connectedDevice = deviceRepository.findById(deviceId).orElse(null);
        connectedDevice.setMagmaCodecId(updateCodecId);
        deviceRepository.save(connectedDevice);
        return connectedDevice;
    }

    public Device disconnectDeviceAndCodec(String deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        device.setMagmaCodecId(null);
        deviceRepository.save(device);
        return device;
    }

    public List<Device> bulkDisconnectDeviceAndCodec(List<String> deviceIds) {
        List<Device> updatedDevices = new ArrayList<>();

        for (String deviceId : deviceIds) {
            Device device = deviceRepository.findById(deviceId).orElse(null);

            if (device != null) {
                device.setMagmaCodecId(null);
                deviceRepository.save(device);
                updatedDevices.add(device);
            } else {
                throw new RuntimeException("Device with ID " + deviceId + " not found");
            }
        }

        return updatedDevices;
    }

    // validate files content
    private void validateCodecContent(String fileName, String fileContent, String codecType) {
        String className = getClassName(fileContent);
        if (!fileName.replace(".java", "").equals(className)) {
            throw new MagmaException(codecType.equals("Decoder") ? MagmaStatus.DECODER_INVALID_CLASS_NAME : MagmaStatus.ENCODER_INVALID_CLASS_NAME);
        }

        // Validate import statements for external libraries
        if (containsExternalLibraries(fileContent)) {
            throw new MagmaException(codecType.equals("Decoder") ? MagmaStatus.DECODER_INVALID_IMPORTS : MagmaStatus.ENCODER_INVALID_IMPORTS);
        }

        // Validate method name and parameters
        if (!isValidMethodName(fileContent)) {
            throw new MagmaException(codecType.equals("Decoder") ? MagmaStatus.DECODER_INVALID_METHOD : MagmaStatus.ENCODER_INVALID_METHOD);
        }

        if (!isValidMethodParameters(fileContent)) {
            throw new MagmaException(codecType.equals("Decoder") ? MagmaStatus.DECODER_INVALID_ARGUMENT : MagmaStatus.ENCODER_INVALID_ARGUMENT);
        }
    }

    // Helper method to extract class name from Java code
    private String getClassName(String fileContent) {
        String classRegex = "class\\s+(\\w+)";
        Pattern pattern = Pattern.compile(classRegex);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    // Helper method to check for external libraries in import statements
    private boolean containsExternalLibraries(String fileContent) {
        String importRegex = "import\\s+((?!java\\.).)+;";
        Pattern pattern = Pattern.compile(importRegex);
        Matcher matcher = pattern.matcher(fileContent);
        return matcher.find();
    }

    // Helper method to validate method and parameters
    private boolean isValidMethodName(String fileContent) {
        String methodRegex = "public\\s+String\\s+convert\\(.*\\)";
        Pattern pattern = Pattern.compile(methodRegex);
        Matcher matcher = pattern.matcher(fileContent);
        return matcher.find();
    }

    private boolean isValidMethodParameters(String fileContent) {
        String paramRegex = "public\\s+String\\s+convert\\(\\s*Object\\s+\\w*\\s*\\)";
        Pattern pattern = Pattern.compile(paramRegex);
        Matcher matcher = pattern.matcher(fileContent);
        return matcher.find();
    }
}
