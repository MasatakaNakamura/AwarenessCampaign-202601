package com.example.oneonone.service;

import com.example.oneonone.dto.OneOnOneRequest;
import com.example.oneonone.model.OneOnOne;
import com.example.oneonone.model.OneOnOneStatus;
import com.example.oneonone.repository.OneOnOneRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OneOnOneService {

    private final OneOnOneRepository repository;

    @Transactional(readOnly = true)
    public List<OneOnOne> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<OneOnOne> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public OneOnOne create(OneOnOneRequest request) {
        OneOnOneStatus status = request.status() != null ? request.status() : OneOnOneStatus.SCHEDULED;
        OneOnOne entity = OneOnOne.builder()
                .title(request.title())
                .organizer(request.organizer())
                .participant(request.participant())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .location(request.location())
                .status(status)
                .tags(request.tags())
                .notes(request.notes())
                .build();
        return repository.save(entity);
    }
}
