package com.example.portfolio.controller.api;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.exception.GlobalExceptionHandler;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.service.ProjectService;
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
class ProjectsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectsController(projectService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void indexReturnsProjectsList() throws Exception {
        when(projectService.getAll()).thenReturn(List.of(
                buildProjectDto(1L, "portfolio-api"),
                buildProjectDto(2L, "portfolio-web")
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].slug").value("portfolio-api"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].slug").value("portfolio-web"));
    }

    @Test
    void showReturnsProjectById() throws Exception {
        when(projectService.getById(8L)).thenReturn(buildProjectDto(8L, "portfolio-api"));

        mockMvc.perform(get("/api/projects/{id}", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.slug").value("portfolio-api"));
    }

    @Test
    void showReturnsNotFoundWhenServiceThrows() throws Exception {
        when(projectService.getById(8L)).thenThrow(new ResourceNotFoundException("Project not found: 8"));

        mockMvc.perform(get("/api/projects/{id}", 8L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project not found: 8"));
    }

    @Test
    void createReturnsCreatedProject() throws Exception {
        when(projectService.create(any(ProjectCreateDTO.class))).thenReturn(buildProjectDto(11L, "portfolio-api"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": 1,
                                  "slug": "portfolio-api",
                                  "name": "Portfolio API",
                                  "description": "REST API"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.slug").value("portfolio-api"));
    }

    @Test
    void createReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(projectService);
    }

    @Test
    void updateReturnsUpdatedProject() throws Exception {
        when(projectService.update(eq(9L), any())).thenReturn(buildProjectDto(9L, "portfolio-updated"));

        mockMvc.perform(patch("/api/projects/{id}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "portfolio-updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.slug").value("portfolio-updated"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/projects/{id}", 12L))
                .andExpect(status().isNoContent());

        verify(projectService).delete(12L);
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
