package com.rusko.repository;

import com.rusko.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  public User findByUsername(String username);
}
