package com.tattoo.scheduler.service;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.dto.auth.RegisterRequest;
import com.tattoo.scheduler.mapper.UserMapper;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.exception.auth.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public User registerUser(RegisterRequest request){
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .birthDate(request.birthDate())
                .build();

        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Transactional (readOnly = true)
    public User authenticate(String email, String rawPassword){
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        if(!passwordEncoder.matches(rawPassword, entity.getPassword())){
            throw new UsernameNotFoundException("Invalid credentials");
        }
        return userMapper.toDomain(entity);
    }
}
