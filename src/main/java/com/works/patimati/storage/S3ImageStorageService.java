package com.works.patimati.storage;

import com.works.patimati.config.S3StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class S3ImageStorageService implements ImageStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(S3ImageStorageService.class);

    private static final String JPEG_CONTENT_TYPE = MediaType.IMAGE_JPEG_VALUE;
    private static final String STORAGE_SCHEME = "s3";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;

    public S3ImageStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3StorageProperties properties
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        if (images.size() > properties.maxFileCount()) {
            throw new InvalidImageException(
                    "Bir ilana en fazla " + properties.maxFileCount()
                            + " fotoğraf yüklenebilir"
            );
        }

        List<String> uploadedReferences = new ArrayList<>(images.size());

        try {
            for (MultipartFile image : images) {
                uploadedReferences.add(uploadImage(image));
            }

            return List.copyOf(uploadedReferences);
        } catch (RuntimeException exception) {
            deleteUploadedImagesQuietly(uploadedReferences);
            throw exception;
        }
    }

    @Override
    public String createTemporaryReadUrl(String storageReference) {
        String objectKey = extractObjectKey(storageReference);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(properties.presignedUrlDuration())
                        .getObjectRequest(getObjectRequest)
                        .build();

        try {
            return s3Presigner.presignGetObject(presignRequest)
                    .url()
                    .toExternalForm();
        } catch (SdkException exception) {
            throw new ImageStorageException(
                    "Fotoğraf için geçici indirme adresi oluşturulamadı",
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

    private String uploadImage(MultipartFile image) {
        byte[] imageBytes = readAndValidateJpeg(image);
        String objectKey = createObjectKey();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(JPEG_CONTENT_TYPE)
                .contentLength((long) imageBytes.length)
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(imageBytes)
            );
            return createStorageReference(objectKey);
        } catch (SdkException exception) {
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

        if (image.getSize() > properties.maxFileSize().toBytes()) {
            throw new InvalidImageException(
                    "Fotoğraf boyutu "
                            + properties.maxFileSize().toMegabytes()
                            + " MB sınırını aşamaz"
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

    private String createObjectKey() {
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        return String.format(
                Locale.ROOT,
                "ads/%d/%02d/%s.jpg",
                currentDate.getYear(),
                currentDate.getMonthValue(),
                UUID.randomUUID()
        );
    }

    private String createStorageReference(String objectKey) {
        return STORAGE_SCHEME + "://" + properties.bucket() + "/" + objectKey;
    }

    private String extractObjectKey(String storageReference) {
        if (storageReference == null || storageReference.isBlank()) {
            throw new InvalidImageException(
                    "Fotoğraf depolama referansı boş olamaz"
            );
        }

        try {
            URI reference = URI.create(storageReference);
            String objectKey = reference.getPath();

            if (!STORAGE_SCHEME.equalsIgnoreCase(reference.getScheme())
                    || !properties.bucket().equals(reference.getHost())
                    || objectKey == null
                    || objectKey.length() <= 1) {
                throw new InvalidImageException(
                        "Geçersiz fotoğraf depolama referansı"
                );
            }

            return objectKey.substring(1);
        } catch (IllegalArgumentException exception) {
            throw new InvalidImageException(
                    "Geçersiz fotoğraf depolama referansı"
            );
        }
    }

    private void deleteImage(String storageReference) {
        String objectKey = extractObjectKey(storageReference);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();

        try {
            s3Client.deleteObject(deleteRequest);
        } catch (SdkException exception) {
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
                        "Başarısız yükleme sonrası S3 nesnesi temizlenemedi: {}",
                        storageReference,
                        rollbackException
                );
            }
        }
    }
}
