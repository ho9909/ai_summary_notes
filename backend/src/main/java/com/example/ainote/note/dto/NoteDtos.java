package com.example.ainote.note.dto;

import com.example.ainote.note.domain.Note;
import jakarta.validation.constraints.NotBlank;

public class NoteDtos {
    public record Create(
        @NotBlank(message = "title must not be blank") String title,
        @NotBlank(message = "contentMd must not be blank") String contentMd,
        String tags
    ) {}
    public record Update(String title, String contentMd, String status, String tags) {}
    public record Response(Long id, String title, String contentMd, String contentText, String status, String tags) {}

    public static Response fromEntity(Note n) {
        if (n == null) return null;
        // status 타입이 enum이면 n.getStatus().name() 으로
        return new Response(
                n.getId(),
                n.getTitle(),
                n.getContentMd(),
                n.getContentText(),
                n.getStatus(),      // enum이면 .name()
                n.getTags()
        );
    }
}
