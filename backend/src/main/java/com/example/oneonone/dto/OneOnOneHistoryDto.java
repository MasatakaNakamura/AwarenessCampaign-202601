package com.example.oneonone.dto;

import com.example.oneonone.model.OneOnOneHistory;
import com.example.oneonone.model.OneOnOneStatus;
import java.time.LocalDateTime;

public record OneOnOneHistoryDto(
        Long id,
        Long oneOnOneId,
        OneOnOneStatus status,
        String changedBy,
        LocalDateTime changedAt,
        String comment) {

    public static OneOnOneHistoryDto from(OneOnOneHistory entity) {
        return new OneOnOneHistoryDto(
                entity.getId(),
                entity.getOneOnOneId(),
                entity.getStatus(),
                entity.getChangedBy(),
                entity.getChangedAt(),
                entity.getComment());
    }
}
