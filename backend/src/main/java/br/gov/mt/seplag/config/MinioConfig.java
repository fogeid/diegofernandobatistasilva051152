package br.gov.mt.seplag.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.internal-url}")
    private String minioInternalUrl;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    @Primary
    @Qualifier("minioInternalClient")
    public MinioClient minioInternalClient() {
        log.info("MinIO INTERNAL: {} bucket={}", minioInternalUrl, bucketName);

        MinioClient client = MinioClient.builder()
                .endpoint(minioInternalUrl)
                .credentials(accessKey, secretKey)
                .build();

        ensureBucketExists(client);
        return client;
    }

    @Bean
    @Qualifier("minioPublicClient")
    public MinioClient minioPublicClient() {
        log.info("MinIO PUBLIC (rewrite host to {}), signing via INTERNAL endpoint: {} bucket={}",
                minioPublicUrl, minioInternalUrl, bucketName);

        return MinioClient.builder()
                .endpoint(minioInternalUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    private void ensureBucketExists(MinioClient minioClient) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket '{}' criado", bucketName);
            } else {
                log.info("Bucket '{}' já existe", bucketName);
            }
        } catch (Exception e) {
            log.error("Erro bucket MinIO: {}", e.getMessage(), e);
        }
    }
}
