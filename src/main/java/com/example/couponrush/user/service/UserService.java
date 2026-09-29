package com.example.couponrush.user.service;

import com.example.couponrush.common.exception.UserNotFoundException;
import com.example.couponrush.user.dto.request.UserRequest;
import com.example.couponrush.user.dto.response.UserResponse;
import com.example.couponrush.user.entity.User;
import com.example.couponrush.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser (UserRequest dto) {
        User user = User.builder()
                .name(dto.getName())
                .build();

        userRepository.save(user);

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers () {
        return userRepository.findAll()
                .stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
