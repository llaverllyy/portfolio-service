package com.example.portfolio.service.impl;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.dto.ProjectUpdateDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.mapper.ProjectMapper;
import com.example.portfolio.model.Project;
import com.example.portfolio.repository.ProjectRepository;
import com.example.portfolio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public List<ProjectDTO> getAll() {
        return projectRepository.findAll().stream()
                .map(projectMapper::map)
                .toList();
    }

    @Override
    public ProjectDTO getById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return projectMapper.map(project);
    }

    @Override
    @Transactional
    public ProjectDTO create(ProjectCreateDTO dto) {
        validateUniqueSlug(dto.getSlug(), null);
        Project project = projectMapper.map(dto);
        validateOwner(project);
        projectRepository.save(project);
        return projectMapper.map(project);
    }

    @Override
    @Transactional
    public ProjectDTO update(Long id, ProjectUpdateDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        projectMapper.update(dto, project);
        validateOwner(project);
        validateUniqueSlug(project.getSlug(), project.getId());
        projectRepository.save(project);
        return projectMapper.map(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        project.getSkills().clear();
        projectRepository.delete(project);
    }

    private void validateOwner(Project project) {
        if (project.getOwner() == null) {
            throw new IllegalArgumentException("Project ownerId cannot be null");
        }
    }

    private void validateUniqueSlug(String slug, Long currentProjectId) {
        projectRepository.findBySlug(slug)
                .filter(existingProject -> !existingProject.getId().equals(currentProjectId))
                .ifPresent(existingProject -> {
                    throw new ConflictException("Project already exists with slug: " + slug);
                });
    }
}