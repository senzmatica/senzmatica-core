package com.magma.core.data.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.MagmaDateTimeSerializer;

public class DeviceParameterUpdateStatus {


    private String parameterId;
    private String parameter;
    private String defaultValue;
    private Boolean isinValid;
    private String parameterCategory;


    public String getParameterId(){
        return
                this.parameterId;
    }
    public void setParameterId(String parameterid){

        this.parameterId=parameterid;
    }
    public String getParameter(){

        return this.parameter;
    }
    public void setParameter(String parameter){

        this.parameter=parameter;
    }
    public String getDefaultValue(){

        return this.defaultValue;
    }
    public void setDefaultValue(String defaultValue){

        this.defaultValue=defaultValue;
    }
    public Boolean getIsinValid() {

        return this.isinValid;
    }
    public void setIsinValid(boolean isinValid) {

        this.isinValid=isinValid;
    }
    public String getParameterCategory(){

        return this.parameterCategory;
    }
    public void setParameterCategory(String parameterCategory){

        this.parameterCategory=parameterCategory;
    }
}
