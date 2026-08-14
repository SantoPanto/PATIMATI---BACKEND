package com.works.patimati.storage;

import com.works.patimati.config.S3StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ImageStorageServiceTest {

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3ImageStorageService storageService;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);

        S3StorageProperties properties = new S3StorageProperties(
                "patimati-test",
                "eu-central-1",
                null,
                false,
                "minioadmin",
                "minioadmin",
                Duration.ofMinutes(15),
                5,
                DataSize.ofMegabytes(10)
        );

        storageService = new S3ImageStorageService(
                s3Client,
                s3Presigner,
                properties
        );
    }

    @Test
    void shouldUploadJpegWithRequiredContentType() {
        byte[] jpegBytes = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x00
        };

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.jpg",
                "image/jpeg",
                jpegBytes
        );

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(PutObjectResponse.builder().build());

        List<String> references =
                storageService.uploadImages(List.of(image));

        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
                requestCaptor.capture(),
                any(RequestBody.class)
        );

        PutObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo("patimati-test");
        assertThat(request.contentType()).isEqualTo("image/jpeg");
        assertThat(request.key())
                .startsWith("ads/")
                .endsWith(".jpg");
        assertThat(references)
                .singleElement()
                .asString()
                .startsWith("s3://patimati-test/ads/");
    }

    @Test
    void shouldRejectAFileThatIsNotJpeg() {
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(
                () -> storageService.uploadImages(List.of(image))
        )
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("JPEG");

        verify(s3Client, never()).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    @Test
    void shouldRejectMoreImagesThanConfiguredLimit() {
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.jpg",
                "image/jpeg",
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF
                }
        );

        assertThatThrownBy(
                () -> storageService.uploadImages(
                        List.of(image, image, image, image, image, image)
                )
        )
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("en fazla 5");

        verify(s3Client, never()).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    @Test
    void shouldCreateAFifteenMinuteTemporaryReadUrl()
            throws MalformedURLException {
        PresignedGetObjectRequest presignedRequest =
                mock(PresignedGetObjectRequest.class);

        when(presignedRequest.url())
                .thenReturn(new URL("https://example.com/cat.jpg"));
        when(s3Presigner.presignGetObject(
                any(GetObjectPresignRequest.class)
        )).thenReturn(presignedRequest);

        String temporaryUrl = storageService.createTemporaryReadUrl(
                "s3://patimati-test/ads/2026/07/cat.jpg"
        );

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);

        verify(s3Presigner).presignGetObject(requestCaptor.capture());

        GetObjectPresignRequest request = requestCaptor.getValue();
        GetObjectRequest objectRequest = request.getObjectRequest();

        assertThat(request.signatureDuration())
                .isEqualTo(Duration.ofMinutes(15));
        assertThat(objectRequest.bucket()).isEqualTo("patimati-test");
        assertThat(objectRequest.key())
                .isEqualTo("ads/2026/07/cat.jpg");
        assertThat(temporaryUrl)
                .isEqualTo("https://example.com/cat.jpg");
    }

    @Test
    void shouldHandleNoSuchBucketExceptionWhenBucketDoesNotExist() {
        byte[] jpegBytes = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x00
        };

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.jpg",
                "image/jpeg",
                jpegBytes
        );

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenThrow(NoSuchBucketException.builder().message("The specified bucket does not exist").build());

        assertThatThrownBy(
                () -> storageService.uploadImages(List.of(image))
        )
                .isInstanceOf(ImageStorageException.class)
                .hasMessageContaining("patimati-test");
    }
}
