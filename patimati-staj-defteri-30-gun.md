# PATİMATİ PROJESİ STAJ DEFTERİ (30 GÜN)

## 1. Gün

Staj sürecinin ilk gününde, kurum binasının ve çalışma alanlarının detaylı bir fiziksel tanıtımı yapıldı. Kurum kültürünü, genel işleyişi ve projelerin yönetim süreçlerini kavramamız amacıyla kapsamlı bir oryantasyon eğitimi gerçekleştirildi. Bu sürecin ardından, stajyer öğrencilerin yetkinlikleri ve takım çalışmasına yatkınlıkları göz önünde bulundurularak 10'ar kişilik yazılım geliştirme ekipleri oluşturuldu. Ekip üyeleriyle tanışma toplantısı düzenlenerek, önümüzdeki haftalarda yürütülecek ortak çalışma dinamiğinin temelleri atıldı.

## 2. Gün

Oluşturulan ekiplerden, staj süresince uçtan uca geliştirip teslim edecekleri projelerin fikir aşamasını başlatmaları istendi. Fikirlerin profesyonel bir yazılım yaşam döngüsüne oturtulması için Ürün Gereksinim Belgesi (PRD - Product Requirements Document) oluşturma ve analiz eğitimi verildi. Ek olarak, versiyon kontrol sistemlerinin takım çalışmasındaki kritik rolü nedeniyle Git ve GitHub üzerinde temel ve ileri seviye (branching, merging, conflict resolution) uygulamalı eğitimler düzenlendi. Yazılım projelerinin sürdürülebilirliğini doğrudan etkileyen "teknik borç" (technical debt) kavramı üzerine bilgilendirmeler yapılarak temiz kod (clean code) pratikleri aktarıldı.

## 3. Gün

Gerçekleştirilen ekip içi beyin fırtınası seansları sonucunda, üzerinde çalışacağımız evcil hayvan sahiplendirme ve kayıp hayvan takip platformu olan "PatiMati" projesinin temel hatları netleştirildi. Projenin kapsamını, hedef kitlesini ve temel özelliklerini barındıran PRD ve "one-pager" (özet) dokümanları titizlikle hazırlandı. Sistem mimarisini planlayarak; arka uç (backend) geliştirme için Spring Boot, veritabanı yönetimi için PostgreSQL, mesaj kuyruğu sistemi için RabbitMQ ve ön uç (frontend) için React gibi modern teknolojilerin kullanılmasına karar verdim. Ayrıca sisteme Google OAuth2 ve JWT tabanlı güvenlik entegrasyonlarını dahil ettim. Proje gereksinimlerine göre grup içi iş paylaşımı yapılarak görevleri modüllere böldüm ve ekibin sorumlu olacağı servisleri belirledim. İlk hafta tamamlanması gereken görevleri atadıktan sonra, benzer platformların pazar ve saha analizlerini ekibimle birlikte gerçekleştirerek kodlama aşamasına hazırlık yaptık.

## 4. Gün (2026-07-21 — 2026-07-28)

Backend projesinde `README.md` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Frontend projesinde `README.md` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Servis katmanı doğrulamaları için `PatiMatiApplicationTests.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `README.md` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

AI mikroservisi kurulum talimatlarını ve çalışma ortamı gereksinimlerini belirten `README.md` dosyası güncellendi.

Güvenlik katmanında `pom.xml`, `SecurityConfig.java`, `AuthController.java` ve 9 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Geliştirme dalı (pull request #1) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `SecurityConfig.java`, `AuthController.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #2) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitattributes`, `.gitignore`, `maven-wrapper.properties` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Güvenlik katmanında `SecurityConfig.java`, `AuthController.java`, `GoogleAuthRequest.java` ve 4 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Kullanıcı profil ve yetkilendirme modülünde `pom.xml`, `AuthController.java`, `UserController.java` ve 1 ek dosya dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `AdController.java`, `Ad.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #5) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `SecurityConfig.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #6) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Ad.java`, `AgeGroup.java`, `CoatPattern.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Güvenlik katmanında `pom.xml`, `SecurityConfig.java`, `ValidationExceptionHandler.java` ve 4 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Geliştirme dalı (pull request #3) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `SecurityConfig.java` ve 25 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #7) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `S3Config.java`, `S3StorageProperties.java` ve 18 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `0af10de1`, `92f47bdf`, `f77eb4d9`, `d77dcb0a`, `d2e1eb21`, `1f283e81`, `59c6c78b`, `f6b3a80c`, `d9d9c6ff`, `ebb259db`, `2a7ca0b0`, `4873cd8e`, `d6f4bb1a`, `7fbabfa8`, `c285bfe4`, `f0e2c0c9`, `9b319114`, `8ccc196c`, `28c6820a`

## 5. Gün (2026-07-28 — 2026-08-04)

