package com.example.oneonone.repository;

import com.example.oneonone.model.OneOnOne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneOnOneRepository extends JpaRepository<OneOnOne, Long> {
}
