package com.works.patimati.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Cloudflare R2 / AWS S3 canlı ortam (production) depolama servisi implementasyonu.
 *
 * <p>Canlı ortamda ("prod" profili) Cloudflare R2 uç noktası üzerinden dosya yükleme
 * ve silme işlemlerini yürütür. Görseller presigned URL yerine Cloudflare Custom Domain / CDN
 * adresi (örneğin: https://media.patimati.com/ads/2026/08/...) üzerinden doğrudan sunulur.
 */
@Service
@Profile("prod")
public class S3ImageStorageServiceImpl implements ImageStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(S3ImageStorageServiceImpl.class);

    private static final String JPEG_CONTENT_TYPE = MediaType.IMAGE_JPEG_VALUE;
    private static final String STORAGE_SCHEME = "s3";

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicDomain;

    public S3ImageStorageServiceImpl(
            S3Client s3Client,
            @Value("${aws.s3.bucket-name:${app.storage.s3.bucket:patimati-medya-kutusu}}") String bucketName,
            @Value("${app.storage.r2.public-domain}") String publicDomain
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicDomain = publicDomain;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        return uploadImages(images, "ads");
    }

    /**
     * {@link #uploadImages(List)} ile aynı, yalnızca nesne anahtarının ön ekini
     * seçmeye izin verir (varsayılan {@code "ads"}) -- Instagram medyası
     * {@code "external"} önekiyle saklanır (bkz. {@link S3ImageStorageService}'in
     * eşdeğeri).
     */
    @Override
    public List<String> uploadImages(List<MultipartFile> images, String keyPrefix) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        if (images.size() > 3) {
            throw new InvalidImageException(
                    "Bir ilana en fazla 3 fotoğraf yüklenebilir"
            );
        }

        List<String> uploadedReferences = new ArrayList<>(images.size());

        try {
            for (MultipartFile image : images) {
                uploadedReferences.add(uploadImage(image, keyPrefix));
            }

            return List.copyOf(uploadedReferences);
        } catch (RuntimeException exception) {
            deleteUploadedImagesQuietly(uploadedReferences);
            throw exception;
        }
    }

    @Override
    public String createTemporaryReadUrl(String storageReference) {
        if (storageReference == null || storageReference.isBlank()) {
            throw new InvalidImageException("Fotoğraf depolama referansı boş olamaz");
        }

        String objectKey = extractObjectKey(storageReference);

        // Zaten http:// veya https:// protokolü içeriyorsa (örn: tamamen formatlanmış CDN adresi)
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            return objectKey;
        }

        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length() - 1)
                : publicDomain;

        if (!cleanDomain.startsWith("http://") && !cleanDomain.startsWith("https://")) {
            cleanDomain = "https://" + cleanDomain;
        }

        String cleanKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;

        return cleanDomain + "/" + cleanKey;
    }

    @Override
    public byte[] readImage(String storageReference) {
        String objectKey = extractDownloadObjectKey(storageReference);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        try {
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (SdkException exception) {
            log.error(
                    "Fotoğraf Cloudflare R2 depolama servisinden okunamadı. Reference: {}, Bucket: {}, Hata: {}",
                    storageReference,
                    bucketName,
                    exception.getMessage(),
                    exception
            );
            throw new ImageStorageException(
                    "Fotoğraf bulut depolamadan okunamadı",
                    exception
            );
        }
    }

    @Override
    public void deleteImages(Collection<String> storageReferences) {
        if (storageReferences == null || storageReferences.isEmpty()) {
            return;
        }

        for (String storageReference : storageReferences) {
            deleteImage(storageReference);
        }
    }

    private String uploadImage(MultipartFile image, String keyPrefix) {
        byte[] imageBytes = readAndValidateJpeg(image);
        String objectKey = createObjectKey(keyPrefix);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(JPEG_CONTENT_TYPE)
                .contentLength((long) imageBytes.length)
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(imageBytes)
            );
            // Madde 3: Veritabanına tam URI (s3://...) yerine doğrudan objectKey string'i döndürülür
            return objectKey;
        } catch (NoSuchBucketException exception) {
            log.error(
                    "Cloudflare R2 üzerinde '{}' isimli bucket bulunamadı! Bucket: {}",
                    bucketName,
                    exception
            );
            throw new ImageStorageException(
                    "Hedef depolama alanı (bucket: " + bucketName + ") bulunamadı",
                    exception
            );
        } catch (SdkException exception) {
            log.error(
                    "Fotoğraf Cloudflare R2 depolama servisine yüklenemedi. Bucket: {}, Hata: {}",
                    bucketName,
                    exception.getMessage(),
                    exception
            );
            throw new ImageStorageException(
                    "Fotoğraf bulut depolamaya yüklenemedi",
                    exception
            );
        }
    }

    private byte[] readAndValidateJpeg(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Boş fotoğraf yüklenemez");
        }

        if (image.getSize() > 5 * 1024 * 1024) {
            throw new InvalidImageException(
                    "Fotoğraf boyutu 5 MB sınırını aşamaz"
            );
        }

        String contentType = image.getContentType();
        if (contentType == null
                || (!JPEG_CONTENT_TYPE.equalsIgnoreCase(contentType)
                && !"image/jpg".equalsIgnoreCase(contentType))) {
            throw new InvalidImageException(
                    "Yalnızca JPEG formatındaki fotoğraflar yüklenebilir"
            );
        }

        try {
            byte[] imageBytes = image.getBytes();
            if (!hasJpegSignature(imageBytes)) {
                throw new InvalidImageException(
                        "Dosya içeriği geçerli bir JPEG fotoğrafı değil"
                );
            }
            return imageBytes;
        } catch (IOException exception) {
            throw new ImageStorageException(
                    "Fotoğraf dosyası okunamadı",
                    exception
            );
        }
    }

    private boolean hasJpegSignature(byte[] imageBytes) {
        return imageBytes.length >= 3
                && (imageBytes[0] & 0xFF) == 0xFF
                && (imageBytes[1] & 0xFF) == 0xD8
                && (imageBytes[2] & 0xFF) == 0xFF;
    }

    private String createObjectKey(String keyPrefix) {
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        return String.format(
                Locale.ROOT,
                "%s/%d/%02d/%s.jpg",
                keyPrefix,
                currentDate.getYear(),
                currentDate.getMonthValue(),
                UUID.randomUUID()
        );
    }

    private String extractObjectKey(String storageReference) {
        if (storageReference == null || storageReference.isBlank()) {
            throw new InvalidImageException(
                    "Fotoğraf depolama referansı boş olamaz"
            );
        }

        // Zaten http veya https protokolü barındırıyorsa
        if (storageReference.startsWith("http://") || storageReference.startsWith("https://")) {
            return storageReference;
        }

        // s3://bucketName/objectKey formatı için geriye dönük uyumluluk
        if (storageReference.startsWith(STORAGE_SCHEME + "://")) {
            try {
                URI reference = URI.create(storageReference);
                String objectKey = reference.getPath();

                if (objectKey != null && objectKey.length() > 1) {
                    return objectKey.substring(1);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Doğrudan objectKey string'i ise (örn: ads/2026/08/...jpg)
        return storageReference;
    }

    /**
     * CDN adresi saklanmış eski kayıtları nesne anahtarına dönüştürür. URL'in
     * kendisine istek atılmaz; veri her zaman yapılandırılmış R2 bucket'ından okunur.
     */
    private String extractDownloadObjectKey(String storageReference) {
        String objectKey = extractObjectKey(storageReference);

        if (!objectKey.startsWith("http://") && !objectKey.startsWith("https://")) {
            return objectKey;
        }

        try {
            String path = URI.create(objectKey).getPath();
            if (path == null || path.length() <= 1) {
                throw new InvalidImageException("Fotoğraf depolama referansı geçersiz");
            }
            return path.substring(1);
        } catch (IllegalArgumentException exception) {
            throw new InvalidImageException("Fotoğraf depolama referansı geçersiz");
        }
    }

    private void deleteImage(String storageReference) {
        String objectKey = extractObjectKey(storageReference);

        // Zaten tam URL gelmişse, domain sonrasını key olarak çıkar
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            try {
                URI uri = URI.create(objectKey);
                String path = uri.getPath();
                if (path != null && path.length() > 1) {
                    objectKey = path.substring(1);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        try {
            s3Client.deleteObject(deleteRequest);
        } catch (NoSuchBucketException exception) {
            log.error(
                    "Silme işlemi için Cloudflare R2 bucket'ı bulunamadı! Bucket: {}",
                    bucketName,
                    exception
            );
            throw new ImageStorageException(
                    "Hedef depolama alanı (bucket: " + bucketName + ") bulunamadı",
                    exception
            );
        } catch (SdkException exception) {
            log.error(
                    "Fotoğraf Cloudflare R2 depolama servisinden silinemedi. Reference: {}, Bucket: {}, Hata: {}",
                    storageReference,
                    bucketName,
                    exception.getMessage(),
                    exception
            );
            throw new ImageStorageException(
                    "Fotoğraf bulut depolamadan silinemedi",
                    exception
            );
        }
    }

    private void deleteUploadedImagesQuietly(
            Collection<String> storageReferences
    ) {
        for (String storageReference : storageReferences) {
            try {
                deleteImage(storageReference);
            } catch (RuntimeException rollbackException) {
                log.warn(
                        "Başarısız yükleme sonrası Cloudflare R2 nesnesi temizlenemedi: {}",
                        storageReference,
                        rollbackException
                );
            }
        }
    }
}
