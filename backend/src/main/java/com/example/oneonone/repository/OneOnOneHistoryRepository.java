package com.example.oneonone.repository;

import com.example.oneonone.model.OneOnOneHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneOnOneHistoryRepository extends JpaRepository<OneOnOneHistory, Long> {

    List<OneOnOneHistory> findByOneOnOneIdOrderByChangedAtDesc(Long oneOnOneId);
}
