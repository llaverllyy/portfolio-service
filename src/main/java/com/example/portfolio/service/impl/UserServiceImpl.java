package com.example.portfolio.service.impl;

import com.example.portfolio.dto.UserCreateDTO;
import com.example.portfolio.dto.UserDTO;
import com.example.portfolio.dto.UserUpdateDTO;
import com.example.portfolio.exception.ConflictException;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.mapper.UserMapper;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public UserDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return userMapper.map(user);
    }

    @Override
    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new ConflictException("User already exists with email: " + dto.getEmail());
        }
        User user = userMapper.map(dto);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userMapper.update(dto, user);
        validateUniqueEmail(user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.getSkills().clear();
        userRepository.delete(user);
    }

    private void validateUniqueEmail(User user) {
        userRepository.findByEmail(user.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new ConflictException("User already exists with email: " + user.getEmail());
                });
    }
}