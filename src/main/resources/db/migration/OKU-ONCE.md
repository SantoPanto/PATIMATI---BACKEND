# Migration kuralları — kısa ve bağlayıcı

## 1. Uygulanmış bir migration dosyası ASLA değiştirilmez

Flyway her dosyanın bir sağlama (checksum) değerini `flyway_schema_history`
tablosunda tutar. Dosya sonradan değişirse — bir boşluk bile — uygulama
açılışta şununla durur:

```
Migration checksum mismatch for migration version 1
-> Applied to database : 863440007
-> Resolved locally    : -738123801
```

Bu **bizde yaşandı**: `V1__init_schema.sql`'e `users.created_at` sonradan
eklendi (`b3daf7d`). Sonuç: veritabanı zaten kurulu olan **herkeste backend
hiç açılmadı**. Yeni kurulumda görünmedi, çünkü orada V1 ilk kez uygulanıyor.

**Yeni bir alan gerekiyorsa yeni bir dosya açılır** (`V11__...`), eskisi
elletilmez.

### Backend'iniz bu hatayla açılmıyorsa

Bir kez `repair` çalıştırmanız yeterli — geçmişteki sağlamayı dosyanın
bugünkü hâline eşitler, veriye dokunmaz:

```
./mvnw flyway:repair \
  -Dflyway.url=jdbc:postgresql://localhost:5432/patimati_db \
  -Dflyway.user=postgres -Dflyway.password=<sifre>
```

`V1` bilerek geri alınmadı: değişiklik `main`'de bir süredir duruyor ve
07.08'den sonra kurulan veritabanları artık YENİ sağlamaya sahip. Dosyayı
eski hâline döndürmek bu sefer onları kırardı. Doğru olan, V1'i olduğu gibi
bırakıp bir defalık `repair` yapmak.

## 2. Entity'ye alan eklemek migration YERİNE GEÇMEZ

`ddl-auto: update` bir güvenlik ağı değil. Boş olmayan bir tabloya `NOT NULL`
sütun **ekleyemez** ve bunu yalnızca WARN seviyesinde söyleyip geçer:

```
Error executing DDL "alter table if exists ads add column suspended boolean
not null" -> ERROR: column "suspended" of relation "ads" contains null values
```

Sütun oluşmaz, uygulama açılır, sonra o sütunu okuyan **her sorgu 500 döner**.
`ads.suspended` tam olarak böyle oldu ve ilan listeleyen bütün uçları kırdı
(`V10__add_suspended_to_ads.sql` bunu kapatıyor).

Kural: **entity'ye eklenen her yeni sütun için aynı PR'da bir migration.**

## 3. Yeni sütun NOT NULL ise DEFAULT ver

Mevcut satırlar o değeri bir yerden almalı. `DEFAULT` yoksa migration dolu
bir tabloda çalışmaz.

## 4. `@Builder.Default` null geçilirse EZİLİR

Entity'de varsayılan tanımlı olması yetmez:

```java
private CoatPattern coatPattern = CoatPattern.UNKNOWN;  // @Builder.Default
```

`.coatPattern(null)` çağırmak bu varsayılanı **null yapar**. İsteğe bağlı bir
alan builder'a doğrudan verilecekse null kontrolü şart:

```java
.coatPattern(request.coatPattern() != null ? request.coatPattern() : CoatPattern.UNKNOWN)
```

Aksi hâlde insert `null value in column "coat_pattern" violates not-null
constraint` ile düşer. Sahiplendirme ilanı oluşturma ucu tam bu yüzden hiç
çalışmıyordu.
