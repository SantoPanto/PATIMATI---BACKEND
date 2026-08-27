package com.works.patimati.instagram;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@code ImageStorageService.uploadImages(List<MultipartFile>)} yalnız
 * {@link MultipartFile} kabul ediyor -- sunucu tarafında üretilen (ör.
 * Instagram için en-boy oranı düzeltilmiş) bir görseli disk/ağdan gelen
 * gerçek bir yükleme olmadan aynı yoldan depoya yazabilmek için bellekteki
 * byte'ları sarmalar.
 */
public final class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content == null ? 0 : content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream out = new FileOutputStream(dest)) {
            out.write(content);
        }
    }
}
