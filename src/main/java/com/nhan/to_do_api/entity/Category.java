package com.nhan.to_do_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String name;
    String color;
    LocalDateTime completedAt;
    LocalDateTime updatedAt;
    @ManyToOne
    User user;
    @OneToMany
    Todo todo;
}
