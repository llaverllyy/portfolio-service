package com.example.portfolio.service.impl;

import com.example.portfolio.dto.SkillCreateDTO;
import com.example.portfolio.dto.SkillDTO;
import com.example.portfolio.dto.SkillUpdateDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.mapper.SkillMapper;
import com.example.portfolio.model.Project;
import com.example.portfolio.model.Skill;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.SkillRepository;
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
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void getAllReturnsMappedSkills() {
        var firstSkill = buildSkill(1L, "Java");
        var secondSkill = buildSkill(2L, "Spring");
        var firstDto = buildSkillDto(1L, "Java");
        var secondDto = buildSkillDto(2L, "Spring");

        when(skillRepository.findAll()).thenReturn(List.of(firstSkill, secondSkill));
        when(skillMapper.map(firstSkill)).thenReturn(firstDto);
        when(skillMapper.map(secondSkill)).thenReturn(secondDto);

        var actual = skillService.getAll();

        assertThat(actual).containsExactly(firstDto, secondDto);
    }

    @Test
    void getByIdReturnsMappedSkill() {
        var skill = buildSkill(1L, "Java");
        var dto = buildSkillDto(1L, "Java");

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillMapper.map(skill)).thenReturn(dto);

        var actual = skillService.getById(1L);

        assertThat(actual).isSameAs(dto);
    }

    @Test
    void getByIdThrowsWhenSkillIsMissing() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Skill not found: 1");
    }

    @Test
    void createSavesSkillWhenNameIsUnique() {
        var createDto = new SkillCreateDTO();
        createDto.setName("Java");
        var skill = buildSkill(null, "Java");
        var dto = buildSkillDto(10L, "Java");

        when(skillRepository.findByName("Java")).thenReturn(Optional.empty());
        when(skillMapper.map(createDto)).thenReturn(skill);
        when(skillMapper.map(skill)).thenReturn(dto);

        var actual = skillService.create(createDto);

        assertThat(actual).isSameAs(dto);
        verify(skillRepository).save(skill);
    }

    @Test
    void createThrowsConflictWhenNameExists() {
        var createDto = new SkillCreateDTO();
        createDto.setName("Java");

        when(skillRepository.findByName("Java")).thenReturn(Optional.of(buildSkill(1L, "Java")));

        assertThatThrownBy(() -> skillService.create(createDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Skill already exists with name: Java");

        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void updateSavesSkillWhenNameIsUnique() {
        var updateDto = new SkillUpdateDTO();
        var skill = buildSkill(5L, "Java");
        var dto = buildSkillDto(5L, "Java");

        when(skillRepository.findById(5L)).thenReturn(Optional.of(skill));
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        when(skillMapper.map(skill)).thenReturn(dto);

        var actual = skillService.update(5L, updateDto);

        assertThat(actual).isSameAs(dto);
        verify(skillMapper).update(updateDto, skill);
        verify(skillRepository).save(skill);
    }

    @Test
    void updateThrowsConflictWhenNameBelongsToAnotherSkill() {
        var updateDto = new SkillUpdateDTO();
        var skill = buildSkill(5L, "Java");
        var existingSkill = buildSkill(9L, "Spring");

        when(skillRepository.findById(5L)).thenReturn(Optional.of(skill));
        doAnswer(invocation -> {
            skill.setName("Spring");
            return null;
        }).when(skillMapper).update(updateDto, skill);
        when(skillRepository.findByName("Spring")).thenReturn(Optional.of(existingSkill));

        assertThatThrownBy(() -> skillService.update(5L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Skill already exists with name: Spring");

        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void deleteRemovesLinksAndDeletesSkill() {
        var skill = buildSkill(3L, "Java");
        var user = User.builder().id(1L).skills(new HashSet<>()).build();
        var project = Project.builder().id(2L).skills(new HashSet<>()).build();
        user.getSkills().add(skill);
        project.getSkills().add(skill);
        skill.setUsers(new HashSet<>(Set.of(user)));
        skill.setProjects(new HashSet<>(Set.of(project)));

        when(skillRepository.findById(3L)).thenReturn(Optional.of(skill));

        skillService.delete(3L);

        assertThat(skill.getUsers()).isEmpty();
        assertThat(skill.getProjects()).isEmpty();
        assertThat(user.getSkills()).doesNotContain(skill);
        assertThat(project.getSkills()).doesNotContain(skill);
        verify(skillRepository).delete(skill);
    }

    @Test
    void deleteThrowsWhenSkillIsMissing() {
        when(skillRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.delete(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Skill not found: 3");
    }

    private Skill buildSkill(Long id, String name) {
        return Skill.builder()
                .id(id)
                .name(name)
                .users(new HashSet<>())
                .projects(new HashSet<>())
                .build();
    }

    private SkillDTO buildSkillDto(Long id, String name) {
        var dto = new SkillDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}