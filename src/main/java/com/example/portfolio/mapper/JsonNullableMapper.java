package com.example.portfolio.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface JsonNullableMapper {

    @Named("unwrap")
    default <T> T unwrap(JsonNullable<T> value) {
        return value == null ? null : value.orElse(null);
    }

    default <T> JsonNullable<T> wrap(T value) {
        return JsonNullable.of(value);
    }

    default boolean isPresent(JsonNullable<?> value) {
        return value != null && value.isPresent();
    }
}