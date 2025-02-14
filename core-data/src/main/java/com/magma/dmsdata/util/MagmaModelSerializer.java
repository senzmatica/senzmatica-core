package com.magma.dmsdata.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.magma.dmsdata.data.entity.KitModel;

import java.io.IOException;

public class MagmaModelSerializer extends JsonSerializer<KitModel> {

    @Override
    public void serialize(KitModel value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", value.getId());
        gen.writeStringField("name", value.getName());
        gen.writeNumberField("noOfProperties", value.getNoOfProperties());
        gen.writeObjectField("properties", value.getProperties());

        if (value.getGpsEnabled() != null) {
            gen.writeBooleanField("gpsEnabled", value.getGpsEnabled());
        }
        if (value.getLbsEnabled() != null) {
            gen.writeBooleanField("lbsEnabled", value.getLbsEnabled());
        }
        if (value.getBatteryEnabled() != null) {
            gen.writeBooleanField("batteryEnabled", value.getBatteryEnabled());
        }
        gen.writeStringField("type", value.getType());
        gen.writeEndObject();
    }
}
