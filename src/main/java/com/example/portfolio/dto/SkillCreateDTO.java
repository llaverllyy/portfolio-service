package com.example.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillCreateDTO {
    @NotBlank
    private String name;
}