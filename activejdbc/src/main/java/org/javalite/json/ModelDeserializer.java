package org.javalite.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.AbstractDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.javalite.activejdbc.Model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModelDeserializer extends JsonDeserializer<Model> implements ContextualDeserializer {

    private Class<?> targetClass;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        var c = ctxt.getContextualType().getRawClass();
        if (Model.class.isAssignableFrom(c)) {
            targetClass = c;
        }
        return this;
    }

    @Override
    public Model deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Model model;
        try {
            model = ((Model) targetClass.getDeclaredConstructor(null).newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return model.fromMap(ctxt.readValue(p, Map.class));
    }

}
