package com.example.ainote.note.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteDtos {
    public record Create(
        @NotBlank(message = "title must not be blank") String title,
        @NotBlank(message = "contentMd must not be blank") String contentMd
    ) {}

    public record Update(String title, String contentMd, String status) {}

    public record Response(Long id, String title, String contentMd, String contentText, String status) {}
}
