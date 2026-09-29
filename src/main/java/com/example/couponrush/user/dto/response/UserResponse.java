package com.example.couponrush.user.dto.response;

import com.example.couponrush.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(Long id, String name) {

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
