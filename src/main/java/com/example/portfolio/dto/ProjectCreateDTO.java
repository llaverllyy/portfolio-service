package com.example.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreateDTO {
    @NotNull
    private Long ownerId;
    @NotBlank
    private String slug;
    @NotBlank
    private String name;
    private String description;
}