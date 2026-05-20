package com.nhan.to_do_api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CategoryRepository extends JpaRepository<com.nhan.to_do_api.entity.Category, Long> {

}
