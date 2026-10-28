package com.example.portfolio.service;

import com.example.portfolio.dto.UserCreateDTO;
import com.example.portfolio.dto.UserDTO;
import com.example.portfolio.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();
    UserDTO getById(Long id);
    UserDTO create(UserCreateDTO dto);
    UserDTO update(Long id, UserUpdateDTO dto);
    void delete(Long id);
}