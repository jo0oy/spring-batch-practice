package com.jo0oy.springbatchpractice.part4;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdatedDateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
