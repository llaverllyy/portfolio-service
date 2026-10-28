package com.example.portfolio;

import com.example.portfolio.model.Project;
import com.example.portfolio.model.Skill;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.ProjectRepository;
import com.example.portfolio.repository.SkillRepository;
import com.example.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PortfolioApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void clearDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        skillRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createProjectWithUnknownOwnerReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": 9999,
                                  "slug": "missing-owner",
                                  "name": "Missing owner",
                                  "description": "test"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found: 9999"));
    }

    @Test
    void createUserWithDuplicateEmailReturnsConflict() throws Exception {
        userRepository.save(User.builder()
                .firstName("Ivan")
                .lastName("Petrov")
                .email("ivan@example.com")
                .bio("bio")
                .build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Petr",
                                  "lastName": "Ivanov",
                                  "email": "ivan@example.com",
                                  "bio": "another"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists with email: ivan@example.com"));
    }

    @Test
    void patchUserUpdatesOnlyProvidedFields() throws Exception {
        User user = userRepository.save(User.builder()
                .firstName("Ivan")
                .lastName("Petrov")
                .email("patch-user@example.com")
                .bio("initial bio")
                .build());

        mockMvc.perform(patch("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Petr"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Petr"))
                .andExpect(jsonPath("$.lastName").value("Petrov"))
                .andExpect(jsonPath("$.email").value("patch-user@example.com"))
                .andExpect(jsonPath("$.bio").value("initial bio"));
    }

    @Test
    void deleteSkillRemovesLinksAndReturnsNoContent() throws Exception {
        Skill skill = skillRepository.save(Skill.builder()
                .name("Java")
                .build());

        User user = userRepository.save(User.builder()
                .firstName("Ivan")
                .lastName("Petrov")
                .email("delete-skill@example.com")
                .bio("bio")
                .build());
        user.getSkills().add(skill);
        userRepository.save(user);

        Project project = projectRepository.save(Project.builder()
                .owner(user)
                .name("Portfolio")
                .slug("portfolio-project")
                .description("desc")
                .skills(Set.of(skill))
                .build());

        mockMvc.perform(delete("/api/skills/{id}", skill.getId()))
                .andExpect(status().isNoContent());

        assertThat(skillRepository.findById(skill.getId())).isEmpty();
        assertThat(countRows("select count(*) from user_skills where skill_id = ?", skill.getId())).isZero();
        assertThat(countRows("select count(*) from project_skills where skill_id = ?", skill.getId())).isZero();
        assertThat(projectRepository.findById(project.getId())).isPresent();
    }

    private int countRows(String sql, Long id) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count == null ? 0 : count;
    }
}