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
import com.example.portfolio.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public List<SkillDTO> getAll() {
        return skillRepository.findAll().stream()
                .map(skillMapper::map)
                .toList();
    }

    @Override
    public SkillDTO getById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
        return skillMapper.map(skill);
    }

    @Override
    @Transactional
    public SkillDTO create(SkillCreateDTO dto) {
        validateUniqueName(dto.getName(), null);
        Skill skill = skillMapper.map(dto);
        skillRepository.save(skill);
        return skillMapper.map(skill);
    }

    @Override
    @Transactional
    public SkillDTO update(Long id, SkillUpdateDTO dto) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
        skillMapper.update(dto, skill);
        validateUniqueName(skill.getName(), skill.getId());
        skillRepository.save(skill);
        return skillMapper.map(skill);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));

        for (User user : new HashSet<>(skill.getUsers())) {
            user.getSkills().remove(skill);
            skill.getUsers().remove(user);
        }

        for (Project project : new HashSet<>(skill.getProjects())) {
            project.getSkills().remove(skill);
            skill.getProjects().remove(project);
        }

        skillRepository.delete(skill);
    }

    private void validateUniqueName(String skillName, Long currentSkillId) {
        skillRepository.findByName(skillName)
                .filter(existingSkill -> !existingSkill.getId().equals(currentSkillId))
                .ifPresent(existingSkill -> {
                    throw new ConflictException("Skill already exists with name: " + skillName);
                });
    }
}