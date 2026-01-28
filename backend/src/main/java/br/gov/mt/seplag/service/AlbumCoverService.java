package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.AlbumCoverResponse;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.AlbumCover;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumCoverRepository;
import br.gov.mt.seplag.repository.AlbumRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AlbumCoverService {

    private final AlbumCoverRepository albumCoverRepository;
    private final AlbumRepository albumRepository;
    private final NotificationService notificationService;

    private final @Qualifier("minioInternalClient") MinioClient minioInternalClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    public List<AlbumCoverResponse> findByAlbumId(Long albumId) {
        albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + albumId));

        return albumCoverRepository.findByAlbumId(albumId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AlbumCoverResponse findById(Long albumId, Long coverId) {
        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> new ResourceNotFoundException("Capa não encontrada com ID: " + coverId));

        if (!cover.getAlbum().getId().equals(albumId)) {
            throw new BadRequestException("Essa capa não pertence ao álbum informado");
        }

        return toResponse(cover);
    }

    @Transactional
    public AlbumCoverResponse uploadCover(Long albumId, MultipartFile file, String username) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Álbum não encontrado com ID: " + albumId));

        validateImageFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String minioKey = generateMinioKey(albumId, extension);

        try (InputStream inputStream = file.getInputStream()) {

            minioInternalClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(minioKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            AlbumCover saved = albumCoverRepository.save(
                    AlbumCover.builder()
                            .album(album)
                            .fileName(file.getOriginalFilename())
                            .minioKey(minioKey)
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .build()
            );

            notificationService.notifyCoverUploaded(
                    album.getId(),
                    album.getTitle(),
                    username
            );

            log.info("Capa enviada para álbum {} por {}: {}", albumId, username, minioKey);
            return toResponse(saved);

        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem", e);
            throw new RuntimeException("Erro ao fazer upload da imagem", e);
        }
    }

    @Transactional
    public void delete(Long albumId, Long coverId) {
        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> new ResourceNotFoundException("Capa não encontrada com ID: " + coverId));

        if (!cover.getAlbum().getId().equals(albumId)) {
            throw new BadRequestException("Essa capa não pertence ao álbum informado");
        }

        delete(cover);
    }

    @Transactional
    public void deleteByAlbumId(Long albumId) {
        List<AlbumCover> covers = albumCoverRepository.findByAlbumId(albumId);
        covers.forEach(this::delete);
    }

    private void delete(AlbumCover cover) {
        try {
            String normalizedKey = normalizeObjectKey(cover.getMinioKey());

            minioInternalClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(normalizedKey)
                            .build()
            );

            albumCoverRepository.delete(cover);

            log.info("Capa deletada: {}", normalizedKey);

        } catch (Exception e) {
            log.error("Erro ao deletar imagem", e);
            throw new RuntimeException("Erro ao deletar imagem", e);
        }
    }

    private String publicUrl(String minioKey) {
        String base = (minioPublicUrl == null ? "" : minioPublicUrl.trim());
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        String key = normalizeObjectKey(minioKey);
        if (key == null || key.isBlank()) return null;

        if (key.startsWith("/")) key = key.substring(1);

        return base + "/" + bucketName + "/" + key;
    }

    private AlbumCoverResponse toResponse(AlbumCover cover) {
        return AlbumCoverResponse.builder()
                .id(cover.getId())
                .albumId(cover.getAlbum().getId())
                .fileName(cover.getFileName())
                .imageUrl(publicUrl(cover.getMinioKey()))
                .contentType(cover.getContentType())
                .fileSize(cover.getFileSize())
                .createdAt(cover.getCreatedAt())
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Arquivo não pode ser vazio");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Arquivo deve ser uma imagem");
        }

        if (file.getOriginalFilename() == null ||
                !file.getOriginalFilename().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new BadRequestException("Formato de imagem não suportado");
        }

        if (file.getSize() > 10L * 1024 * 1024) {
            throw new BadRequestException("Imagem muito grande. Máximo: 10MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateMinioKey(Long albumId, String extension) {
        return String.format("%d/%s.%s", albumId, UUID.randomUUID(), extension);
    }

    private String normalizeObjectKey(String key) {
        if (key == null) return null;

        String k = key.trim();
        if (k.startsWith("/")) k = k.substring(1);

        String bucketPrefix = bucketName + "/";
        if (k.startsWith(bucketPrefix)) {
            k = k.substring(bucketPrefix.length());
        }

        if (k.startsWith("albums/")) {
            k = k.substring("albums/".length());
        }

        return k;
    }
}
