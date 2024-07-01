package com.magma.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.magma.core.data.entity.Property;

import java.io.IOException;

public class MagmaPropertyReadingSerializer extends JsonSerializer<Property> {


    @Override
    public void serialize(Property value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getValue());
    }
}
