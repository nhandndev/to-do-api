package com.nhan.to_do_api.repository;

import com.nhan.to_do_api.entity.Todo;
import com.nhan.to_do_api.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<com.nhan.to_do_api.entity.Todo, Long> {
    Optional<Todo> findById (long id);
    Optional<Todo> findAllByUser (User user);
    Optional<Todo> findByIdAndUser (Long id, User user);
}
