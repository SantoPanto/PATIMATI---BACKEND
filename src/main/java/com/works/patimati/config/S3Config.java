package com.works.patimati.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3Config {

    /**
     * S3 ve MinIO kimlik bilgisi sağlayıcısı (Credentials Provider).
     * 
     * Local MinIO (Development):
     *   `accessKey` ve `secretKey` tanımlıysa StaticCredentialsProvider kullanılır.
     * 
     * AWS S3 (Production):
     *   Canlı ortama geçildiğinde ortam değişkenleri, IAM Rolleri veya DefaultCredentialsProvider kullanılır.
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(S3StorageProperties properties) {
        // Local MinIO (Development) static credentials
        if (StringUtils.hasText(properties.accessKey()) && StringUtils.hasText(properties.secretKey())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
            );
        }

        // AWS S3 (Production) - IAM Roles / Env Vars / AWS Profile
        // return DefaultCredentialsProvider.builder().build();
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    public S3Client s3Client(
            S3StorageProperties properties,
            AwsCredentialsProvider credentialsProvider
    ) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration(properties));

        URI endpoint = endpoint(properties);
        if (endpoint != null) {
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(
            S3StorageProperties properties,
            AwsCredentialsProvider credentialsProvider
    ) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration(properties));

        URI endpoint = endpoint(properties);
        if (endpoint != null) {
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    /**
     * Path-style adresleme kararının TEK yeri — hem istemci hem presigner buradan besleniyor.
     *
     * <p>Presigner'a path-style bilgisini geçirmenin başka yolu yok:
     * {@code S3Presigner.Builder} sınıfında {@code forcePathStyle} metodu
     * <b>bulunmuyor</b> (AWS SDK 2.49.3'te javap ile doğrulandı). Bu satır buradan
     * kaldırılıp yerine yalnız {@code S3ClientBuilder.forcePathStyle(...)} konursa
     * yükleme çalışmaya devam eder ama presigner sessizce virtual-host URL üretir:
     * {@code http://patimati-medya-kutusu.localhost:9000/...} — böyle bir alan adı
     * çözülmediği için hiçbir fotoğraf görüntülenemez ve AI servisi fotoğrafları
     * indiremez. Bekçisi: {@code S3PresignedUrlSekliTest}.
     */
    private S3Configuration serviceConfiguration(S3StorageProperties properties) {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(properties.pathStyleAccess())
                .build();
    }

    private URI endpoint(S3StorageProperties properties) {
        if (!StringUtils.hasText(properties.endpoint())) {
            return null;
        }

        try {
            return URI.create(properties.endpoint().trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "app.storage.s3.endpoint geçerli bir URI olmalıdır",
                    exception
            );
        }
    }
}
