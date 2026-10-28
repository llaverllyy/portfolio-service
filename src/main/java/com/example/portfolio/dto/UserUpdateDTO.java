package com.example.portfolio.dto;

import com.example.portfolio.jackson.JsonNullableValueDeserializer;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
public class UserUpdateDTO {
    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> firstName;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> lastName;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> email;

    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> bio;
}