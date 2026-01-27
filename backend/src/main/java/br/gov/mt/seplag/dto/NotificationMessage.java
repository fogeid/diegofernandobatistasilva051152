package br.gov.mt.seplag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private NotificationType type;

    private String message;

    private Object data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String username;

    public enum NotificationType {
        ALBUM_CREATED,
        ALBUM_UPDATED,
        ALBUM_DELETED,
        ARTIST_CREATED,
        ARTIST_UPDATED,
        ARTIST_DELETED,
        COVER_UPLOADED,
        COVER_DELETED,
        REGIONAIS_SYNCED
    }
}