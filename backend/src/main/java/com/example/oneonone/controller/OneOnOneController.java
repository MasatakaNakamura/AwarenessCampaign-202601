package com.example.oneonone.controller;

import com.example.oneonone.dto.OneOnOneHistoryDto;
import com.example.oneonone.dto.OneOnOneRequest;
import com.example.oneonone.model.OneOnOne;
import com.example.oneonone.service.OneOnOneHistoryService;
import com.example.oneonone.service.OneOnOneService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oneonones")
public class OneOnOneController {

    private final OneOnOneService service;
    private final OneOnOneHistoryService historyService;

    public OneOnOneController(OneOnOneService service, OneOnOneHistoryService historyService) {
        this.service = service;
        this.historyService = historyService;
    }

    @GetMapping
    public List<OneOnOne> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OneOnOne> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OneOnOneHistoryDto>> history(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(historyService.findHistory(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<OneOnOne> create(@Valid @RequestBody OneOnOneRequest request) {
        OneOnOne saved = service.create(request);
        return ResponseEntity.created(URI.create("/api/oneonones/" + saved.getId())).body(saved);
    }
}
