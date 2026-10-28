package com.example.portfolio.controller.api;

import com.example.portfolio.dto.SkillCreateDTO;
import com.example.portfolio.dto.SkillDTO;
import com.example.portfolio.dto.SkillUpdateDTO;
import com.example.portfolio.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final SkillService skillService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SkillDTO> index() {
        return skillService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SkillDTO show(@PathVariable Long id) {
        return skillService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillDTO create(@Valid @RequestBody SkillCreateDTO dto) {
        return skillService.create(dto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SkillDTO update(@PathVariable Long id, @Valid @RequestBody SkillUpdateDTO dto) {
        return skillService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        skillService.delete(id);
    }
}