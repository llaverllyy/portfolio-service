package com.example.portfolio.service;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.dto.ProjectUpdateDTO;

import java.util.List;

public interface ProjectService {
    List<ProjectDTO> getAll();
    ProjectDTO getById(Long id);
    ProjectDTO create(ProjectCreateDTO dto);
    ProjectDTO update(Long id, ProjectUpdateDTO dto);
    void delete(Long id);
}