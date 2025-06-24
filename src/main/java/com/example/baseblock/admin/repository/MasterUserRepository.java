package com.example.baseblock.admin.repository;

import com.example.baseblock.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterUserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByIdAsc();
}
