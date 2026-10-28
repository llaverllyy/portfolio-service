package com.example.portfolio.service;

import com.example.portfolio.dto.SkillCreateDTO;
import com.example.portfolio.dto.SkillDTO;
import com.example.portfolio.dto.SkillUpdateDTO;

import java.util.List;

public interface SkillService {
    List<SkillDTO> getAll();
    SkillDTO getById(Long id);
    SkillDTO create(SkillCreateDTO dto);
    SkillDTO update(Long id, SkillUpdateDTO dto);
    void delete(Long id);
}