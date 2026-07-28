package com.works.patimati.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.storage.s3")
public record S3StorageProperties(
        @NotBlank
        String bucket,

        @NotBlank
        String region,

        String endpoint,

        boolean pathStyleAccess,

        @NotNull
        Duration presignedUrlDuration,

        @Min(1)
        int maxFileCount,

        @NotNull
        @DataSizeUnit(DataUnit.MEGABYTES)
        DataSize maxFileSize
) {

    private static final Duration MINIMUM_PRESIGNED_URL_DURATION =
            Duration.ofMinutes(10);

    @AssertTrue(message = "Presigned URL duration must be at least 10 minutes")
    public boolean isPresignedUrlDurationValid() {
        return presignedUrlDuration != null
                && !presignedUrlDuration.minus(MINIMUM_PRESIGNED_URL_DURATION).isNegative();
    }

    @AssertTrue(message = "Maximum file size must be greater than zero")
    public boolean isMaxFileSizeValid() {
        return maxFileSize != null && maxFileSize.toBytes() > 0;
    }
}
