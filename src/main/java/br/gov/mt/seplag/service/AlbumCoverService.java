package br.gov.mt.seplag.service;

import br.gov.mt.seplag.dto.AlbumCoverResponse;
import br.gov.mt.seplag.entity.Album;
import br.gov.mt.seplag.entity.AlbumCover;
import br.gov.mt.seplag.exception.BadRequestException;
import br.gov.mt.seplag.exception.ResourceNotFoundException;
import br.gov.mt.seplag.repository.AlbumCoverRepository;
import br.gov.mt.seplag.repository.AlbumRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumCoverService {

    private final AlbumCoverRepository albumCoverRepository;
    private final AlbumRepository albumRepository;
    private final NotificationService notificationService;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.presigned-url-expiration}")
    private int presignedUrlExpiration;

    public List<AlbumCoverResponse> findByAlbumId(Long albumId) {
        return albumCoverRepository.findByAlbumId(albumId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AlbumCoverResponse findById(Long id) {
        AlbumCover cover = albumCoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capa não encontrada com ID: " + id));

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

            minioClient.putObject(
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

            return toResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da imagem", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        AlbumCover cover = albumCoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capa não encontrada com ID: " + id));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(cover.getMinioKey())
                            .build()
            );

            albumCoverRepository.delete(cover);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar imagem: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteByAlbumId(Long albumId) {
        List<AlbumCover> covers = albumCoverRepository.findByAlbumId(albumId);

        for (AlbumCover cover : covers) {
            delete(cover.getId());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Arquivo não pode ser vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Arquivo deve ser uma imagem");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new BadRequestException("Formato de imagem não suportado. Use: jpg, jpeg, png, gif, webp");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("Imagem muito grande. Máximo: 10MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateMinioKey(Long albumId, String extension) {
        String uuid = UUID.randomUUID().toString();
        return String.format("albums/%d/%s.%s", albumId, uuid, extension);
    }

    private String generatePresignedUrl(String minioKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(minioKey)
                            .expiry(presignedUrlExpiration, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL de download: " + e.getMessage(), e);
        }
    }

    private AlbumCoverResponse toResponse(AlbumCover cover) {
        String presignedUrl = generatePresignedUrl(cover.getMinioKey());

        return AlbumCoverResponse.builder()
                .id(cover.getId())
                .albumId(cover.getAlbum().getId())
                .fileName(cover.getFileName())
                .imageUrl(presignedUrl)
                .contentType(cover.getContentType())
                .fileSize(cover.getFileSize())
                .createdAt(cover.getCreatedAt())
                .build();
    }
}