package com.works.patimati.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3Config {

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
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
