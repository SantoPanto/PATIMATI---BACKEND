package com.works.patimati.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface ImageStorageService {

    /**
     * Görselleri yükler ve veritabanında saklanabilecek kalıcı referansları döndürür.
     */
    List<String> uploadImages(List<MultipartFile> images);

    /**
     * Kalıcı bir depolama referansı için geçici HTTP indirme adresi üretir.
     */
    String createTemporaryReadUrl(String storageReference);

    /**
     * Verilen kalıcı depolama referanslarına ait nesneleri siler.
     */
    void deleteImages(Collection<String> storageReferences);
}
