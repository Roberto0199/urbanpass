package com.urbanpass.urbanpass.service;

import com.urbanpass.urbanpass.dto.CreateUserRequest;
import com.urbanpass.urbanpass.dto.UserResponse;
import com.urbanpass.urbanpass.entity.User;
import com.urbanpass.urbanpass.exception.BusinessException;
import com.urbanpass.urbanpass.exception.ResourceNotFoundException;
import com.urbanpass.urbanpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creando usuario con email: {}", request.getEmail());

        // Verificar que el email no esté registrado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con ese email.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        User saved = userRepository.save(user);
        log.info("Usuario creado con ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Mapper privado: convierte Entity → DTO
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}