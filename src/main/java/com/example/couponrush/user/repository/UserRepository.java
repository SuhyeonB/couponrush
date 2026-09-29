package com.example.couponrush.user.repository;

import com.example.couponrush.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}