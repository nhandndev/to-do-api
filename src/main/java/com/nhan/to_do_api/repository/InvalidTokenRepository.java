package com.nhan.to_do_api.repository;

import com.nhan.to_do_api.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken,String> {
}
