package com.example.oneonone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import com.example.oneonone.model.OneOnOneStatus;

public record OneOnOneRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 120) String organizer,
        @Size(max = 120) String participant,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @Size(max = 255) String location,
        OneOnOneStatus status,
        @Size(max = 255) String tags,
        @Size(max = 1024) String notes) {
}
