package com.example.portfolio.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private LocalDate createdAt;
    private Set<Long> skillIds;
}