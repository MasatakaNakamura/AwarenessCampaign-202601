package com.example.oneonone.controller;

import com.example.oneonone.dto.OneOnOneRequest;
import com.example.oneonone.model.OneOnOne;
import com.example.oneonone.service.OneOnOneService;
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

    public OneOnOneController(OneOnOneService service) {
        this.service = service;
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

    @PostMapping
    public ResponseEntity<OneOnOne> create(@Valid @RequestBody OneOnOneRequest request) {
        OneOnOne saved = service.create(request);
        return ResponseEntity.created(URI.create("/api/oneonones/" + saved.getId())).body(saved);
    }
}
