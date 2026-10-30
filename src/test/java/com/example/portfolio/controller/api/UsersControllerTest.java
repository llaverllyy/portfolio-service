package com.example.portfolio.controller.api;

import com.example.portfolio.dto.UserCreateDTO;
import com.example.portfolio.dto.UserDTO;
import com.example.portfolio.exception.GlobalExceptionHandler;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.service.UserService;
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
class UsersControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UsersController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void indexReturnsUsersList() throws Exception {
        var firstUser = buildUserDto(1L, "first@example.com");
        var secondUser = buildUserDto(2L, "second@example.com");

        when(userService.getAll()).thenReturn(List.of(firstUser, secondUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("first@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("second@example.com"));
    }

    @Test
    void showReturnsUserById() throws Exception {
        when(userService.getById(5L)).thenReturn(buildUserDto(5L, "user@example.com"));

        mockMvc.perform(get("/api/users/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void showReturnsNotFoundWhenServiceThrows() throws Exception {
        when(userService.getById(5L)).thenThrow(new ResourceNotFoundException("User not found: 5"));

        mockMvc.perform(get("/api/users/{id}", 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found: 5"));
    }

    @Test
    void createReturnsCreatedUser() throws Exception {
        when(userService.create(any(UserCreateDTO.class))).thenReturn(buildUserDto(10L, "ivan@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ivan",
                                  "lastName": "Petrov",
                                  "email": "ivan@example.com",
                                  "bio": "bio"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void createReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Petrov",
                                  "email": "not-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(userService.update(eq(7L), any())).thenReturn(buildUserDto(7L, "updated@example.com"));

        mockMvc.perform(patch("/api/users/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 3L))
                .andExpect(status().isNoContent());

        verify(userService).delete(3L);
    }

    private UserDTO buildUserDto(Long id, String email) {
        var dto = new UserDTO();
        dto.setId(id);
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setEmail(email);
        dto.setBio("bio");
        return dto;
    }
}
