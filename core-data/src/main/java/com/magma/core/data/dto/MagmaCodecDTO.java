package com.magma.core.data.dto;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class MagmaCodecDTO {
    @NotNull(message = "Decoder File cannot be null")
    MultipartFile decoderFile;

    @NotNull(message = "Encoder File cannot be null")
    MultipartFile encoderFile;

    @NotNull(message = "Codec Name cannot be null")
    @NotEmpty(message = "Codec Name cannot be empty")
    String codecName;

    @NotNull(message = "Script format cannot be null")
    @NotEmpty(message = "Script format cannot be empty")
    String scriptFormat;

    public MultipartFile getEncoderFile() {
        return encoderFile;
    }

    public void setEncoderFile(MultipartFile encoderFile) {
        this.encoderFile = encoderFile;
    }

    public MultipartFile getDecoderFile() {
        return decoderFile;
    }

    public void setDecoderFile(MultipartFile decoderFile) {
        this.decoderFile = decoderFile;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getScriptFormat() {
        return scriptFormat;
    }

    public void setScriptFormat(String scriptFormat) {
        this.scriptFormat = scriptFormat;
    }

    @Override
    public String toString() {
        return "MagmaCodecDTO{" +
                "decoderFile=" + decoderFile +
                ", encoderFile=" + encoderFile +
                ", codecName='" + codecName + '\'' +
                ", scriptFormat='" + scriptFormat + '\'' +
                '}';
    }
}
