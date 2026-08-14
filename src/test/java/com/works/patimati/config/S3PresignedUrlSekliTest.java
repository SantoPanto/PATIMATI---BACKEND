package com.works.patimati.config;

import com.works.patimati.storage.S3ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Presigned URL'in ŞEKLİNİ koruyan bekçi.
 *
 * <p>Neden var: 14.08'de {@code S3Config.serviceConfiguration()} içinden
 * {@code .pathStyleAccessEnabled(...)} silinip yerine yalnız
 * {@code S3ClientBuilder.forcePathStyle(...)} kondu. Yükleme çalışmaya devam etti,
 * ama presigner sessizce virtual-host URL üretmeye başladı
 * ({@code http://patimati-medya-kutusu.localhost:9000/...}) — çözülmeyen bir alan
 * adı. Bütün birim testleri yeşil kaldı çünkü mevcut
 * {@code S3ImageStorageServiceTest} presigner'ı <b>mock'luyor</b>.
 *
 * <p>Bu yüzden buradaki testler presigner'ı mock'lamaz: üretim fabrika metodu
 * {@code new S3Config().s3Presigner(...)} ile <b>gerçek</b> presigner kurulur ve
 * <b>gerçek</b> {@code S3ImageStorageService.createTemporaryReadUrl()} çağrılır.
 * Sınanan şey kaynak koddaki bir dizgi değil, kullanıcıya/AI servisine giden
 * adresin kendisidir. Ağ gerektirmez (SigV4 imzası yerelde hesaplanır).
 */
class S3PresignedUrlSekliTest {

    private static final String KOVA = "patimati-medya-kutusu";
    private static final String BOLGE = "eu-central-1";
    private static final String REFERANS = "s3://" + KOVA + "/ads/2026/08/kedi.jpg";

    /**
     * ⚠ İki adres birden sınanıyor ve bu ŞART: AWS SDK'nın uç kuralları, endpoint
     * host'u IP literali olduğunda path-style'a kendiliğinden geçiyor. Yani
     * {@code 127.0.0.1} ile gerileme GÖRÜNMEZ, yalnız {@code localhost} ile görünür.
     * Ölçüm (14.08, SDK 2.49.3): pathStyle verilmediğinde
     * {@code localhost} → {@code patimati-medya-kutusu.localhost} (bozuk),
     * {@code 127.0.0.1} → {@code 127.0.0.1} (tesadüfen doğru).
     * Tek adresle yazılmış bir bekçi bu gerilemeyi kaçırırdı.
     */
    @DisplayName("MinIO ucunda presigned URL path-style olmalı (kova host'ta değil yolda)")
    @ParameterizedTest(name = "endpoint={0}")
    @ValueSource(strings = {"http://localhost:9000", "http://127.0.0.1:9000"})
    void presignedUrlPathStyleOlmali(String endpoint) {
        String url = temporaryReadUrl(properties(endpoint, true));
        URI uri = URI.create(url);

        assertThat(uri.getHost())
                .as("kova adı host'a yapışmamalı — çözülemeyen alan adı üretir: %s", url)
                .doesNotContain(KOVA)
                .isEqualTo(URI.create(endpoint).getHost());

        assertThat(uri.getPath())
                .as("kova adı yolda olmalı: %s", url)
                .startsWith("/" + KOVA + "/")
                .endsWith("/ads/2026/08/kedi.jpg");

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getPort()).isEqualTo(9000);

        assertThat(url)
                .as("gerçekten imzalanmış olmalı, elle birleştirilmiş dize değil")
                .contains("X-Amz-Signature")
                .contains("X-Amz-Expires=900");
    }

    /**
     * Aşırı-düzeltme bekçisi: path-style'ı koşulsuz açmak gerçek AWS'de de eski
     * stile düşürürdü ({@code https://s3.eu-central-1.amazonaws.com/kova/...}).
     * Ayar property'den okunmalı; endpoint yokken varsayılan virtual-host kalmalı.
     */
    @Test
    @DisplayName("Gerçek AWS ucunda (endpoint yok) virtual-host şekli korunmalı")
    void gercekAwsUcuVirtualHostKalmali() {
        String url = temporaryReadUrl(properties(null, false));
        URI uri = URI.create(url);

        assertThat(uri.getHost()).isEqualTo(KOVA + ".s3." + BOLGE + ".amazonaws.com");
        assertThat(uri.getPath()).isEqualTo("/ads/2026/08/kedi.jpg");
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(url).contains("X-Amz-Signature");
    }

    private String temporaryReadUrl(S3StorageProperties properties) {
        S3Config config = new S3Config();
        AwsCredentialsProvider credentials = config.awsCredentialsProvider(properties);
        S3Presigner presigner = config.s3Presigner(properties, credentials);

        // s3Client presign yolunda kullanılmıyor; mock'lanacak tek şey o olabilirdi,
        // presigner DEĞİL — bu testin varlık sebebi tam olarak bu ayrım.
        return new S3ImageStorageService(null, presigner, properties)
                .createTemporaryReadUrl(REFERANS);
    }

    private S3StorageProperties properties(String endpoint, boolean pathStyleAccess) {
        return new S3StorageProperties(
                KOVA,
                BOLGE,
                endpoint,
                pathStyleAccess,
                "minioadmin",
                "minioadmin",
                Duration.ofMinutes(15),
                3,
                DataSize.ofMegabytes(5)
        );
    }
}
