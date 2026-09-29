package com.example.couponrush.user.controller;

import com.example.couponrush.user.dto.request.UserRequest;
import com.example.couponrush.user.dto.response.UserResponse;
import com.example.couponrush.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser (
            @Valid @RequestBody UserRequest dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers () {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser (
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getUser(id));
    }
}
