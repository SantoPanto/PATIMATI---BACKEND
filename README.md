# PATIM---BACKEND

## Fotoğraf deposu — yerelde MinIO, üretimde AWS S3

İlan fotoğrafları S3'te tutulur; veritabanında yalnız `s3://<kova>/<anahtar>`
referansı durur, dışarı verilirken 15 dakikalık imzalı URL üretilir.

MinIO S3 uyumludur, **kodda hiçbir değişiklik gerekmez** — yalnız profil açılır:

```
SPRING_PROFILES_ACTIVE=local
minio.exe server <veri-dizini> --address 127.0.0.1:9000
```

Kova adı `patimati-medya-kutusu` (MinIO konsolundan bir kez oluşturulur).
MinIO'yu `127.0.0.1`'e bağladıysan `S3_ENDPOINT=http://127.0.0.1:9000` ver.

Profil verilmezse uygulama **gerçek AWS S3**'e bakar; kimlik bilgisi
`DefaultCredentialsProvider` ile gelir (IAM rolü / `AWS_ACCESS_KEY_ID`).

⚠ MinIO'da imzalı URL'ler **path-style** üretilmeli
(`http://localhost:9000/<kova>/...`). Virtual-host şekli (`<kova>.localhost`)
çözülmez ⇒ fotoğraflar görüntülenemez ve AI servisi indiremez.
Bekçisi: `S3PresignedUrlSekliTest`.
