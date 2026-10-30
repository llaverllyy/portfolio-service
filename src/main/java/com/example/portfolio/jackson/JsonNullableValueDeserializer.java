package com.example.portfolio.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.IOException;

public class JsonNullableValueDeserializer extends JsonDeserializer<JsonNullable<?>> implements ContextualDeserializer {

    private final JavaType valueType;

    public JsonNullableValueDeserializer() {
        this(null);
    }

    private JsonNullableValueDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonNullable<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return JsonNullable.of(null);
        }

        JavaType targetType = valueType == null
                ? context.constructType(Object.class)
                : valueType;

        Object value = context.readValue(parser, targetType);
        return JsonNullable.of(value);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : context.getContextualType();
        JavaType nestedType = wrapperType == null
                ? context.constructType(Object.class)
                : wrapperType.containedTypeOrUnknown(0);

        return new JsonNullableValueDeserializer(nestedType);
    }

    @Override
    public JsonNullable<?> getNullValue(DeserializationContext context) {
        return JsonNullable.of(null);
    }
}