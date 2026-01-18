package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String topic, NotificationMessage notification) {
        try {
            log.info("Enviando notificação para {}: {}", topic, notification.getMessage());
            messagingTemplate.convertAndSend(topic, notification);
        } catch (Exception e) {
            log.error("Erro ao enviar notificação WebSocket", e);
        }
    }

    public void notifyAlbumCreated(Long albumId, String title, String username) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationMessage.NotificationType.ALBUM_CREATED)
                .message(String.format("Novo álbum criado: %s", title))
                .data(albumId)
                .username(username)
                .build();

        sendNotification("/topic/albums", notification);
    }

    public void notifyAlbumUpdated(Long albumId, String title, String username) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationMessage.NotificationType.ALBUM_UPDATED)
                .message(String.format("Álbum atualizado: %s", title))
                .data(albumId)
                .username(username)
                .build();

        sendNotification("/topic/albums", notification);
    }

    public void notifyAlbumDeleted(Long albumId, String title, String username) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationMessage.NotificationType.ALBUM_DELETED)
                .message(String.format("Álbum deletado: %s", title))
                .data(albumId)
                .username(username)
                .build();

        sendNotification("/topic/albums", notification);
    }

    public void notifyArtistCreated(Long artistId, String name, String username) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationMessage.NotificationType.ARTIST_CREATED)
                .message(String.format("Novo artista criado: %s", name))
                .data(artistId)
                .username(username)
                .build();

        sendNotification("/topic/artists", notification);
    }

    public void notifyCoverUploaded(Long albumId, String albumTitle, String username) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationMessage.NotificationType.COVER_UPLOADED)
                .message(String.format("Nova capa enviada para: %s", albumTitle))
                .data(albumId)
                .username(username)
                .build();

        sendNotification("/topic/covers", notification);
    }
}