package com.example.portfolio.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class ProjectDTO {
    private Long id;
    private Long ownerId;
    private String slug;
    private String name;
    private String description;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Set<Long> skillIds;
}