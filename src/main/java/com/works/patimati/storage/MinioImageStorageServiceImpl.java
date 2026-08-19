package com.works.patimati.storage;

import com.works.patimati.config.S3StorageProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * MinIO yerel (local) depolama servisi implementasyonu.
 *
 * <p>Yerel geliştirme ("local" veya varsayılan profil) sırasında MinIO nesne depolama
 * sunucusu üzerinden dosya yükleme, indirme URL'si imzalama ve silme işlemlerini yürütür.
 */
@Service
@Profile({"local", "default"})
public class MinioImageStorageServiceImpl extends S3ImageStorageService {

    public MinioImageStorageServiceImpl(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3StorageProperties properties
    ) {
        super(s3Client, s3Presigner, properties);
    }
}
