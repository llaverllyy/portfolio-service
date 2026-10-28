package com.example.portfolio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Skill implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String name;

    @ManyToMany(mappedBy = "skills")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToMany(mappedBy = "skills")
    @Builder.Default
    private Set<Project> projects = new HashSet<>();
}