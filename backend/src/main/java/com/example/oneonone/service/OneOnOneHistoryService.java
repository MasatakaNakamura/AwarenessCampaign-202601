package com.example.oneonone.service;

import com.example.oneonone.dto.OneOnOneHistoryDto;
import com.example.oneonone.repository.OneOnOneHistoryRepository;
import com.example.oneonone.repository.OneOnOneRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OneOnOneHistoryService {

    private final OneOnOneHistoryRepository historyRepository;
    private final OneOnOneRepository oneOnOneRepository;

    @Transactional(readOnly = true)
    public List<OneOnOneHistoryDto> findHistory(Long oneOnOneId) {
        if (!oneOnOneRepository.existsById(oneOnOneId)) {
            throw new EntityNotFoundException("OneOnOne not found: " + oneOnOneId);
        }
        return historyRepository.findByOneOnOneIdOrderByChangedAtDesc(oneOnOneId)
                .stream()
                .map(OneOnOneHistoryDto::from)
                .toList();
    }
}
