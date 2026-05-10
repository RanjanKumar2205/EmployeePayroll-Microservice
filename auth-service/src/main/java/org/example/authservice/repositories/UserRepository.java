package org.example.authservice.repositories;

import org.example.authservice.entities.Role;
import org.example.authservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByRoleAndIsProtected(Role role, boolean isProtected);
}
