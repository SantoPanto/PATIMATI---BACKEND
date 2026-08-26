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
     * {@link #uploadImages(List)} ile aynı, yalnızca S3 nesne anahtarının
     * ön ekini seçmeye izin verir (varsayılan {@code "ads"}). EKLENDİ — Faz 2
     * revize blueprint §7: Instagram medyası {@code "external"} önekiyle
     * saklanır, aynı doğrulama/geri alma (rollback) mantığı üzerinden.
     * Var olan {@code uploadImages(List)} çağıranları için davranış DEĞİŞMEZ.
     */
    List<String> uploadImages(List<MultipartFile> images, String keyPrefix);

    /**
     * Kalıcı bir depolama referansı için geçici HTTP indirme adresi üretir.
     */
    String createTemporaryReadUrl(String storageReference);

    /**
     * Kalıcı depolama referansındaki görseli uygulama içinde kullanılmak üzere okur.
     *
     * <p>Bu metot özellikle PDF afiş gibi sunucu tarafında görsel üretilen
     * akışlar içindir. Dışarıdan verilen rastgele bir URL'e istek atmaz; nesneyi
     * doğrudan uygulamanın yapılandırılmış MinIO/R2 alanından getirir.</p>
     */
    byte[] readImage(String storageReference);

    /**
     * Verilen kalıcı depolama referanslarına ait nesneleri siler.
     */
    void deleteImages(Collection<String> storageReferences);
}
