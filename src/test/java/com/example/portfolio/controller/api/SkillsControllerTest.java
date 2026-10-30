package com.example.portfolio.controller.api;

import com.example.portfolio.dto.SkillCreateDTO;
import com.example.portfolio.dto.SkillDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.GlobalExceptionHandler;
import com.example.portfolio.service.SkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SkillsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SkillService skillService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SkillsController(skillService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void indexReturnsSkillsList() throws Exception {
        when(skillService.getAll()).thenReturn(List.of(buildSkillDto(1L, "Java"), buildSkillDto(2L, "Spring")));

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Spring"));
    }

    @Test
    void createReturnsCreatedSkill() throws Exception {
        when(skillService.create(any(SkillCreateDTO.class))).thenReturn(buildSkillDto(3L, "Java"));

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Java"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void createReturnsBadRequestForBlankName() throws Exception {
        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(skillService);
    }

    @Test
    void createReturnsConflictWhenServiceThrows() throws Exception {
        when(skillService.create(any(SkillCreateDTO.class)))
                .thenThrow(new ConflictException("Skill already exists with name: Java"));

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Java"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Skill already exists with name: Java"));
    }

    @Test
    void updateReturnsUpdatedSkill() throws Exception {
        when(skillService.update(eq(4L), any())).thenReturn(buildSkillDto(4L, "Spring Boot"));

        mockMvc.perform(patch("/api/skills/{id}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spring Boot"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Spring Boot"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/skills/{id}", 6L))
                .andExpect(status().isNoContent());

        verify(skillService).delete(6L);
    }

    private SkillDTO buildSkillDto(Long id, String name) {
        var dto = new SkillDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
