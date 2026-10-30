package com.example.portfolio.service.impl;

import com.example.portfolio.dto.UserCreateDTO;
import com.example.portfolio.dto.UserDTO;
import com.example.portfolio.dto.UserUpdateDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.mapper.UserMapper;
import com.example.portfolio.model.Skill;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.UserRepository;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllReturnsMappedUsers() {
        var firstUser = buildUser(1L, "first@example.com");
        var secondUser = buildUser(2L, "second@example.com");
        var firstDto = buildUserDto(1L, "first@example.com");
        var secondDto = buildUserDto(2L, "second@example.com");

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(userMapper.map(firstUser)).thenReturn(firstDto);
        when(userMapper.map(secondUser)).thenReturn(secondDto);

        var actual = userService.getAll();

        assertThat(actual).containsExactly(firstDto, secondDto);
    }

    @Test
    void getByIdReturnsMappedUser() {
        var user = buildUser(1L, "user@example.com");
        var dto = buildUserDto(1L, "user@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.map(user)).thenReturn(dto);

        var actual = userService.getById(1L);

        assertThat(actual).isSameAs(dto);
    }

    @Test
    void getByIdThrowsWhenUserIsMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: 1");
    }

    @Test
    void createSavesMappedUserWhenEmailIsUnique() {
        var createDto = new UserCreateDTO();
        createDto.setEmail("new@example.com");
        var user = buildUser(null, "new@example.com");
        var dto = buildUserDto(10L, "new@example.com");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userMapper.map(createDto)).thenReturn(user);
        when(userMapper.map(user)).thenReturn(dto);

        var actual = userService.create(createDto);

        assertThat(actual).isSameAs(dto);
        verify(userRepository).save(user);
    }

    @Test
    void createThrowsConflictWhenEmailExists() {
        var createDto = new UserCreateDTO();
        createDto.setEmail("duplicate@example.com");

        when(userRepository.findByEmail("duplicate@example.com"))
                .thenReturn(Optional.of(buildUser(1L, "duplicate@example.com")));

        assertThatThrownBy(() -> userService.create(createDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already exists with email: duplicate@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateSavesUserWhenEmailIsUnique() {
        var updateDto = new UserUpdateDTO();
        var user = buildUser(5L, "current@example.com");
        var dto = buildUserDto(5L, "current@example.com");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("current@example.com")).thenReturn(Optional.of(user));
        when(userMapper.map(user)).thenReturn(dto);

        var actual = userService.update(5L, updateDto);

        assertThat(actual).isSameAs(dto);
        verify(userMapper).update(updateDto, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateThrowsWhenUserIsMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(5L, new UserUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: 5");
    }

    @Test
    void updateThrowsConflictWhenEmailBelongsToAnotherUser() {
        var updateDto = new UserUpdateDTO();
        var user = buildUser(5L, "current@example.com");
        var existingUser = buildUser(9L, "duplicate@example.com");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        doAnswer(invocation -> {
            user.setEmail("duplicate@example.com");
            return null;
        }).when(userMapper).update(updateDto, user);
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.update(5L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already exists with email: duplicate@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteClearsSkillsAndDeletesUser() {
        var user = buildUser(3L, "delete@example.com");
        user.setSkills(new HashSet<>(Set.of(Skill.builder().id(1L).name("Java").build())));

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        userService.delete(3L);

        assertThat(user.getSkills()).isEmpty();
        verify(userRepository).delete(user);
    }

    @Test
    void deleteThrowsWhenUserIsMissing() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: 3");
    }

    private User buildUser(Long id, String email) {
        return User.builder()
                .id(id)
                .firstName("Test")
                .lastName("User")
                .email(email)
                .bio("bio")
                .build();
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