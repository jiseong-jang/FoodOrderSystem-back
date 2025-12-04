package com.mrdinner.repository;

import com.mrdinner.entity.User;
import com.mrdinner.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByIdAndRole(String id, UserRole role);
    boolean existsById(String id);
}

