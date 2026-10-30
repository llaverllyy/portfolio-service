package com.example.portfolio.repository;

import com.example.portfolio.model.Skill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SkillRepositoryTest {

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void findByNameReturnsSkillWhenNameExists() {
        var skill = skillRepository.save(buildSkill("Java"));

        var actual = skillRepository.findByName(skill.getName());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(skill.getId());
        assertThat(actual.get().getName()).isEqualTo(skill.getName());
    }

    @Test
    void findByNameReturnsEmptyWhenNameDoesNotExist() {
        var actual = skillRepository.findByName("Missing Skill");

        assertThat(actual).isEmpty();
    }

    private Skill buildSkill(String name) {
        return Skill.builder()
                .name(name)
                .build();
    }
}