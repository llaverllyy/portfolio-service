package com.example.portfolio.dto;

import com.example.portfolio.jackson.JsonNullableValueDeserializer;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
public class SkillUpdateDTO {
    @JsonDeserialize(using = JsonNullableValueDeserializer.class)
    private JsonNullable<String> name;
}