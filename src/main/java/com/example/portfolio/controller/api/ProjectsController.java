package com.example.portfolio.controller.api;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.dto.ProjectUpdateDTO;
import com.example.portfolio.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectsController {

    private final ProjectService projectService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDTO> index() {
        return projectService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectDTO show(@PathVariable Long id) {
        return projectService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDTO create(@Valid @RequestBody ProjectCreateDTO dto) {
        return projectService.create(dto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectDTO update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateDTO dto) {
        return projectService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }
}