Veritabanı şema versiyonlaması için Flyway migration betiği (`V4__create_password_reset_tokens_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #8) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `S3Config.java` ve 45 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Şikayet işlemleri kapsamında `pom.xml`, `ComplaintController.java`, `ComplaintRequest.java` ve 6 ek dosya sınıfları üzerinde değişiklik yapıldı. İlan ve kullanıcı şikayet kayıtlarının işlenmesi ve veri modelinin düzenlenmesi sağlandı.

Geliştirme dalı (pull request #10) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `ComplaintController.java`, `ComplaintRequest.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Güvenlik katmanında `SecurityConfig.java`, `ValidationExceptionHandler.java` sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Kullanıcı profil ve yetkilendirme modülünde `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Geliştirme dalı (pull request #11) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `ValidationExceptionHandler.java`, `UserService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `OAuth2LoginSuccessHandler.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #12) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `OAuth2LoginSuccessHandler.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #16) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #17) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `WebSocketProperties.java`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #20) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `WebSocketConfig.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `ComplaintControllerTest.java`, `ComplaintReasonTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Servis katmanı doğrulamaları için `ComplaintControllerTest.java`, `ComplaintServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Güvenlik katmanında `SecurityConfig.java`, `AdController.java`, `Ad.java` ve 2 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 20 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #22) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `ComplaintController.java` ve 14 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `7a796ec9`, `69464c50`, `b56b71ce`, `5f996c0d`, `516fc4c8`, `ccd85351`, `450468e6`, `1d0fa5ce`, `9edb13aa`, `57a51f30`, `5d705179`, `6de16124`, `0b0fc40e`, `7eef6053`, `bfb2aa51`, `54ca2741`, `80984d67`, `6db493dc`, `de428912`

## 6. Gün (2026-08-04 — 2026-08-07)

Veritabanı şema versiyonlaması için Flyway migration betiği (`V7__create_decoupled_complaint_tables.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (branch 'jwt') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 20 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ComplaintController.java`, `AdComplaint.java`, `Complaint.java` ve 13 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Kullanıcı profil ve yetkilendirme modülünde `User.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Geliştirme dalı (pull request #21) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Message.java`, `User.java`, `MessageRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Message.java`, `User.java`, `MessageRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V6__create_messages_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #25) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiRabbitConfig.java`, `SecurityConfig.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 70 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `UserRepository.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Backend projesinde `UserRepository.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 85 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisPublisher.java`, `AdRepository.java`, `UserRepository.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `UserRepository.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #29) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `all_codes.txt`, `PatiMatiApplication.java`, `AdController.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #30) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageResponse.java`, `MessageRepository.java`, `MessageService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #31) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V1__init_schema.sql`, `V4__create_password_reset_tokens_table.sql`, `V7__create_decoupled_complaint_tables.sql` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #32) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiRabbitConfig.java`, `AiJsonMessageConverterTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `84b4edab`, `375a63d0`, `78aa7d36`, `99980eb3`, `910f365c`, `2e9b0d64`, `b59ea8eb`, `125c5e58`, `978c4c7f`, `013d1d2f`, `eb85de8f`, `12d2f2c0`, `c5b916a5`, `ded1ad01`, `879bd518`, `f71eb1a1`, `6ec71f17`, `a6da1b62`, `56abe460`

## 7. Gün (2026-08-07 — 2026-08-11)

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Backend projesinde `AdminController.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #33) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisResult.java`, `AdResponse.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `AdminController.java`, `AdoptionComplaintController.java`, `AdoptionController.java` ve 11 ek dosya üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #34) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdminController.java`, `AdoptionComplaintController.java` ve 23 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #35) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisResult.java`, `AdResponse.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Kullanıcı profil ve yetkilendirme modülünde `AuthResponse.java`, `SafeUserDTO.java`, `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Geliştirme dalı (pull request #37) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AuthResponse.java`, `SafeUserDTO.java`, `UserService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Kullanıcı profil ve yetkilendirme modülünde `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Geliştirme dalı (pull request #36) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `all_codes.txt`, `PatiMatiApplication.java`, `AiAnalysisListener.java` ve 51 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V9__add_reward_points_to_users.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #38) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdoptionController.java`, `SafeUserDTO.java` ve 10 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #2) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `ChatPage.tsx`, `ListingsPage.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #3) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Güvenlik katmanında `SecurityConfig.java`, `UserController.java`, `UpdateProfileRequest.java` ve 1 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Kullanıcı profil ve yetkilendirme modülünde `AuthResponse.java`, `RegisterRequest.java`, `SafeUserDTO.java` ve 3 ek dosya dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Kullanıcı profil ve yetkilendirme modülünde `UpdateProfileRequest.java`, `UserResponseDTO.java`, `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Kullanıcı profil ve yetkilendirme modülünde `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Frontend projesinde `package-lock.json`, `package.json`, `App.tsx` ve 15 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.


*Geliştirme Commitleri:* `b3daf7dd`, `bffd3e99`, `e2e4be28`, `348eed06`, `6b55250f`, `23e23731`, `3f4e03f5`, `ca2d6d93`, `65bdb575`, `eb0fb5d4`, `c2c60b00`, `e1decf9f`, `69c0fe18`, `3868be25`, `459e07dd`, `1e0c7514`, `ff309bfd`, `dd4051f2`, `89211b40`

## 8. Gün (2026-08-11 — 2026-08-14)

İlk hafta boyunca geliştirilen modüllerin GitHub üzerinden versiyon kontrolü ve kod birleştirmeleri (merge) yapıldı. Projenin güncel ilerleme durumuna göre takım arkadaşlarıma yeni görev atamaları gerçekleştirildi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #4) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `TeamUI.tsx`, `AdoptionCreatePage.tsx`, `LoginRedirectPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #5) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #6) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.gitignore`, `AdminComplaintsPage.tsx` ve 73 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #7) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #8) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `sayfa-denetcisi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #39) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `OKU-ONCE.md`, `V10__add_suspended_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `OKU-ONCE.md`, `V10__add_suspended_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Kullanıcı profil ve yetkilendirme modülünde `UserController.java`, `FcmTokenUpdateDTO.java`, `UserService.java` dosyaları düzenlendi. Kullanıcı verilerinin sorgulanması ve yanıt DTO alanları yapılandırıldı.

Geliştirme dalı (pull request #41) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `UserController.java`, `AuthResponse.java` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `.gitignore` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Frontend projesinde `App.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #10) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #11) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (pull request #44) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdServiceTest.java`, `S3ImageStorageServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #45) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitignore`, `pom.xml`, `FirebaseConfig.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.


*Geliştirme Commitleri:* `f3be184c`, `d7d8f41a`, `d589a0d9`, `22ebbc54`, `e39d9702`, `ad833758`, `ef064149`, `31e36c3c`, `04a3c4d3`, `c27e5219`, `fa9055bb`, `c35f92e6`, `4b411c22`, `d0800926`, `f4cf988a`, `415ebdb2`, `eed57ab6`, `a6b9da5b`, `4a81a41b`

## 9. Gün (2026-08-14 — 2026-08-17)

Servis katmanı doğrulamaları için `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #46) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `UserController.java`, `AdUpdateRequest.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `App.tsx`, `PetDetailPage.tsx`, `listingpage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'develop') ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (pull request #12) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `PetDetailPage.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #47) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `S3Config.java`, `S3PresignedUrlSekliTest.java`, `S3ImageStorageServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #48) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `application-local.yml`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdminServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Anlık mesajlaşma modülünde `index.html`, `firebase-messaging-sw.js`, `ComplaintModal.tsx` ve 11 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #13) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `firebase-messaging-sw.js`, `ComplaintModal.tsx` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #49) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminServiceImpl.java`, `AdminServiceImplTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #15) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `ComplaintPage.tsx`, `ListingsPage.tsx` ve 33 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `.gitattributes`, `ci.yml` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `kuyruk.py`, `entegrasyon-sozlesmesi.md` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #9) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `olcum-raporu.md`, `eslesme_yok_testi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `AdoptionAdCreateRequest.java`, `application.yml` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #56) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionAdCreateRequest.java`, `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık iletişim altyapısında `ChatRoomResponseDTO.java`, `MessageResponse.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya sınıfları güncellendi. STOMP istemci bağlantı noktaları ve canlı mesaj kanalı güvenlik denetimleri düzenlendi.

Geliştirme dalı (pull request #57) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatRoomResponseDTO.java`, `MessageResponse.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `3d228a08`, `e751ff3a`, `9ecf9edf`, `313de6c0`, `de80e4f6`, `8680f383`, `c780c819`, `fe8392fc`, `ebfcf8b0`, `a90e0907`, `aca2986f`, `ced9e43b`, `14eff144`, `0b8a8640`, `3003e3fe`, `e61bc68e`, `e117bb61`, `fcf495a8`, `6faf148e`

## 10. Gün (2026-08-17 — 2026-08-18)

Frontend projesinde `eslint.config.js`, `package-lock.json`, `useChatWebSocket.ts` ve 9 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

Geliştirme dalı (pull request #58) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdRepository.java`, `AiAdayAskiyaAlmaTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık iletişim altyapısında `ChatController.java`, `MessageRepository.java`, `WebSocketChannelInterceptor.java` ve 1 ek dosya sınıfları güncellendi. STOMP istemci bağlantı noktaları ve canlı mesaj kanalı güvenlik denetimleri düzenlendi.

Geliştirme dalı (pull request #59) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatController.java`, `MessageRepository.java`, `WebSocketChannelInterceptor.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #60) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiMatchNotifier.java`, `FirebasePushNotificationService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `useChatWebSocket.ts`, `AdoptionDetailPage.tsx`, `ChatPage.tsx` ve 3 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #17) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `eslint.config.js`, `package-lock.json`, `useChatWebSocket.ts` ve 10 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `.gitignore`, `application.yml` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Güvenlik katmanında `PasswordEncoderConfig.java`, `SecurityConfig.java`, `OAuth2AuthenticationSuccessHandler.java` ve 3 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Anlık mesajlaşma modülünde `App.tsx`, `ReportUserModal.tsx`, `ChatPage.tsx` ve 4 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ReportUserModal.tsx`, `ChatPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #61) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitignore`, `PasswordEncoderConfig.java`, `SecurityConfig.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'main') ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `eslint.config.js`, `package-lock.json`, `App.tsx` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #16) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.tsx`, `posters.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V11__create_ad_matches_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Kimlik doğrulama katmanında `App.tsx`, `MatchCard.tsx`, `LoginPage.tsx` ve 4 ek dosya bileşenleri güncellendi. Token saklama ve giriş/kayıt form akışları düzenlendi.

Geliştirme dalı (pull request #50) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicAdController.java`, `AdRepository.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #20) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `admin.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `ced306b8`, `fbb1fdc9`, `fc0bca2e`, `b879ed0d`, `d41a1ed6`, `a4ec8eb2`, `12e3e5ff`, `9a46eec8`, `b54f0e19`, `ffccea89`, `a2399d5f`, `33f40a2e`, `6c460656`, `8e8b0e87`, `3dbaeec1`, `f434bd42`, `a084b0ac`, `9867d026`, `1b8882a2`

## 11. Gün (2026-08-18 — 2026-08-19)

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `notifications.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #66) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (pull request #21) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `MatchCard.tsx`, `LoginPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V11__create_ad_matches_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #67) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchController.java`, `AdMatchResponseDTO.java`, `AdMatchSaveRequest.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #68) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdControllerTest.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #70) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalyzeController.java`, `AiMatchService.java`, `application.yml` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #69) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiMatchNotifier.java`, `AiAnalysisResult.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `.gitignore`, `AiAnalysisListener.java` ve 45 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #63) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `MessageController.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #22) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `pom.xml`, `S3Config.java`, `MinioImageStorageServiceImpl.java` ve 3 ek dosya üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Frontend projesinde `package-lock.json`, `AddListingPage.tsx`, `firbase.ts` ve 1 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `AddListingPage.tsx`, `firbase.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `package-lock.json`, `AdminComplaintsPage.tsx` dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `AdminComplaintsPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Projenin Maven yapılandırma dosyası (`pom.xml`) güncellendi. Bağımlılık kütüphanelerinin sürüm tanımları ve derleme eklentileri düzenlendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `AiAnalysisListener.java`, `AiMatchNotifier.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 29 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `2e543c79`, `56260409`, `1695e8ed`, `b49751f8`, `8007871b`, `aebb1452`, `ba537fdc`, `c8faf715`, `a80918ad`, `3e16ec72`, `11f1f21f`, `d85c0ec5`, `4eb74aab`, `f225dcca`, `2cd8b63a`, `ff5f3b72`, `2c8afb23`, `f47e7509`, `9dc58683`

## 12. Gün (2026-08-19)

Geliştirme dalı (pull request #72) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `S3Config.java`, `MinioImageStorageServiceImpl.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 21 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan modülünde `AdService.java` dosyaları güncellendi. İlan oluşturma, arama parametreleri ve DTO veri aktarım dönüşümleri düzenlendi.

Geliştirme dalı (pull request #73) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan modülünde `AdService.java` dosyaları güncellendi. İlan oluşturma, arama parametreleri ve DTO veri aktarım dönüşümleri düzenlendi.

Geliştirme dalı (pull request #74) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Yapay zeka servisinde `.env.example` üzerinde teknik düzenlemeler yapıldı.

Backend projesinde `application.yml` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #76) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V12__create_adoption_complaints_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #77) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `application-prod.yml`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Kimlik doğrulama katmanında `.env`, `.env.development`, `.env.production` ve 5 ek dosya bileşenleri güncellendi. Token saklama ve giriş/kayıt form akışları düzenlendi.

Geliştirme dalı (pull request #27) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.env.development`, `.env.production` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`...ion_complaints_table.sql  V13__create_adoption_complaints_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #78) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `...ion_complaints_table.sql  V13__create_adoption_complaints_table.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `application.yml` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #79) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Konum servisi arayüzünde `MatchCard.tsx`, `AddListingPage.tsx`, `MapPage.tsx` ve 4 ek dosya bileşenleri güncellendi. Harita üzeri işaretçi ve konum parametreleri düzenlendi.

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `AddListingPage.tsx`, `MapPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `bfbfead5`, `731f7182`, `e18f47db`, `488d9272`, `9816cc9b`, `2409c127`, `60c9e3b4`, `c4976674`, `4ed36c69`, `2ea05625`, `d26ef957`, `cc6ff6bb`, `79817861`, `8815a922`, `02e5a2fd`, `62c26d4d`, `d0f58797`, `a531d61b`, `604249a5`

## 13. Gün (2026-08-19 — 2026-08-20)

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `HomePage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `imageUrl.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #29) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageUrl.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #30) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `HomePage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `FavoritesPage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #31) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #80) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `UserController.java`, `GoogleAuthRequest.java`, `UserService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #81) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `goc-numarasi-denetimi.py`, `GocNumarasiCakismasiTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #32) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AddListingPage.tsx`, `AiMatchPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `MessageControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #84) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageController.java`, `MessageControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Güvenlik katmanında `pom.xml`, `OpenApiConfig.java`, `SecurityConfig.java` ve 2 ek dosya sınıfları düzenlendi. Spring Security filtre zinciri, JWT doğrulama kuralları ve uç nokta erişim izinleri yapılandırıldı.

Geliştirme dalı (pull request #85) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `OpenApiConfig.java`, `SecurityConfig.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `SwaggerDocsIntegrationTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #86) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `application.yml`, `SwaggerDocsIntegrationTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #83) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdController.java`, `FavoriteAd.java`, `FavoriteAdRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #87) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdRepository.java`, `MessageService.java`, `MesajOdasiIliskiIstiyorTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `generate-firebase-sw.mjs`, `eslint.config.js`, `favicon.svg` ve 8 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

Geliştirme dalı (pull request #47) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.


*Geliştirme Commitleri:* `d126b4d8`, `9ef2462c`, `9d8ccdc3`, `e4b62e07`, `62a1efc4`, `085194f2`, `1bdb2954`, `d312d541`, `b6cad449`, `bdca7653`, `0b2033fe`, `bef656c8`, `3d9de7be`, `dbfaf1bb`, `1c34cda2`, `744ddb3c`, `2a14b1df`, `2ae84600`, `fc2caf07`

## 14. Gün (2026-08-20)

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AddListingPage.tsx`, `AiMatchPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #48) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `eslint.config.js`, `favicon.svg` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #33) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.env.development`, `.env.production` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #34) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #35) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoritesPage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #36) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `LoginPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #37) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #40) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `ReportUserModal.tsx`, `AdoptionCreatePage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #41) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `RequireAuth.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #42) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionDetailPage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #43) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `AdminDashboardPage.tsx`, `admin.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #45) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.test.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #46) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `ReportUserModal.tsx`, `AdoptionCreatePage.tsx` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `Dockerfile`, `README.md` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `Dockerfile`, `README.md` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `README.md`, `main.py` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #25) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `analiz.py`, `attributes.py`, `hayvansiz_olcum.py` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #88) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdRepository.java`, `AdService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #38) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `dcb29758`, `f4b61c07`, `6a7bd65d`, `b8524249`, `6919cadc`, `50dff0fb`, `2fdbb52a`, `6d824ec4`, `3282060d`, `02055f23`, `03f3df97`, `08d79793`, `28678780`, `d8b8b368`, `3af3884e`, `7c8d5812`, `33cb7117`, `74f64c0e`, `50524255`

## 15. Gün (2026-08-20)

Projenin ilerleyişi ve mevcut planlama doğrultusunda takım arkadaşlarıma yeni geliştirme görevlerinin ataması yapıldı ve sürecin genel takibi sağlandı.

Geliştirme dalı (pull request #89) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageService.java`, `MesajOdasiIliskiIstiyorTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #90) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdMatchRepository.java`, `EslesmeListelemeTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #91) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdMapper.java`, `IlanDuzenlemeMikrocipTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #92) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `enum-aynasi.json`, `enum-aynasi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `matcher.py`, `...CatIndividualImages_avito-siglip2_120birey.json` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdoptionControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #93) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionAdCreateRequest.java`, `AdoptionAdUpdateRequest.java`, `GlobalExceptionHandler.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AiStatusTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #94) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `AiStatus.java`, `AiStatusTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `CorsConfigTest.java`, `AdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #95) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java`, `CorsConfigTest.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #49) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChangePasswordPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdControllerTest.java`, `AdoptionComplaintControllerTest.java`, `PublicAdControllerTest.java` ve 2 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Yönetim paneli kapsamında `AdminComplaintsPage.tsx`, `ChangePasswordPage.tsx`, `ads.ts` ve 4 ek dosya bileşenleri güncellendi. Şikayet takibi ve yönetim aksiyon butonları düzenlendi.

Geliştirme dalı (pull request #50) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `ChangePasswordPage.tsx`, `ads.ts` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #97) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `AdoptionComplaintController.java` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `AdoptionComplaintController.java` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #96) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `UserController.java`, `ChangePasswordRequest.java`, `UserService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V15__add_poster_privacy_settings.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.


*Geliştirme Commitleri:* `b425faf0`, `d26217d2`, `238a3ba7`, `4e0709f4`, `6f50363f`, `27dc47e8`, `18ac9ea2`, `333be27d`, `23fee4af`, `216befb4`, `ef2e420c`, `2d542d33`, `66f6d22a`, `aa68724d`, `009bb199`, `2e8ec597`, `72e3ceca`, `d2134348`, `036b1b68`

## 16. Gün (2026-08-20)

Geliştirme dalı (pull request #98) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `PublicAdController.java`, `UserController.java` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `PosterSettingsModal.tsx`, `ChangePasswordPage.tsx`, `MyListingsPage.tsx` ve 4 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #51) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterSettingsModal.tsx`, `ChangePasswordPage.tsx`, `MyListingsPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdServiceTest.java`, `PosterServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #100) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BusinessException.java`, `GlobalExceptionHandler.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #99) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdResponse.java`, `AdMapper.java`, `AdControllerTest.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #52) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #53) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.tsx`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdEditModal.tsx`, `PosterSettingsForm.tsx`, `PosterSettingsModal.tsx` ve 4 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #54) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `PosterSettingsForm.tsx`, `PosterSettingsModal.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdControllerTest.java`, `AdoptionControllerTest.java`, `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #102) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdoptionAdCreateRequest.java`, `AdoptionServiceImpl.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdEditModal.tsx`, `AdoptionCreatePage.tsx`, `PetDetailPage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #55) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `AdoptionCreatePage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdoptionControllerTest.java`, `PosterServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #103) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdoptionAdCreateRequest.java`, `GlobalExceptionHandler.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #56) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdoptionControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.


*Geliştirme Commitleri:* `942d9702`, `16160176`, `b70841dd`, `91b196c3`, `951191a4`, `4895b985`, `2e742a6c`, `98dc9bf4`, `04820ced`, `7e90a21d`, `e5beb1a4`, `9b5c9fcf`, `176fcd60`, `07b0932e`, `f4d899d6`, `153ca657`, `7c5793f9`, `3ea11dfb`, `3727ba2d`

## 17. Gün (2026-08-20)

Geliştirme dalı (pull request #104) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterServiceImpl.java`, `application.yml`, `Roboto-Bold.ttf` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #57) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #58) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `JacksonConfig.java`, `AdCreateRequest.java`, `AdResponse.java` ve 2 ek dosya üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #106) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `JacksonConfig.java`, `AdCreateRequest.java`, `AdResponse.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `pom.xml`, `JacksonConfig.java`, `AdCreateRequest.java` ve 1 ek dosya üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #107) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `JacksonConfig.java`, `AdCreateRequest.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdoptionControllerTest.java`, `AdMapperTest.java`, `IlanDuzenlemeMikrocipTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #108) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdUpdateRequest.java`, `AdoptionAdCreateRequest.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #109) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdResponse.java`, `AdMapper.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx`, `errorMessage.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #61) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx`, `errorMessage.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `adoptions.ts`, `ads.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Frontend projesinde `authStorage.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #62) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `adoptions.ts`, `ads.ts`, `authStorage.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.


*Geliştirme Commitleri:* `55d19af6`, `288c9806`, `b83f154e`, `02bfe10d`, `592e1c43`, `bc57f7db`, `cee234ed`, `ab6dd065`, `77bf312d`, `49636387`, `dcf05990`, `c229bede`, `9342c671`, `a316c54a`, `e8115b0a`, `61d80269`, `3115dc2a`, `89d0cdcc`, `0509b7c8`

## 18. Gün (2026-08-20 — 2026-08-21)

Geliştirme dalı (pull request #110) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `GlobalExceptionHandler.java`, `JwtAuthFilter.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #59) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveFoundModal.test.tsx`, `ResolveFoundModal.tsx`, `MyListingsPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdEditModal.tsx`, `AddListingPage.tsx`, `AdoptionCreatePage.tsx` ve 3 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #66) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `AddListingPage.tsx`, `AdoptionCreatePage.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #67) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `ads.tarih.test.ts`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #75) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchNotifier.java`, `NotificationController.java`, `NotificationResponse.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #39) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #63) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AboutPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #64) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RegisterPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #65) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Contact.tsx`, `HomePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #68) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.test.tsx`, `MyListingsPage.tsx`, `types.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `S3ImageStorageServiceImpl.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #114) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `S3ImageStorageServiceImpl.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #105) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveLostAdRequest.java`, `Ad.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #111) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RegisterRequest.java`, `RegisterRequestValidationTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdServiceTest.java`, `IlanYenidenYayinlamaTest.java`, `KayipIlanCozumBagiTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchNotifier.java`, `NotificationController.java`, `NotificationResponse.java` ve 22 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #115) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FirebasePushNotificationService.java`, `NotificationService.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #116) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.


*Geliştirme Commitleri:* `e0ec3e51`, `4176754f`, `a1abbb0f`, `fc562094`, `4c25782d`, `fa5f6581`, `0e341949`, `c4677fa2`, `db7d7a8c`, `2e3af064`, `40ef2238`, `6708f44d`, `ad824ba8`, `00316268`, `fd1693ef`, `962b301a`, `7e5e7467`, `1a079e4e`, `cfd5d05a`

## 19. Gün (2026-08-21)

Servis katmanı doğrulamaları için `AdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #117) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `IlanYenidenYayinlamaTest.java`, `KayipIlanCozumBagiTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #118) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FirebasePushNotificationService.java`, `NotificationService.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Yapay zeka servisinde `.env.example`, `indirici.py`, `test_indirici.py` üzerinde teknik düzenlemeler yapıldı.

Geliştirme dalı (pull request #112) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdResponse.java`, `AdMapper.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdEditModal.tsx` ve 27 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #60) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Footer.tsx`, `AboutPage.tsx`, `Adoption.tsx` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AiMatchControllerTest.java`, `AiAnahtarBasligiTest.java`, `AiMatchServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #119) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchController.java`, `MatchedAdResponseDTO.java`, `AiMatchService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `MatchedAdCard.tsx`, `AddListingPage.tsx`, `AiMatchResultsPage.tsx` ve 1 ek dosya bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `attributes.py`, `tasma_dogruluk.py`, `tasma_etiketleri.json` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 40 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #69) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchedAdCard.tsx`, `AddListingPage.tsx`, `AiMatchResultsPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #70) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Frontend projesinde `AddListingPage.tsx`, `types.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #71) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 40 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #72) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `0bb1d681`, `b5c3af62`, `d475bafc`, `593cfe14`, `a769b4b8`, `a0cedb45`, `cb4c50a6`, `a3c47b06`, `dcc2070f`, `8f8b5f22`, `354cdcd6`, `2ebb4612`, `3f47da7b`, `5d398d2c`, `b59675c4`, `6aaba786`, `89b14b0e`, `04dc70d1`, `8452c27a`

## 20. Gün (2026-08-21)

İlan yönetimi arayüzünde `App.tsx`, `CreateAdLayout.tsx`, `AddListingPage.tsx` ve 2 ek dosya bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (pull request #73) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `CreateAdLayout.tsx`, `AddListingPage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #75) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `App.tsx`, `CreateAdLayout.tsx`, `TeamUI.tsx` ve 4 ek dosya bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (pull request #76) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `CreateAdLayout.tsx`, `TeamUI.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #74) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ScrollToTop.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Konum servisi arayüzünde `App.css`, `Header.tsx`, `MainLayout.tsx` ve 1 ek dosya bileşenleri güncellendi. Harita üzeri işaretçi ve konum parametreleri düzenlendi.

Geliştirme dalı (pull request #77) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `Header.tsx`, `MainLayout.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `Adoption.tsx`, `AdoptionDetailPage.tsx`, `HomePage.tsx` ve 1 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #78) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Adoption.tsx`, `AdoptionDetailPage.tsx`, `HomePage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Konum servisi arayüzünde `App.tsx`, `AdoptionDetailPage.tsx`, `MapPage.tsx` ve 2 ek dosya bileşenleri güncellendi. Harita üzeri işaretçi ve konum parametreleri düzenlendi.

Geliştirme dalı (pull request #79) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdoptionDetailPage.tsx`, `MapPage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `App.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #80) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `AdCard.tsx`, `FavoritesPage.tsx` bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (pull request #81) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #82) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `HomePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `AdCard.tsx`, `MatchedAdCard.tsx`, `FavoritesPage.tsx` bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.


*Geliştirme Commitleri:* `a6a82982`, `3d518a54`, `96632e2f`, `d28c777e`, `e7402549`, `2050172b`, `25bbaf1a`, `08fcd074`, `2c6de6f3`, `d207ded3`, `618d95e4`, `e62c0ad2`, `52acc231`, `68cf528c`, `2258dcf0`, `f4645051`, `5fc7c2b9`, `aa053ef6`, `f12b1215`

## 21. Gün (2026-08-21 — 2026-08-22)

Projenin canlı ortama taşınması (deployment) amacıyla Hetzner üzerinden bir sunucu (server) kiralandı. Geliştirilen sistemin ve veritabanının gerekli yapılandırmaları tamamlanarak tüm sistem sunucu ortamına taşındı ve çalışabilirliği test edildi.

Geliştirme dalı (pull request #83) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx`, `MatchedAdCard.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `AdCard.tsx` bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (pull request #84) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `PetListingCard.tsx`, `FavoritesPage.tsx`, `listingpage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #85) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetListingCard.tsx`, `FavoritesPage.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `listingpage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #86) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `imageUrl.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #87) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageUrl.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `FavoriteAdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #123) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdService.java`, `FavoriteAdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `FavoriteAdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #124) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #88) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `HomePage.tsx`, `mesafe.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #89) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #90) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #91) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #121) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdminController.java`, `AdRepository.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #122) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `GlobalExceptionHandler.java`, `V18__ads_description_text.sql`, `GlobalExceptionHandlerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `1020d887`, `d827f774`, `ff065f18`, `5564b6cf`, `02e433eb`, `a63f66a7`, `104d631d`, `6a71913e`, `39a67838`, `e6068063`, `471fbff2`, `acd72960`, `5ea1402c`, `2e4ed3b1`, `36a33ece`, `cc4605dd`, `88f6824b`, `61e88fe6`, `bfb1f95b`

## 22. Gün (2026-08-22)

Geliştirme dalı (pull request #125) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchController.java`, `AiMatchService.java`, `AiMatchControllerTest.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #126) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicAdController.java`, `AdRepository.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #129) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `enum-aynasi.json`, `pom.xml` ve 122 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #128) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `NotificationService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #127) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `ResetPasswordRequest.java`, `EmailService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #92) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `listingpage.test.tsx`, `listingpage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #93) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ResetPasswordPage.test.tsx`, `ResetPasswordPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #94) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Header.test.tsx`, `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #95) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `MyListingsPage.tsx`, `MyMatchesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #96) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Adoption.test.tsx`, `Adoption.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #97) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `BottomNav.test.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `App.css`, `App.tsx` ve 21 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #98) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `PetListingCard.test.tsx`, `PetListingCard.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #99) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `CreateAdLayout.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #100) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.test.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #131) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminController.java`, `AdCreateRequest.java`, `AdResponse.java` ve 24 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #101) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetListingCard.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #108) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Footer.tsx`, `GizlilikPage.test.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #107) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `robots.txt`, `sitemap.xml` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `f9ff9dc4`, `eb379217`, `5996344e`, `cdcba82f`, `c05bf877`, `b05b0e30`, `e1d78d23`, `d260a6bc`, `60734f18`, `b36b153b`, `cbb67a02`, `9ad411e8`, `090222e3`, `cfb0facc`, `29b4bd24`, `0062abe8`, `f6b98421`, `2a7b130c`, `24f2ed26`

## 23. Gün (2026-08-22 — 2026-08-23)

Geliştirme dalı (pull request #109) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveFoundModal.test.tsx`, `ResolveFoundModal.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AiAutofillCard.tsx`, `AiMatchModal.tsx`, `AddListingPage.tsx` ve 5 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #111) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAutofillCard.tsx`, `AiMatchModal.tsx`, `AddListingPage.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `IsMatchRequiredTest.java`, `AdMapperTest.java`, `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #134) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisPublisher.java`, `AiAnalysisRequest.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #112) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `IsMatchRequiredTest.java`, `AdControllerTest.java`, `AdoptionControllerTest.java` ve 2 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #135) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `IsMatchRequiredTest.java`, `AdControllerTest.java`, `AdoptionControllerTest.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #113) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MapPage.tsx`, `haritaSunum.test.ts`, `haritaSunum.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #114) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BottomNav.test.tsx`, `BottomNav.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Veritabanı şema versiyonlaması için Flyway migration betiği (`V20__add_is_match_required_to_ads.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

Geliştirme dalı (pull request #136) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V20__add_is_match_required_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #141) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `PatiMatiApplication.java`, `AiAnalysisListener.java` ve 83 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Yapay zeka servisinde `requirements.txt` üzerinde teknik düzenlemeler yapıldı.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `ci.yml`, `.gitignore` ve 28 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

İlan yönetimi arayüzünde `AdEditModal.tsx`, `MatchCard.tsx`, `MatchedAdCard.tsx` ve 5 ek dosya bileşenleri güncellendi. İlan detay gösterimi ve form doğrulama mantığı düzenlendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BottomNav.test.tsx`, `BottomNav.tsx`, `NotificationPanel.tsx` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #117) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `MatchCard.tsx`, `MatchedAdCard.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `32f65e10`, `8796a3cc`, `50c31833`, `b199e2b4`, `ef775451`, `30d4a6d2`, `74a75ca7`, `c25db841`, `339236a5`, `4c0dc255`, `964fdef0`, `0e18d340`, `b071d01b`, `5474a976`, `a4994cee`, `b82f96a1`, `773aef75`, `e50f925c`, `5cc8eb1b`

## 24. Gün (2026-08-23)

Yönetim paneli kapsamında `AdminFilterBar.tsx`, `useDebounce.ts`, `AdminComplaintsPage.tsx` ve 5 ek dosya bileşenleri güncellendi. Şikayet takibi ve yönetim aksiyon butonları düzenlendi.

Geliştirme dalı (pull request #118) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminFilterBar.tsx`, `useDebounce.ts`, `AdminComplaintsPage.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdminControllerSearchTest.java`, `AdminServiceImplTest.java`, `AdminSpecificationTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #144) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminController.java`, `AdComplaintRepository.java`, `AdRepository.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `package-lock.json`, `package.json`, `SightingModal.tsx` ve 6 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

Geliştirme dalı (pull request #119) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `package.json`, `SightingModal.tsx` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `imageCompression.test.ts`, `imageCompression.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #120) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageCompression.test.ts`, `imageCompression.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `useChatWebSocket.ts` ve 6 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Servis katmanı doğrulamaları için `UserPresenceControllerTest.java`, `PresenceEventListenerTest.java`, `NotificationServiceTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #121) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `useChatWebSocket.ts` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #145) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `UserPresenceController.java`, `NotificationResponse.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #122) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #123) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #124) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx`, `websocket.ts` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.


*Geliştirme Commitleri:* `16bd6330`, `e8d5da2b`, `00e6cb9d`, `1208cf92`, `fe341b09`, `abce4320`, `44ed5024`, `9723b273`, `4fba3fb3`, `89143674`, `92bc1373`, `75e27d6b`, `36c8c100`, `e623844e`, `49fec050`, `d10fa4b0`, `3eb2f56a`, `627326a4`, `6378eff0`

## 25. Gün (2026-08-23)

Geliştirme dalı (pull request #125) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #126) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `websocket.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #127) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `MessageServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #146) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageRepository.java`, `MessageService.java`, `MessageServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx`, `websocket.ts` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Servis katmanı doğrulamaları için `WebSocketConfigTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #149) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatController.java`, `WebSocketChannelInterceptor.java`, `MessageService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #129) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #128) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `ChatPage.tsx`, `types.ts` bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Anlık iletişim altyapısında `WebSocketChannelInterceptor.java`, `MessageService.java` sınıfları güncellendi. STOMP istemci bağlantı noktaları ve canlı mesaj kanalı güvenlik denetimleri düzenlendi.

Anlık mesajlaşma modülünde `useChatWebSocket.ts`, `ChatPage.tsx`, `api.ts` ve 1 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #151) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketChannelInterceptor.java`, `MessageService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #150) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `NotificationRepository.java`, `MessageService.java`, `NotificationService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `WebSocketConfigTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Kimlik doğrulama katmanında `AuthContext.tsx`, `websocket.ts` bileşenleri güncellendi. Token saklama ve giriş/kayıt form akışları düzenlendi.


*Geliştirme Commitleri:* `d5b9081a`, `97cff0f9`, `807b37ed`, `b0e353a8`, `f17bb63c`, `25b98614`, `68e3ffd6`, `91a958c0`, `d4faf424`, `faf29c48`, `f6712757`, `955545fb`, `93b744e3`, `c7f116d4`, `30f47de8`, `6c5449aa`, `f64a2db6`, `0402c94e`, `8ee161fe`

## 26. Gün (2026-08-23 — 2026-08-24)

Geliştirme dalı (pull request #152) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `WebSocketConfigTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #131) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useChatWebSocket.ts`, `ChatPage.tsx`, `api.ts` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #130) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiMatchPage.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `websocket.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #133) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AuthContext.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `websocket.ts` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #134) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #132) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `App.css`, `AdCard.tsx` ve 56 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #135) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MapPicker.tsx`, `SightingModal.tsx`, `AddListingPage.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #136) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useChatWebSocket.ts`, `websocket.baglanti-durumu.test.ts`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #153) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterServiceImpl.java`, `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #154) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `WebSocketConfigTest.java`, `MesajCanliTeslimatTest.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AiAnalyzeControllerTest.java`, `AiAnalyzeMapperTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #156) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalyzeController.java`, `AiAnalyzeResponse.java`, `AiAnalyzeMapper.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AiAnahtarBasligiTest.java`, `AiMatchServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #158) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `AiMatchService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #137) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `firebase-messaging-sw.js`, `generate-firebase-sw.mjs`, `sw-bildirim-hedefi.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #159) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V27__ads_description_text_tekrar.sql`, `AciklamaSutunuTipiTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `2e45e227`, `b383cc78`, `91094978`, `4419f3fd`, `23530ebc`, `777b5dc2`, `6362b11f`, `6c9db67e`, `a25adfa7`, `adb599e3`, `58883df9`, `088edc99`, `79ff4fd9`, `07c12b1b`, `f6f9ec3a`, `9fe66dc9`, `c6bc5d31`, `6e08ec9c`

## 27. Gün (2026-08-24 — 2026-08-25)

Geliştirme dalı (pull request #139) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #138) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RequireAuth.tsx`, `RequireGuest.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 4 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `firebase-messaging-sw.js`, `generate-firebase-sw.mjs`, `RequireAuth.tsx` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #140) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `Dockerfile` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #141) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #142) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `Dockerfile`, `nginx.conf` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #143) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `nginx.conf` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `nginx.conf` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #144) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `nginx.conf` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #145) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdEditModal.tsx`, `Header.tsx` ve 26 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #157) ilgili ana dikey ile birleştirildi. Bu birleştirme (merge) işleminde doğrudan uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdEditModal.tsx`, `Header.tsx` ve 26 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #147) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `types.ts`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #148) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `Header.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `7459e79b`, `e7db3fe5`, `746da895`, `307911e2`, `4f922d86`, `e3916d5a`, `667a448d`, `d5684cf2`, `9829888a`, `0055570d`, `e4450900`, `7d241cbf`, `88bf70dd`, `052f12e9`, `826b99c2`, `d891c69a`, `d3c3a3f4`, `5645d7b6`

## 28. Gün (2026-08-25 — 2026-08-26)

Geliştirme dalı (pull request #149) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `InstagramPublishModal.tsx`, `AddListingPage.tsx`, `AdminDashboardPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `enum-aynasi.json` ve 146 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #162) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `PresenceController.java`, `UserStatusEvent.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `enum-aynasi.json` ve 149 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #163) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicPetAnalysisController.java`, `PetAnalysisRateLimiter.java`, `AiMatchService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `AiMatchService.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #165) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Backend projesinde `AiMatchService.java`, `NotificationService.java` üzerinde teknik düzenleme yapıldı. İlgili veri erişim veya servis mantığı güncellendi.

Geliştirme dalı (pull request #166) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchService.java`, `NotificationService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `AdminControllerSearchTest.java`, `SahiplendirmeZorunluAlanTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #167) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminControllerSearchTest.java`, `SahiplendirmeZorunluAlanTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Servis katmanı doğrulamaları için `PresenceControllerTest.java`, `UserPresenceControllerTest.java`, `PresenceEventListenerTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

Geliştirme dalı (pull request #169) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `PresenceController.java`, `UserPresenceController.java` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 4 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdEditModal.tsx` ve 33 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #151) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `useAdMatchingMachine.test.ts`, `useAdMatchingMachine.ts`, `AddListingPage.tsx` ve 1 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #152) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useAdMatchingMachine.test.ts`, `useAdMatchingMachine.ts`, `AddListingPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.


*Geliştirme Commitleri:* `39c046bb`, `bcd92e28`, `22af69b6`, `f9dce40d`, `d1469c2d`, `c93635b9`, `b05db129`, `0cb5d2dd`, `4b1ce167`, `fae6d779`, `88b8d98b`, `9637d084`, `a28fb9a7`, `b6ff4731`, `e3d2b20a`, `f3c9be81`, `8199b1bb`, `5aa6845b`

## 29. Gün (2026-08-26 — 2026-08-27)

Frontend projesinde `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 1 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #153) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Anlık mesajlaşma modülünde `MatchCard.test.tsx`, `MatchCard.tsx`, `SharedAdCard.test.tsx` ve 7 ek dosya bileşenleri düzenlendi. Sohbet görünümü ve kullanıcı mesaj akışı güncellendi.

Geliştirme dalı (pull request #154) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.test.tsx`, `MatchCard.tsx`, `SharedAdCard.test.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `AddListingPage.test.tsx`, `AddListingPage.tsx`, `PetDetailPage.test.tsx` ve 1 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (pull request #157) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.test.tsx`, `AddListingPage.tsx`, `PetDetailPage.test.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #160) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `MunicipalityReportQueuePage.tsx`, `PublicReportPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #161) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

Geliştirme dalı (pull request #150) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdCard.tsx` ve 93 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `BenNeyimPage.test.tsx`, `BenNeyimPage.tsx`, `petAnalizi.ts` ve 3 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdCard.tsx` ve 93 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `BenNeyimPage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Header.test.tsx`, `Header.tsx` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `PetAiReportCard.tsx`, `PetReportView.tsx`, `BenNeyimPage.test.tsx` ve 3 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Header.test.tsx`, `Header.tsx` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

Frontend projesinde `Header.test.tsx`, `Header.tsx`, `BenNeyimPage.test.tsx` ve 1 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Frontend projesinde `App.css`, `App.tsx`, `BottomNav.tsx` ve 6 ek dosya üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

Frontend projesinde `Adoption.test.tsx`, `listingpage.test.tsx`, `listingpage.tsx` üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.


*Geliştirme Commitleri:* `eaa54393`, `00344f6d`, `7c9006fa`, `e3817b2b`, `cb4611eb`, `f8238bb5`, `ec5e3e6c`, `29e45304`, `c269dd71`, `55940bf8`, `c97636fe`, `98b08bb2`, `9e58764b`, `28fed81e`, `ac7d558b`, `ff8f59e6`, `5d4318da`, `e6510521`

## 30. Gün

Staj sürecinin son gününde, bir aydır üzerinde titizlikle çalıştığımız PatiMati projesinin final sunumu kurum yöneticilerine ve diğer ekiplere gerçekleştirildi. Sunum esnasında projenin çıkış noktası, kullanılan teknolojiler, mimari kararlar, karşılaşılan teknik zorluklar ve bunlara üretilen çözümler detaylı bir şekilde aktarıldı. Uygulamanın Hetzner sunucusu üzerindeki performansı, kullanıcı arayüzü ve aktif çalışan modülleri canlı bir demo ile gösterildi. Sunumun ardından gelen sorular yanıtlanıp profesyonellerden geri bildirimler alındı. Geliştirme ve sunum sürecinin başarıyla tamamlanmasıyla birlikte, üniversiteye teslim edilecek staj defteri onayları, değerlendirme formları ve kurum içi zorunlu ilişik kesme evraklarının hazırlığı eksiksiz bir şekilde yapılarak staj programı noktalandı.

---

**Toplam İncelenen ve Atanan Commit Sayısı:** 490
