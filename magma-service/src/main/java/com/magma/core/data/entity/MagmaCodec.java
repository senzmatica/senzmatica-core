package com.magma.core.data.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "magmaCodec")
public class MagmaCodec {

    @Id
    private String id;

    private String codecName;

    private String decoderFileName;

    private String decoderFileContent;

    private String decoderFileExtension;

    private Boolean decoderStatus;

    private String encoderFileName;

    private String encoderFileContent;

    private String encoderFileExtension;

    private boolean encoderStatus;

    private String scriptFormat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getDecoderFileName() {
        return decoderFileName;
    }

    public void setDecoderFileName(String decoderFileName) {
        this.decoderFileName = decoderFileName;
    }

    public String getDecoderFileContent() {
        return decoderFileContent;
    }

    public void setDecoderFileContent(String decoderFileContent) {
        this.decoderFileContent = decoderFileContent;
    }

    public String getDecoderFileExtension() {
        return decoderFileExtension;
    }

    public void setDecoderFileExtension(String decoderFileExtension) {
        this.decoderFileExtension = decoderFileExtension;
    }

    public Boolean getDecoderStatus() {
        return decoderStatus;
    }

    public void setDecoderStatus(Boolean decoderStatus) {
        this.decoderStatus = decoderStatus;
    }

    public String getEncoderFileName() {
        return encoderFileName;
    }

    public void setEncoderFileName(String encoderFileName) {
        this.encoderFileName = encoderFileName;
    }

    public String getEncoderFileContent() {
        return encoderFileContent;
    }

    public void setEncoderFileContent(String encoderFileContent) {
        this.encoderFileContent = encoderFileContent;
    }

    public String getEncoderFileExtension() {
        return encoderFileExtension;
    }

    public void setEncoderFileExtension(String encoderFileExtension) {
        this.encoderFileExtension = encoderFileExtension;
    }

    public boolean isEncoderStatus() {
        return encoderStatus;
    }

    public void setEncoderStatus(boolean encoderStatus) {
        this.encoderStatus = encoderStatus;
    }

    public String getScriptFormat() {
        return scriptFormat;
    }

    public void setScriptFormat(String scriptFormat) {
        this.scriptFormat = scriptFormat;
    }

    @Override
    public String toString() {
        return "MagmaCodec{" +
                "id='" + id + '\'' +
                ", codecName='" + codecName + '\'' +
                ", decoderFileName='" + decoderFileName + '\'' +
                ", decoderFileContent='" + decoderFileContent + '\'' +
                ", decoderFileExtension='" + decoderFileExtension + '\'' +
                ", decoderStatus='" + decoderStatus + '\'' +
                ", encoderFileName='" + encoderFileName + '\'' +
                ", encoderFileContent='" + encoderFileContent + '\'' +
                ", encoderFileExtension='" + encoderFileExtension + '\'' +
                ", encoderStatus=" + encoderStatus +
                ", scriptFormat='" + scriptFormat + '\'' +
                '}';
    }
}
