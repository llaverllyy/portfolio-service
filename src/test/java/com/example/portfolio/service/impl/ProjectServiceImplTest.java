package com.example.portfolio.service.impl;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.dto.ProjectUpdateDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.mapper.ProjectMapper;
import com.example.portfolio.model.Project;
import com.example.portfolio.model.Skill;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void getAllReturnsMappedProjects() {
        var firstProject = buildProject(1L, "portfolio-api");
        var secondProject = buildProject(2L, "portfolio-web");
        var firstDto = buildProjectDto(1L, "portfolio-api");
        var secondDto = buildProjectDto(2L, "portfolio-web");

        when(projectRepository.findAll()).thenReturn(List.of(firstProject, secondProject));
        when(projectMapper.map(firstProject)).thenReturn(firstDto);
        when(projectMapper.map(secondProject)).thenReturn(secondDto);

        var actual = projectService.getAll();

        assertThat(actual).containsExactly(firstDto, secondDto);
    }

    @Test
    void getByIdReturnsMappedProject() {
        var project = buildProject(1L, "portfolio-api");
        var dto = buildProjectDto(1L, "portfolio-api");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.map(project)).thenReturn(dto);

        var actual = projectService.getById(1L);

        assertThat(actual).isSameAs(dto);
    }

    @Test
    void getByIdThrowsWhenProjectIsMissing() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: 1");
    }

    @Test
    void createSavesProjectWhenSlugIsUniqueAndOwnerExists() {
        var createDto = new ProjectCreateDTO();
        createDto.setSlug("portfolio-api");
        var project = buildProject(null, "portfolio-api");
        var dto = buildProjectDto(10L, "portfolio-api");

        when(projectRepository.findBySlug("portfolio-api")).thenReturn(Optional.empty());
        when(projectMapper.map(createDto)).thenReturn(project);
        when(projectMapper.map(project)).thenReturn(dto);

        var actual = projectService.create(createDto);

        assertThat(actual).isSameAs(dto);
        verify(projectRepository).save(project);
    }

    @Test
    void createThrowsConflictWhenSlugExists() {
        var createDto = new ProjectCreateDTO();
        createDto.setSlug("portfolio-api");

        when(projectRepository.findBySlug("portfolio-api"))
                .thenReturn(Optional.of(buildProject(1L, "portfolio-api")));

        assertThatThrownBy(() -> projectService.create(createDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Project already exists with slug: portfolio-api");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void createThrowsWhenOwnerIsMissing() {
        var createDto = new ProjectCreateDTO();
        createDto.setSlug("portfolio-api");
        var project = buildProject(null, "portfolio-api");
        project.setOwner(null);

        when(projectRepository.findBySlug("portfolio-api")).thenReturn(Optional.empty());
        when(projectMapper.map(createDto)).thenReturn(project);

        assertThatThrownBy(() -> projectService.create(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project ownerId cannot be null");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateSavesProjectWhenSlugIsUniqueAndOwnerExists() {
        var updateDto = new ProjectUpdateDTO();
        var project = buildProject(5L, "portfolio-api");
        var dto = buildProjectDto(5L, "portfolio-api");

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectRepository.findBySlug("portfolio-api")).thenReturn(Optional.of(project));
        when(projectMapper.map(project)).thenReturn(dto);

        var actual = projectService.update(5L, updateDto);

        assertThat(actual).isSameAs(dto);
        verify(projectMapper).update(updateDto, project);
        verify(projectRepository).save(project);
    }

    @Test
    void updateThrowsWhenProjectIsMissing() {
        when(projectRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(5L, new ProjectUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: 5");
    }

    @Test
    void updateThrowsConflictWhenSlugBelongsToAnotherProject() {
        var updateDto = new ProjectUpdateDTO();
        var project = buildProject(5L, "portfolio-api");
        var existingProject = buildProject(9L, "portfolio-web");

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        doAnswer(invocation -> {
            project.setSlug("portfolio-web");
            return null;
        }).when(projectMapper).update(updateDto, project);
        when(projectRepository.findBySlug("portfolio-web")).thenReturn(Optional.of(existingProject));

        assertThatThrownBy(() -> projectService.update(5L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Project already exists with slug: portfolio-web");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateThrowsWhenOwnerIsMissing() {
        var updateDto = new ProjectUpdateDTO();
        var project = buildProject(5L, "portfolio-api");

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        doAnswer(invocation -> {
            project.setOwner(null);
            return null;
        }).when(projectMapper).update(updateDto, project);

        assertThatThrownBy(() -> projectService.update(5L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project ownerId cannot be null");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteClearsSkillsAndDeletesProject() {
        var project = buildProject(3L, "portfolio-api");
        project.setSkills(new HashSet<>(Set.of(Skill.builder().id(1L).name("Java").build())));

        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));

        projectService.delete(3L);

        assertThat(project.getSkills()).isEmpty();
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteThrowsWhenProjectIsMissing() {
        when(projectRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: 3");
    }

    private Project buildProject(Long id, String slug) {
        return Project.builder()
                .id(id)
                .name("Portfolio API")
                .slug(slug)
                .description("REST API")
                .owner(User.builder().id(1L).email("owner@example.com").build())
                .skills(new HashSet<>())
                .build();
    }

    private ProjectDTO buildProjectDto(Long id, String slug) {
        var dto = new ProjectDTO();
        dto.setId(id);
        dto.setOwnerId(1L);
        dto.setName("Portfolio API");
        dto.setSlug(slug);
        dto.setDescription("REST API");
        return dto;
    }
}
