package ru.practicum.ewmservice.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(Long[] ids, Pageable pageable);

    Boolean existsByName(String name);
}
