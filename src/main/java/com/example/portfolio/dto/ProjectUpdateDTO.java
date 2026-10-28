package com.example.portfolio.dto;

import com.example.portfolio.jackson.JsonNullableValueDeserializer;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
public class ProjectUpdateDTO {
    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<Long> ownerId;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> slug;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> name;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> description;
}