package com.example.portfolio.jackson;

import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

public class JsonNullableValueDeserializer extends ValueDeserializer<JsonNullable<?>> {

    private final JavaType valueType;

    public JsonNullableValueDeserializer() {
        this(null);
    }

    private JsonNullableValueDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonNullable<?> deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
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
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : context.getContextualType();
        JavaType nestedType = wrapperType == null
                ? context.constructType(Object.class)
                : wrapperType.containedTypeOrUnknown(0);

        return new JsonNullableValueDeserializer(nestedType);
    }

    @Override
    public Object getNullValue(DeserializationContext context) {
        return JsonNullable.of(null);
    }
}