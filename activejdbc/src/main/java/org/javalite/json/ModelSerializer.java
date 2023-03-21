package org.javalite.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.javalite.activejdbc.Model;

import java.io.IOException;

public class ModelSerializer extends JsonSerializer<Model> {

    @Override
    public void serialize(Model value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (var entry : value.toMap().entrySet()) {
            if (entry.getValue() != null) {
                gen.writeFieldName(entry.getKey());
                gen.writeObject(entry.getValue());
            }
        }
        gen.writeEndObject();
    }

}
