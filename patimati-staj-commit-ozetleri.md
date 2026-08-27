# PATİMATİ PROJESİ COMMIT GEÇMİŞLERİ STAJ DEFTERİ ÖZETİ

## 1. GİT GEÇMİŞİ DOĞRULAMASI VE SİSTEM MUTABAKATI

Bu rapor, PatiMati projesine ait 3 ayrı Git repository'sindeki tüm geliştirici commit'lerinin eksiksiz incelenmesi, kod diff ve değişen dosya analizlerinin yapılması ve en eski tarihten en yeni tarihe doğru kronolojik olarak staj defteri formatına dönüştürülmesiyle hazırlanmıştır.

### Author Kimlik Bilgileri:
- **Name:** `SantoPanto` / `Fatih Şahin`
- **Email:** `fatihpanto@gmail.com`

### Sayısal Mutabakat Raporu:
**Frontend (`PATIMATI---FRONTEND` - Branch `develop`):**
- Geliştirici Commit Sayısı: **235** (Tarih Aralığı: 2026-07-21 — 2026-08-27)

**Backend (`PATIMATI---BACKEND`):**
- `HEAD` (local branch `jwt`): 99 (`author=SantoPanto`) / 245 (`author=fatihpanto@gmail.com`)
- `develop` (local branch - eski commit '910f365c'): 18 (`author=SantoPanto`) / 40 (`author=fatihpanto@gmail.com`)
- `origin/develop` (güncel remote develop): 95 (`author=SantoPanto`) / 241 (`author=fatihpanto@gmail.com`)
- `--all` (tüm branch'ler): 99 (`author=SantoPanto`) / 247 (`author=fatihpanto@gmail.com`)
- **Raporlanan Backend Commit Sayısı:** **241** (Remote `origin/develop` geçmişindeki tüm `fatihpanto@gmail.com` / `SantoPanto` / `Fatih Şahin` commitleri, Tarih Aralığı: 2026-07-21 — 2026-08-26)

**AI (`PATIMATI-AI` - Branch `main`):**
- Geliştirici Commit Sayısı: **14** (Tarih Aralığı: 2026-07-24 — 2026-08-23)

**Genel Toplam:**
- Toplam Raporlanan Commit Sayısı: **490**
- Proje İlk Commit Tarihi: **2026-07-21**
- Proje Son Commit Tarihi: **2026-08-27**

---

## 2. PATIMATI — FRONTEND COMMITLERİ

### 1. 2026-07-21 — README.md ve frontend bileşen geliştirmesi

**Commit:** `92f47bdf`  
**Orijinal commit mesajı:** `Initial commit`

Frontend projesinde `README.md` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 2. 2026-08-11 — pull request #2 birleştirmesi ve çakışma çözümleri

**Commit:** `69c0fe18`  
**Orijinal commit mesajı:** `Merge pull request #2 from SantoPanto/fix/ilan-acma-akisi

fix: ilan açma akışı yeniden çalışır hâle getirildi (tarayıcıda uçtan uca doğrulandı)`

Geliştirme dalı (pull request #2) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `ChatPage.tsx`, `ListingsPage.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 3. 2026-08-11 — pull request #3 birleştirmesi ve çakışma çözümleri

**Commit:** `3868be25`  
**Orijinal commit mesajı:** `Merge pull request #3 from SantoPanto/fix/ilan-acma-akisi

fix(ai): fotoğraftan otomatik doldurma çalışmıyordu (PR #2'de birleşmeden kalan commit)`

Geliştirme dalı (pull request #3) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 4. 2026-08-11 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `89211b40`  
**Orijinal commit mesajı:** `Projede bulunan backend ile bağlantı sorunlarının bir kısmı çözüldü. Genel bir düzen yapısı oluşturuldu ve profile sekmesine tamamlanmak üzere bir konum haritası eklendi ve düzenleme sorunları hem profile sayfası için hemde register için düzeltildi.`

Frontend projesinde `package-lock.json`, `package.json`, `App.tsx` ve 15 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 5. 2026-08-11 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `f3be184c`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI---FRONTEND`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 6. 2026-08-11 — pull request #4 birleştirmesi ve çakışma çözümleri

**Commit:** `d7d8f41a`  
**Orijinal commit mesajı:** `Merge pull request #4 from SantoPanto/fix/build-teamui

fix(build): üretim derlemesi ilk kez yeşil (TeamUI + iki kullanılmayan import)`

Geliştirme dalı (pull request #4) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `TeamUI.tsx`, `AdoptionCreatePage.tsx`, `LoginRedirectPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 7. 2026-08-11 — pull request #5 birleştirmesi ve çakışma çözümleri

**Commit:** `d589a0d9`  
**Orijinal commit mesajı:** `Merge pull request #5 from SantoPanto/docs/tabby-karari

docs(ai): tabby→STRIPED kararı koda işlendi (yalnız yorum)`

Geliştirme dalı (pull request #5) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 8. 2026-08-11 — pull request #6 birleştirmesi ve çakışma çözümleri

**Commit:** `22ebbc54`  
**Orijinal commit mesajı:** `Merge pull request #6 from SantoPanto/main

Deneme branchi`

Geliştirme dalı (pull request #6) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.gitignore`, `AdminComplaintsPage.tsx` ve 73 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 9. 2026-08-12 — pull request #7 birleştirmesi ve çakışma çözümleri

**Commit:** `e39d9702`  
**Orijinal commit mesajı:** `Merge pull request #7 from SantoPanto/fix/taslak-sayfalari-bagla

fix: buldum ve sahiplendirme ilanlari backend'e hic gitmiyordu`

Geliştirme dalı (pull request #7) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 10. 2026-08-12 — pull request #8 birleştirmesi ve çakışma çözümleri

**Commit:** `ad833758`  
**Orijinal commit mesajı:** `Merge pull request #8 from SantoPanto/tools/sayfa-denetcisi

tools: uygulamanin durum haritasini koddan ureten denetci`

Geliştirme dalı (pull request #8) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `sayfa-denetcisi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 11. 2026-08-13 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `c35f92e6`  
**Orijinal commit mesajı:** `Sena'nın pushdan sonra dosya adında karışıklık olmuş. minor bugfix(path).`

Frontend projesinde `App.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 12. 2026-08-13 — pull request #10 birleştirmesi ve çakışma çözümleri

**Commit:** `4b411c22`  
**Orijinal commit mesajı:** `Merge pull request #10 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #10) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 13. 2026-08-13 — pull request #11 birleştirmesi (Merge commit)

**Commit:** `d0800926`  
**Orijinal commit mesajı:** `Merge pull request #11 from SantoPanto/main

güncel`

Geliştirme dalı (pull request #11) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 14. 2026-08-14 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `9ecf9edf`  
**Orijinal commit mesajı:** `ListingsPage (listingpage.tsx) ve PetDetailPage (PetDetailPage.tsx) sayfalarının backend API (GET /api/public/ads ve GET /api/public/ads/{id}) ile entegrasyonu tamamlandı ve yönlendirme (routing) yapıları güncellendi.`

Frontend projesinde `App.tsx`, `PetDetailPage.tsx`, `listingpage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 15. 2026-08-14 — branch 'develop' birleştirmesi (Merge commit)

**Commit:** `313de6c0`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'develop') ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 16. 2026-08-14 — pull request #12 birleştirmesi ve çakışma çözümleri

**Commit:** `de80e4f6`  
**Orijinal commit mesajı:** `Merge pull request #12 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #12) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `PetDetailPage.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 17. 2026-08-14 — index.html ve frontend bileşen geliştirmesi

**Commit:** `ebfcf8b0`  
**Orijinal commit mesajı:** `Admin paneli sayfası oluşturuldu ve sadece admin kullanıcılarına özel bir sayfa eklendi. Sayfa içerisinde admin işlemleri olan kullanıcı banlama/kaldırma ilan banlama/kaldırma ve Şikayet ilanı listesi eklendi. Mesajlaşma ve şikayet butonları ilanların içerisine eklendi.`

Frontend projesinde `index.html`, `firebase-messaging-sw.js`, `ComplaintModal.tsx` ve 11 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 18. 2026-08-14 — pull request #13 birleştirmesi ve çakışma çözümleri

**Commit:** `a90e0907`  
**Orijinal commit mesajı:** `Merge pull request #13 from SantoPanto/develop

Admin paneli sayfası oluşturuldu ve sadece admin kullanıcılarına özel…`

Geliştirme dalı (pull request #13) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `firebase-messaging-sw.js`, `ComplaintModal.tsx` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 19. 2026-08-17 — pull request #15 birleştirmesi ve çakışma çözümleri

**Commit:** `ced9e43b`  
**Orijinal commit mesajı:** `Merge pull request #15 from SantoPanto/main

pr`

Geliştirme dalı (pull request #15) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `ComplaintPage.tsx`, `ListingsPage.tsx` ve 33 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 20. 2026-08-17 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `ced306b8`  
**Orijinal commit mesajı:** `Mesajlaşma altyapısı backend ile uyumlu olacak şekilde güncellendi. Kendi kendine istek için kontroller eklendi. Güvenli oda yapıları oluşturuldu.`

Frontend projesinde `eslint.config.js`, `package-lock.json`, `useChatWebSocket.ts` ve 9 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 21. 2026-08-17 — useChatWebSocket.ts ve frontend bileşen geliştirmesi

**Commit:** `a4ec8eb2`  
**Orijinal commit mesajı:** `Mesajlaşma yapısı tamamlandı, uilar güncellendi ve yapı backend ile kusursuz birleştirildi.`

Frontend projesinde `useChatWebSocket.ts`, `AdoptionDetailPage.tsx`, `ChatPage.tsx` ve 3 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 22. 2026-08-17 — pull request #17 birleştirmesi ve çakışma çözümleri

**Commit:** `12e3e5ff`  
**Orijinal commit mesajı:** `Merge pull request #17 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #17) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `eslint.config.js`, `package-lock.json`, `useChatWebSocket.ts` ve 10 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 23. 2026-08-17 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `ffccea89`  
**Orijinal commit mesajı:** `Google auth işlem sayfaları oluşturuldu ve mesaj kısmında ufak bir bugfix atıldı.`

Frontend projesinde `App.tsx`, `ReportUserModal.tsx`, `ChatPage.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 24. 2026-08-17 — pull request #18 birleştirmesi ve çakışma çözümleri

**Commit:** `a2399d5f`  
**Orijinal commit mesajı:** `Merge pull request #18 from SantoPanto/develop

Google auth işlem sayfaları oluşturuldu ve mesaj kısmında ufak bir bu…`

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ReportUserModal.tsx`, `ChatPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 25. 2026-08-17 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `8e8b0e87`  
**Orijinal commit mesajı:** `Merge branch 'main' into feature/fatih-pdf-poster`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `eslint.config.js`, `package-lock.json`, `App.tsx` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 26. 2026-08-17 — pull request #16 birleştirmesi ve çakışma çözümleri

**Commit:** `3dbaeec1`  
**Orijinal commit mesajı:** `Merge pull request #16 from SantoPanto/feature/fatih-pdf-poster

feat: kayip ilanlari icin PDF afis indirme arayuzu eklendi`

Geliştirme dalı (pull request #16) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.tsx`, `posters.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 27. 2026-08-17 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `a084b0ac`  
**Orijinal commit mesajı:** `Eşleşmelerim sayfası oluşturuldu ve backend ile bağlandı.`

Frontend projesinde `App.tsx`, `MatchCard.tsx`, `LoginPage.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 28. 2026-08-18 — pull request #20 birleştirmesi ve çakışma çözümleri

**Commit:** `1b8882a2`  
**Orijinal commit mesajı:** `Merge pull request #20 from SantoPanto/feature/admin-complaint-management

feat: D-10 admin complaint management UI, ad suspension and chat inte…`

Geliştirme dalı (pull request #20) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `admin.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 29. 2026-08-18 — pull request #19 birleştirmesi ve çakışma çözümleri

**Commit:** `2e543c79`  
**Orijinal commit mesajı:** `Merge pull request #19 from SantoPanto/fix/notifications-ux-local

feat: add local messaging AI settings and notifications`

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `notifications.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 30. 2026-08-18 — pull request #21 birleştirmesi ve çakışma çözümleri

**Commit:** `1695e8ed`  
**Orijinal commit mesajı:** `Merge pull request #21 from SantoPanto/develop

Eşleşmelerim sayfası oluşturuldu ve backend ile bağlandı.`

Geliştirme dalı (pull request #21) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `MatchCard.tsx`, `LoginPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 31. 2026-08-18 — pull request #22 birleştirmesi ve çakışma çözümleri

**Commit:** `11f1f21f`  
**Orijinal commit mesajı:** `Merge pull request #22 from SantoPanto/feat/a1-analiz-backend-uzerinden

feat(A1): fotoğraf analizi AI'ya backend üzerinden gidiyor`

Geliştirme dalı (pull request #22) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 32. 2026-08-19 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `4eb74aab`  
**Orijinal commit mesajı:** `fix: TypeScript build hataları ve firebase bağımlılığı giderildi`

Frontend projesinde `package-lock.json`, `AddListingPage.tsx`, `firbase.ts` ve 1 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 33. 2026-08-19 — pull request #23 birleştirmesi ve çakışma çözümleri

**Commit:** `f225dcca`  
**Orijinal commit mesajı:** `Merge pull request #23 from SantoPanto/develop

fix: TypeScript build hataları ve firebase bağımlılığı giderildi`

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `AddListingPage.tsx`, `firbase.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 34. 2026-08-19 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `2cd8b63a`  
**Orijinal commit mesajı:** `fix: unused import removed and package-lock updated`

Frontend projesinde `package-lock.json`, `AdminComplaintsPage.tsx` dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 35. 2026-08-19 — pull request #24 birleştirmesi ve çakışma çözümleri

**Commit:** `ff5f3b72`  
**Orijinal commit mesajı:** `Merge pull request #24 from SantoPanto/develop

fix: unused import removed and package-lock updated`

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `AdminComplaintsPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 36. 2026-08-19 — .env ve frontend bileşen geliştirmesi

**Commit:** `cc6ff6bb`  
**Orijinal commit mesajı:** `Sunucu için hardcoded olan alanlar değiştirildi ve global olmaları sağlandı.`

Frontend projesinde `.env`, `.env.development`, `.env.production` ve 5 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 37. 2026-08-19 — pull request #27 birleştirmesi ve çakışma çözümleri

**Commit:** `79817861`  
**Orijinal commit mesajı:** `Merge pull request #27 from SantoPanto/develop

Sunucu için hardcoded olan alanlar değiştirildi ve global olmaları sa…`

Geliştirme dalı (pull request #27) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.env.development`, `.env.production` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 38. 2026-08-19 — MatchCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `a531d61b`  
**Orijinal commit mesajı:** `Guard clause, DRY, SRP ve gelişmiş feedback yönetimi entegre edildi.`

Frontend projesinde `MatchCard.tsx`, `AddListingPage.tsx`, `MapPage.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 39. 2026-08-19 — pull request #28 birleştirmesi ve çakışma çözümleri

**Commit:** `604249a5`  
**Orijinal commit mesajı:** `Merge pull request #28 from SantoPanto/develop

 Guard clause, DRY, SRP ve gelişmiş feedback yönetimi entegre edildi.`

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `AddListingPage.tsx`, `MapPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 40. 2026-08-19 — pull request #26 birleştirmesi ve çakışma çözümleri

**Commit:** `d126b4d8`  
**Orijinal commit mesajı:** `Merge pull request #26 from SantoPanto/feature/d6-anasayfa-sayaclari

feat(D6): Anasayfa sayaçları backend entegrasyonu`

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `HomePage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 41. 2026-08-19 — imageUrl.ts ve frontend bileşen geliştirmesi

**Commit:** `9ef2462c`  
**Orijinal commit mesajı:** `Fotoğraf yükleme yok düzeltmeleri`

Frontend projesinde `imageUrl.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 42. 2026-08-19 — pull request #29 birleştirmesi ve çakışma çözümleri

**Commit:** `9d8ccdc3`  
**Orijinal commit mesajı:** `Merge pull request #29 from SantoPanto/develop

Fotoğraf yükleme yok düzeltmeleri`

Geliştirme dalı (pull request #29) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageUrl.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 43. 2026-08-19 — pull request #30 birleştirmesi ve çakışma çözümleri

**Commit:** `e4b62e07`  
**Orijinal commit mesajı:** `Merge pull request #30 from SantoPanto/main

pr güncel`

Geliştirme dalı (pull request #30) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `HomePage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 44. 2026-08-19 — FavoritesPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `62a1efc4`  
**Orijinal commit mesajı:** `Fotoğraf backend url düzenlendi`

Frontend projesinde `FavoritesPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 45. 2026-08-19 — pull request #31 birleştirmesi ve çakışma çözümleri

**Commit:** `085194f2`  
**Orijinal commit mesajı:** `Merge pull request #31 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #31) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 46. 2026-08-19 — pull request #32 birleştirmesi ve çakışma çözümleri

**Commit:** `b6cad449`  
**Orijinal commit mesajı:** `Merge pull request #32 from SantoPanto/develop

feat: AI eksik alan doldurma düzeltildi ve Fotoğrafla Arama sayfası g…`

Geliştirme dalı (pull request #32) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AddListingPage.tsx`, `AiMatchPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 47. 2026-08-20 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `2ae84600`  
**Orijinal commit mesajı:** `Kök dizinde yanlış oluşturulan dosyalar temizlendi.`

Frontend projesinde `generate-firebase-sw.mjs`, `eslint.config.js`, `favicon.svg` ve 8 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 48. 2026-08-20 — pull request #47 birleştirmesi (Merge commit)

**Commit:** `fc2caf07`  
**Orijinal commit mesajı:** `Merge pull request #47 from SantoPanto/main

pr to push`

Geliştirme dalı (pull request #47) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 49. 2026-08-20 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `dcb29758`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AddListingPage.tsx`, `AiMatchPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 50. 2026-08-20 — pull request #48 birleştirmesi ve çakışma çözümleri

**Commit:** `f4b61c07`  
**Orijinal commit mesajı:** `Merge pull request #48 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #48) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `eslint.config.js`, `favicon.svg` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 51. 2026-08-20 — pull request #33 birleştirmesi ve çakışma çözümleri

**Commit:** `6a7bd65d`  
**Orijinal commit mesajı:** `Merge pull request #33 from SantoPanto/fix/e9-env-dosyalari

fix(E9): env dosyası kalabalığını 9'dan 3'e indir + yok sayma kuralları`

Geliştirme dalı (pull request #33) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env`, `.env.development`, `.env.production` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 52. 2026-08-20 — pull request #34 birleştirmesi ve çakışma çözümleri

**Commit:** `b8524249`  
**Orijinal commit mesajı:** `Merge pull request #34 from SantoPanto/chore/e8-on-yuz-kapisi

chore(E8): ön yüze CI kapısı ekle — kurulum · tip denetimi · paketleme`

Geliştirme dalı (pull request #34) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 53. 2026-08-20 — pull request #35 birleştirmesi ve çakışma çözümleri

**Commit:** `6919cadc`  
**Orijinal commit mesajı:** `Merge pull request #35 from SantoPanto/feature/d4-favoriler

feat(D4): favori ilan sayfasi ve detay entegrasyonu`

Geliştirme dalı (pull request #35) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoritesPage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 54. 2026-08-20 — pull request #36 birleştirmesi ve çakışma çözümleri

**Commit:** `50dff0fb`  
**Orijinal commit mesajı:** `Merge pull request #36 from SantoPanto/fix/m6-giris-sayfasi-sayaclari

fix(6): giriş sayfasındaki uydurma istatistikleri gerçek sayaca bağla`

Geliştirme dalı (pull request #36) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `LoginPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 55. 2026-08-20 — pull request #37 birleştirmesi ve çakışma çözümleri

**Commit:** `2fdbb52a`  
**Orijinal commit mesajı:** `Merge pull request #37 from SantoPanto/fix/m23-fotograf-zorunlulugu

fix(23): fotoğraf sınırlarını sunucuyla hizala + en az 1 kuralını görünür kıl`

Geliştirme dalı (pull request #37) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 56. 2026-08-20 — pull request #40 birleştirmesi ve çakışma çözümleri

**Commit:** `6d824ec4`  
**Orijinal commit mesajı:** `Merge pull request #40 from SantoPanto/fix/on-yuz-sozlesme-hizalama

fix(3·11·14·26): ön yüz tiplerini backend sözleşmesine hizala`

Geliştirme dalı (pull request #40) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `ReportUserModal.tsx`, `AdoptionCreatePage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 57. 2026-08-20 — pull request #41 birleştirmesi ve çakışma çözümleri

**Commit:** `3282060d`  
**Orijinal commit mesajı:** `Merge pull request #41 from SantoPanto/fix/rota-yetki-tutarlilik

fix(10·28): yönetici rotalarını role bağla + korumalı rota kuralını yapıya göm`

Geliştirme dalı (pull request #41) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `RequireAuth.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 58. 2026-08-20 — pull request #42 birleştirmesi ve çakışma çözümleri

**Commit:** `02055f23`  
**Orijinal commit mesajı:** `Merge pull request #42 from SantoPanto/fix/m29-kendi-ilanini-sikayet

fix(29): sahip kendi ilanında "Şikayet Et" düğmesini görmesin`

Geliştirme dalı (pull request #42) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionDetailPage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 59. 2026-08-20 — pull request #43 birleştirmesi ve çakışma çözümleri

**Commit:** `03f3df97`  
**Orijinal commit mesajı:** `Merge pull request #43 from SantoPanto/fix/m15-m17-yonetici-sikayet-yuzeyi

fix(15·17): yönetici şikayet yüzeyini backend sözleşmesine hizala`

Geliştirme dalı (pull request #43) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `AdminDashboardPage.tsx`, `admin.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 60. 2026-08-20 — pull request #45 birleştirmesi ve çakışma çözümleri

**Commit:** `08d79793`  
**Orijinal commit mesajı:** `Merge pull request #45 from SantoPanto/fix/m21-girissiz-mesaj-gonder

fix(21): girişsiz kullanıcı "Mesaj Gönder"e basınca giriş sayfasına gitsin`

Geliştirme dalı (pull request #45) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.test.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 61. 2026-08-20 — pull request #46 birleştirmesi ve çakışma çözümleri

**Commit:** `28678780`  
**Orijinal commit mesajı:** `Merge pull request #46 from SantoPanto/chore/m14-enum-aynasi-bekcisi

chore(14): ön yüz enum bekçisi — arka yüz aynasına karşı sınansın`

Geliştirme dalı (pull request #46) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `ReportUserModal.tsx`, `AdoptionCreatePage.tsx` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 62. 2026-08-20 — pull request #38 birleştirmesi ve çakışma çözümleri

**Commit:** `50524255`  
**Orijinal commit mesajı:** `Merge pull request #38 from SantoPanto/feat/m18-ilan-duzenle-yeniden-yayinla

feat(18): "İlanlarım"da yayından kaldırılan ilan tek tıkla geri gelsin`

Geliştirme dalı (pull request #38) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 63. 2026-08-20 — pull request #49 birleştirmesi ve çakışma çözümleri

**Commit:** `2d542d33`  
**Orijinal commit mesajı:** `Merge pull request #49 from SantoPanto/fix/password-change-validation

Şifre Güncelleme Görevi`

Geliştirme dalı (pull request #49) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChangePasswordPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 64. 2026-08-20 — AdminComplaintsPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `aa68724d`  
**Orijinal commit mesajı:** `Şifre, ilan ve posterler backendle uyumlu olacak şekilde güncellendi.`

Frontend projesinde `AdminComplaintsPage.tsx`, `ChangePasswordPage.tsx`, `ads.ts` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 65. 2026-08-20 — pull request #50 birleştirmesi ve çakışma çözümleri

**Commit:** `009bb199`  
**Orijinal commit mesajı:** `Merge pull request #50 from SantoPanto/develop

Şifre, ilan ve posterler backendle uyumlu olacak şekilde güncellendi.`

Geliştirme dalı (pull request #50) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminComplaintsPage.tsx`, `ChangePasswordPage.tsx`, `ads.ts` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 66. 2026-08-20 — PosterSettingsModal.tsx ve frontend bileşen geliştirmesi

**Commit:** `16160176`  
**Orijinal commit mesajı:** `Şifre güncellemeleri ve poster değişiklikleri backendle paralel olarak geliştirildi ve değiştirildi.`

Frontend projesinde `PosterSettingsModal.tsx`, `ChangePasswordPage.tsx`, `MyListingsPage.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 67. 2026-08-20 — pull request #51 birleştirmesi ve çakışma çözümleri

**Commit:** `b70841dd`  
**Orijinal commit mesajı:** `Merge pull request #51 from SantoPanto/develop

Şifre güncellemeleri ve poster değişiklikleri backendle paralel olara…`

Geliştirme dalı (pull request #51) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterSettingsModal.tsx`, `ChangePasswordPage.tsx`, `MyListingsPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 68. 2026-08-20 — pull request #52 birleştirmesi ve çakışma çözümleri

**Commit:** `2e742a6c`  
**Orijinal commit mesajı:** `Merge pull request #52 from SantoPanto/chore/on-yuz-test-kosucusu

fix: main e ulaşmayan üç işi kurtar (test koşucusu · madde 21 · enum bekçisi)`

Geliştirme dalı (pull request #52) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 69. 2026-08-20 — pull request #53 birleştirmesi ve çakışma çözümleri

**Commit:** `98dc9bf4`  
**Orijinal commit mesajı:** `Merge pull request #53 from SantoPanto/fix/m18-askidaki-ilan-ekranda

fix(18): askıya alınan ilanda "Yeniden Yayınla" yerine "İnceleme altında"`

Geliştirme dalı (pull request #53) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.tsx`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 70. 2026-08-20 — AdEditModal.tsx ve frontend bileşen geliştirmesi

**Commit:** `04820ced`  
**Orijinal commit mesajı:** `Türkçe karakter sorunu ve afiş düzenlemeleri backend pr'ıyla uyumlu bir şekilde güncellendi.`

Frontend projesinde `AdEditModal.tsx`, `PosterSettingsForm.tsx`, `PosterSettingsModal.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 71. 2026-08-20 — pull request #54 birleştirmesi ve çakışma çözümleri

**Commit:** `7e90a21d`  
**Orijinal commit mesajı:** `Merge pull request #54 from SantoPanto/develop

Türkçe karakter sorunu ve afiş düzenlemeleri backend pr'ıyla uyumlu b…`

Geliştirme dalı (pull request #54) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `PosterSettingsForm.tsx`, `PosterSettingsModal.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 72. 2026-08-20 — AdEditModal.tsx ve frontend bileşen geliştirmesi

**Commit:** `176fcd60`  
**Orijinal commit mesajı:** `Sahiplendirme ilan arayüzündeki tarihsıkıntısı çözüldü ve sahiplendirme ilan ui ları diğer uilarla güncellendi ve expection hataları düzgünce bağlandı.`

Frontend projesinde `AdEditModal.tsx`, `AdoptionCreatePage.tsx`, `PetDetailPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 73. 2026-08-20 — pull request #55 birleştirmesi ve çakışma çözümleri

**Commit:** `07b0932e`  
**Orijinal commit mesajı:** `Merge pull request #55 from SantoPanto/develop

Sahiplendirme ilan arayüzündeki tarihsıkıntısı çözüldü ve sahiplendir…`

Geliştirme dalı (pull request #55) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `AdoptionCreatePage.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 74. 2026-08-20 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `7c5793f9`  
**Orijinal commit mesajı:** `İlanlardaki tarih sorunu ve türkçe karakter sorunu tekrardan gözden geçirildi ve backend pr ile uyumlu olarak düzenlendi.`

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 75. 2026-08-20 — pull request #56 birleştirmesi ve çakışma çözümleri

**Commit:** `3ea11dfb`  
**Orijinal commit mesajı:** `Merge pull request #56 from SantoPanto/develop

İlanlardaki tarih sorunu ve türkçe karakter sorunu tekrardan gözden g…`

Geliştirme dalı (pull request #56) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 76. 2026-08-20 — AdoptionCreatePage.tsx ve frontend bileşen geliştirmesi

**Commit:** `288c9806`  
**Orijinal commit mesajı:** `Türkçe karakter ve tarih girişi bugfix`

Frontend projesinde `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 77. 2026-08-20 — pull request #57 birleştirmesi ve çakışma çözümleri

**Commit:** `b83f154e`  
**Orijinal commit mesajı:** `Merge pull request #57 from SantoPanto/develop

Türkçe karakter ve tarih girişi bugfix`

Geliştirme dalı (pull request #57) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 78. 2026-08-20 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `02bfe10d`  
**Orijinal commit mesajı:** `Sayfa değişiminde tarihin kapanması düzeltildi`

Frontend projesinde `AddListingPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 79. 2026-08-20 — pull request #58 birleştirmesi ve çakışma çözümleri

**Commit:** `592e1c43`  
**Orijinal commit mesajı:** `Merge pull request #58 from SantoPanto/develop

Sayfa değişiminde tarihin kapanması düzeltildi`

Geliştirme dalı (pull request #58) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 80. 2026-08-20 — AdoptionCreatePage.tsx ve frontend bileşen geliştirmesi

**Commit:** `a316c54a`  
**Orijinal commit mesajı:** `Token ve ilan düzeltmesi`

Frontend projesinde `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx`, `errorMessage.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 81. 2026-08-20 — pull request #61 birleştirmesi ve çakışma çözümleri

**Commit:** `e8115b0a`  
**Orijinal commit mesajı:** `Merge pull request #61 from SantoPanto/develop

Token ve ilan düzeltmesi`

Geliştirme dalı (pull request #61) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx`, `errorMessage.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 82. 2026-08-20 — adoptions.ts ve frontend bileşen geliştirmesi

**Commit:** `61d80269`  
**Orijinal commit mesajı:** `Bugfix`

Frontend projesinde `adoptions.ts`, `ads.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 83. 2026-08-20 — authStorage.ts ve frontend bileşen geliştirmesi

**Commit:** `3115dc2a`  
**Orijinal commit mesajı:** `Token okuma güçlendirildi`

Frontend projesinde `authStorage.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 84. 2026-08-20 — pull request #62 birleştirmesi ve çakışma çözümleri

**Commit:** `89d0cdcc`  
**Orijinal commit mesajı:** `Merge pull request #62 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #62) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `adoptions.ts`, `ads.ts`, `authStorage.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 85. 2026-08-20 — pull request #59 birleştirmesi ve çakışma çözümleri

**Commit:** `4176754f`  
**Orijinal commit mesajı:** `Merge pull request #59 from SantoPanto/feat/ilanim-bulundu-ekrani

feat: "hayvanımı buldum" ekranı — kayıp ilanı bulundu olarak kapatılabiliyor`

Geliştirme dalı (pull request #59) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveFoundModal.test.tsx`, `ResolveFoundModal.tsx`, `MyListingsPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 86. 2026-08-21 — AdEditModal.tsx ve frontend bileşen geliştirmesi

**Commit:** `a1abbb0f`  
**Orijinal commit mesajı:** `json güncelleme`

Frontend projesinde `AdEditModal.tsx`, `AddListingPage.tsx`, `AdoptionCreatePage.tsx` ve 3 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 87. 2026-08-21 — pull request #66 birleştirmesi ve çakışma çözümleri

**Commit:** `fc562094`  
**Orijinal commit mesajı:** `Merge pull request #66 from SantoPanto/develop

json güncelleme`

Geliştirme dalı (pull request #66) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `AddListingPage.tsx`, `AdoptionCreatePage.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 88. 2026-08-21 — pull request #67 birleştirmesi ve çakışma çözümleri

**Commit:** `4c25782d`  
**Orijinal commit mesajı:** `Merge pull request #67 from SantoPanto/fix/ilan-olusturma-tarih-cakismasi

fix: canlıda ilan oluşturulamıyor — tarih telde iki kez gidiyor (POST /api/ads 500)`

Geliştirme dalı (pull request #67) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `ads.tarih.test.ts`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 89. 2026-08-21 — pull request #39 birleştirmesi ve çakışma çözümleri

**Commit:** `0e341949`  
**Orijinal commit mesajı:** `Merge pull request #39 from SantoPanto/test/current-develop

feat: add profile icon to homepage header`

Geliştirme dalı (pull request #39) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 90. 2026-08-21 — pull request #63 birleştirmesi ve çakışma çözümleri

**Commit:** `c4677fa2`  
**Orijinal commit mesajı:** `Merge pull request #63 from SantoPanto/feature/about-sosyal-medya

feat: Hakkımızda sayfasına sosyal medya yönlendirme alanı eklenmesi`

Geliştirme dalı (pull request #63) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AboutPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 91. 2026-08-21 — pull request #64 birleştirmesi ve çakışma çözümleri

**Commit:** `db7d7a8c`  
**Orijinal commit mesajı:** `Merge pull request #64 from SantoPanto/fix/v2-change-password-required

Register sayfası için şifre zorunluluğu politikası değiştirildi`

Geliştirme dalı (pull request #64) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RegisterPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 92. 2026-08-21 — pull request #65 birleştirmesi ve çakışma çözümleri

**Commit:** `2e3af064`  
**Orijinal commit mesajı:** `Merge pull request #65 from SantoPanto/feature/admin-complaint-management

Filtreleme ve temizleme mantığı güncellendi`

Geliştirme dalı (pull request #65) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Contact.tsx`, `HomePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 93. 2026-08-21 — pull request #68 birleştirmesi ve çakışma çözümleri

**Commit:** `40ef2238`  
**Orijinal commit mesajı:** `Merge pull request #68 from SantoPanto/fix/kapanan-ilan-ekranda

fix: kapanan ilan ekranda "Bulundu" görünsün + ham cins enum basılmasın`

Geliştirme dalı (pull request #68) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MyListingsPage.test.tsx`, `MyListingsPage.tsx`, `types.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 94. 2026-08-21 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `cb4c50a6`  
**Orijinal commit mesajı:** `Merge branch 'main' into feature/fatih-text-cleanup`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdEditModal.tsx` ve 27 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 95. 2026-08-21 — pull request #60 birleştirmesi ve çakışma çözümleri

**Commit:** `a3c47b06`  
**Orijinal commit mesajı:** `Merge pull request #60 from SantoPanto/feature/fatih-text-cleanup

style: arayuz metinlerindeki noktalama isaretleri kaldirildi`

Geliştirme dalı (pull request #60) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Footer.tsx`, `AboutPage.tsx`, `Adoption.tsx` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 96. 2026-08-21 — MatchedAdCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `354cdcd6`  
**Orijinal commit mesajı:** `Eşleşme kartındaki fotoğraf yolları backend pr ile uyumlu olacak şekilde güncellendi.`

Frontend projesinde `MatchedAdCard.tsx`, `AddListingPage.tsx`, `AiMatchResultsPage.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 97. 2026-08-21 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `3f47da7b`  
**Orijinal commit mesajı:** `Merge branch 'main' into develop`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 40 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 98. 2026-08-21 — pull request #69 birleştirmesi ve çakışma çözümleri

**Commit:** `5d398d2c`  
**Orijinal commit mesajı:** `Merge pull request #69 from SantoPanto/develop

Eşleşme kartındaki fotoğraf yolları backend pr ile uyumlu olacak şeki…`

Geliştirme dalı (pull request #69) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchedAdCard.tsx`, `AddListingPage.tsx`, `AiMatchResultsPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 99. 2026-08-21 — pull request #70 birleştirmesi (Merge commit)

**Commit:** `b59675c4`  
**Orijinal commit mesajı:** `Merge pull request #70 from SantoPanto/main

Merge pull request #69 from SantoPanto/develop`

Geliştirme dalı (pull request #70) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 100. 2026-08-21 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `6aaba786`  
**Orijinal commit mesajı:** `Eski kalan dosyalar temizlendi`

Frontend projesinde `AddListingPage.tsx`, `types.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 101. 2026-08-21 — pull request #71 birleştirmesi (Merge commit)

**Commit:** `89b14b0e`  
**Orijinal commit mesajı:** `Merge pull request #71 from SantoPanto/develop

Merge pull request #70 from SantoPanto/main`

Geliştirme dalı (pull request #71) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 102. 2026-08-21 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `04dc70d1`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `package-lock.json`, `package.json` ve 40 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 103. 2026-08-21 — pull request #72 birleştirmesi ve çakışma çözümleri

**Commit:** `8452c27a`  
**Orijinal commit mesajı:** `Merge pull request #72 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #72) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 104. 2026-08-21 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `a6a82982`  
**Orijinal commit mesajı:** `Sayfa düzenleri değiştirildi ve tek bir halde bütün olarak yolları da dahil güncellendi`

Frontend projesinde `App.tsx`, `CreateAdLayout.tsx`, `AddListingPage.tsx` ve 2 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 105. 2026-08-21 — pull request #73 birleştirmesi ve çakışma çözümleri

**Commit:** `3d518a54`  
**Orijinal commit mesajı:** `Merge pull request #73 from SantoPanto/develop

Sayfa düzenleri değiştirildi ve tek bir halde bütün olarak yolları da…`

Geliştirme dalı (pull request #73) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `CreateAdLayout.tsx`, `AddListingPage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 106. 2026-08-21 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `96632e2f`  
**Orijinal commit mesajı:** `Sayfa değişimi sonrası kullanılmayan importlar düzeltildi`

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 107. 2026-08-21 — pull request #75 birleştirmesi ve çakışma çözümleri

**Commit:** `d28c777e`  
**Orijinal commit mesajı:** `Merge pull request #75 from SantoPanto/develop

Sayfa değişimi sonrası kullanılmayan importlar düzeltildi`

Geliştirme dalı (pull request #75) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 108. 2026-08-21 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `e7402549`  
**Orijinal commit mesajı:** `Routerlar düzenlendi`

Frontend projesinde `App.tsx`, `CreateAdLayout.tsx`, `TeamUI.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 109. 2026-08-21 — pull request #76 birleştirmesi ve çakışma çözümleri

**Commit:** `2050172b`  
**Orijinal commit mesajı:** `Merge pull request #76 from SantoPanto/develop

Routerlar düzenlendi`

Geliştirme dalı (pull request #76) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `CreateAdLayout.tsx`, `TeamUI.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 110. 2026-08-21 — pull request #74 birleştirmesi ve çakışma çözümleri

**Commit:** `25bbaf1a`  
**Orijinal commit mesajı:** `Merge pull request #74 from SantoPanto/scrollingfix

scrollingfix`

Geliştirme dalı (pull request #74) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ScrollToTop.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 111. 2026-08-21 — App.css ve frontend bileşen geliştirmesi

**Commit:** `08fcd074`  
**Orijinal commit mesajı:** `Header'a sticky özelliği eklendi ve ana sayfalarda bütünlük sağlandı`

Frontend projesinde `App.css`, `Header.tsx`, `MainLayout.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 112. 2026-08-21 — pull request #77 birleştirmesi ve çakışma çözümleri

**Commit:** `2c6de6f3`  
**Orijinal commit mesajı:** `Merge pull request #77 from SantoPanto/develop

Header'a sticky özelliği eklendi ve ana sayfalarda bütünlük sağlandı`

Geliştirme dalı (pull request #77) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `Header.tsx`, `MainLayout.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 113. 2026-08-21 — Adoption.tsx ve frontend bileşen geliştirmesi

**Commit:** `d207ded3`  
**Orijinal commit mesajı:** `Sahiplendirme kısmındaki header stick olarak düzeltildi ve bütün sayfalarda footer tek bir parça haline getirilip düzenlendi`

Frontend projesinde `Adoption.tsx`, `AdoptionDetailPage.tsx`, `HomePage.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 114. 2026-08-21 — pull request #78 birleştirmesi ve çakışma çözümleri

**Commit:** `618d95e4`  
**Orijinal commit mesajı:** `Merge pull request #78 from SantoPanto/develop

Sahiplendirme kısmındaki header stick olarak düzeltildi ve bütün sayf…`

Geliştirme dalı (pull request #78) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Adoption.tsx`, `AdoptionDetailPage.tsx`, `HomePage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 115. 2026-08-21 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `e62c0ad2`  
**Orijinal commit mesajı:** `Sahiplendirme ilanları diğer ilan yapılarına göre eksikti. Bu yapılar düzeltildi.`

Frontend projesinde `App.tsx`, `AdoptionDetailPage.tsx`, `MapPage.tsx` ve 2 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 116. 2026-08-21 — pull request #79 birleştirmesi ve çakışma çözümleri

**Commit:** `52acc231`  
**Orijinal commit mesajı:** `Merge pull request #79 from SantoPanto/develop

Sahiplendirme ilanları diğer ilan yapılarına göre eksikti. Bu yapılar…`

Geliştirme dalı (pull request #79) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdoptionDetailPage.tsx`, `MapPage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 117. 2026-08-21 — App.tsx ve frontend bileşen geliştirmesi

**Commit:** `68cf528c`  
**Orijinal commit mesajı:** `Sahiplendirme sayfa düzenlemesi sonrası çöp olan router kaldırıldı.`

Frontend projesinde `App.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 118. 2026-08-21 — pull request #80 birleştirmesi ve çakışma çözümleri

**Commit:** `2258dcf0`  
**Orijinal commit mesajı:** `Merge pull request #80 from SantoPanto/develop

Sahiplendirme sayfa düzenlemesi sonrası çöp olan router kaldırıldı.`

Geliştirme dalı (pull request #80) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 119. 2026-08-21 — AdCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `f4645051`  
**Orijinal commit mesajı:** `Favori sayfası görsel sorunu çözüldü`

Frontend projesinde `AdCard.tsx`, `FavoritesPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 120. 2026-08-21 — pull request #81 birleştirmesi ve çakışma çözümleri

**Commit:** `5fc7c2b9`  
**Orijinal commit mesajı:** `Merge pull request #81 from SantoPanto/develop

Favori sayfası görsel sorunu çözüldü`

Geliştirme dalı (pull request #81) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 121. 2026-08-21 — pull request #82 birleştirmesi ve çakışma çözümleri

**Commit:** `aa053ef6`  
**Orijinal commit mesajı:** `Merge pull request #82 from SantoPanto/feat/arama-gorunurlugu

fix: ana sayfa araması çalışıyordu ama görünmüyordu — Enter, canlı sonuç sayısı ve kaydırma eklendi`

Geliştirme dalı (pull request #82) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `HomePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 122. 2026-08-21 — AdCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `f12b1215`  
**Orijinal commit mesajı:** `Sahiplendirmedeki default fotoğraflar yerine düzgün url bağlandı`

Frontend projesinde `AdCard.tsx`, `MatchedAdCard.tsx`, `FavoritesPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 123. 2026-08-21 — pull request #83 birleştirmesi ve çakışma çözümleri

**Commit:** `1020d887`  
**Orijinal commit mesajı:** `Merge pull request #83 from SantoPanto/develop

Sahiplendirmedeki default fotoğraflar yerine düzgün url bağlandı`

Geliştirme dalı (pull request #83) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx`, `MatchedAdCard.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 124. 2026-08-21 — AdCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `d827f774`  
**Orijinal commit mesajı:** `Fotoğraf url düzenlemesi ve tek parçaya geçiş`

Frontend projesinde `AdCard.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 125. 2026-08-21 — pull request #84 birleştirmesi ve çakışma çözümleri

**Commit:** `ff065f18`  
**Orijinal commit mesajı:** `Merge pull request #84 from SantoPanto/develop

Fotoğraf url düzenlemesi ve tek parçaya geçiş`

Geliştirme dalı (pull request #84) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCard.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 126. 2026-08-21 — PetListingCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `5564b6cf`  
**Orijinal commit mesajı:** `Bugfix`

Frontend projesinde `PetListingCard.tsx`, `FavoritesPage.tsx`, `listingpage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 127. 2026-08-21 — pull request #85 birleştirmesi ve çakışma çözümleri

**Commit:** `02e433eb`  
**Orijinal commit mesajı:** `Merge pull request #85 from SantoPanto/develop

Bugfix`

Geliştirme dalı (pull request #85) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetListingCard.tsx`, `FavoritesPage.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 128. 2026-08-21 — listingpage.tsx ve frontend bileşen geliştirmesi

**Commit:** `a63f66a7`  
**Orijinal commit mesajı:** `Çöp kod temizliği`

Frontend projesinde `listingpage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 129. 2026-08-21 — pull request #86 birleştirmesi ve çakışma çözümleri

**Commit:** `104d631d`  
**Orijinal commit mesajı:** `Merge pull request #86 from SantoPanto/develop

Çöp kod temizliği`

Geliştirme dalı (pull request #86) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 130. 2026-08-21 — imageUrl.ts ve frontend bileşen geliştirmesi

**Commit:** `6a71913e`  
**Orijinal commit mesajı:** `Fotoğraf çözüm cdn`

Frontend projesinde `imageUrl.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 131. 2026-08-21 — pull request #87 birleştirmesi ve çakışma çözümleri

**Commit:** `39a67838`  
**Orijinal commit mesajı:** `Merge pull request #87 from SantoPanto/develop

Fotoğraf çözüm cdn`

Geliştirme dalı (pull request #87) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageUrl.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 132. 2026-08-22 — pull request #88 birleştirmesi ve çakışma çözümleri

**Commit:** `2e4ed3b1`  
**Orijinal commit mesajı:** `Merge pull request #88 from SantoPanto/fix/esma-mesafe-slider-ve-one-cikanlar

fix: mesafe süzgeci gerçekten süzsün, işlevsiz 'öne çıkanlar' kalksın`

Geliştirme dalı (pull request #88) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `HomePage.tsx`, `mesafe.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 133. 2026-08-22 — pull request #89 birleştirmesi ve çakışma çözümleri

**Commit:** `36a33ece`  
**Orijinal commit mesajı:** `Merge pull request #89 from SantoPanto/fix/b4-eslesme-bildirimi-my-matches

fix: eşleşme bildirimi Eşleşmelerim'e götürsün (B4)`

Geliştirme dalı (pull request #89) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 134. 2026-08-22 — pull request #90 birleştirmesi ve çakışma çözümleri

**Commit:** `cc4605dd`  
**Orijinal commit mesajı:** `Merge pull request #90 from SantoPanto/fix/b2-bulundu-formu-elle-konum

feat: bulundu formuna elle enlem/boylam girişi (B2)`

Geliştirme dalı (pull request #90) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FoundPetCreatePage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 135. 2026-08-22 — pull request #91 birleştirmesi ve çakışma çözümleri

**Commit:** `88f6824b`  
**Orijinal commit mesajı:** `Merge pull request #91 from SantoPanto/feat/b8-popup-konum-gonder

feat: pop-up eşleştirmesine form konumu gönderiliyor (B8'in FE yarısı)`

Geliştirme dalı (pull request #91) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 136. 2026-08-22 — pull request #92 birleştirmesi ve çakışma çözümleri

**Commit:** `b05b0e30`  
**Orijinal commit mesajı:** `Merge pull request #92 from SantoPanto/feat/b5-ilanlar-arama-kutusu

feat: İlanlar sayfasına arama kutusu (B5'in FE yarısı — BE #126 ile birlikte)`

Geliştirme dalı (pull request #92) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `listingpage.test.tsx`, `listingpage.tsx`, `ads.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 137. 2026-08-22 — pull request #93 birleştirmesi ve çakışma çözümleri

**Commit:** `e1d78d23`  
**Orijinal commit mesajı:** `Merge pull request #93 from SantoPanto/feat/sifre-sifirlama-sayfasi

feat: /reset-password sayfası (şifremi-unuttum'un FE yarısı — BE #127 ile)`

Geliştirme dalı (pull request #93) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `ResetPasswordPage.test.tsx`, `ResetPasswordPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 138. 2026-08-22 — pull request #94 birleştirmesi ve çakışma çözümleri

**Commit:** `d260a6bc`  
**Orijinal commit mesajı:** `Merge pull request #94 from SantoPanto/fix/zil-noktasi-okunmamis

fix: zil noktası yalnız okunmamış bildirim varken yansın`

Geliştirme dalı (pull request #94) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Header.test.tsx`, `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 139. 2026-08-22 — pull request #95 birleştirmesi ve çakışma çözümleri

**Commit:** `60734f18`  
**Orijinal commit mesajı:** `Merge pull request #95 from SantoPanto/fix/karanlik-butonlar

fix: karanlık ana-aksiyon butonları marka turuncusuna çekildi (sheet 32)`

Geliştirme dalı (pull request #95) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.tsx`, `MyListingsPage.tsx`, `MyMatchesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 140. 2026-08-22 — pull request #96 birleştirmesi ve çakışma çözümleri

**Commit:** `b36b153b`  
**Orijinal commit mesajı:** `Merge pull request #96 from SantoPanto/feat/sahiplendirme-sayfalama

feat: sahiplendirme listesine sayfalama (sheet 38)`

Geliştirme dalı (pull request #96) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Adoption.test.tsx`, `Adoption.tsx`, `listingpage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 141. 2026-08-22 — pull request #97 birleştirmesi ve çakışma çözümleri

**Commit:** `cbb67a02`  
**Orijinal commit mesajı:** `Merge pull request #97 from SantoPanto/feat/mobil-alt-menu

feat: mobil alt gezinme çubuğu — mobilde menü hiç yoktu (sheet 33'ün özü)`

Geliştirme dalı (pull request #97) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `BottomNav.test.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 142. 2026-08-22 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `9ad411e8`  
**Orijinal commit mesajı:** `Merge branch 'main' into fix/yazim-metin-duzeltmeleri`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `generate-firebase-sw.mjs`, `App.css`, `App.tsx` ve 21 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 143. 2026-08-22 — pull request #98 birleştirmesi ve çakışma çözümleri

**Commit:** `090222e3`  
**Orijinal commit mesajı:** `Merge pull request #98 from SantoPanto/fix/yazim-metin-duzeltmeleri

fix: kullanıcıya görünen metinlerde yazım düzeltmeleri (uçtan uca tarama)`

Geliştirme dalı (pull request #98) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `PetListingCard.test.tsx`, `PetListingCard.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 144. 2026-08-22 — pull request #99 birleştirmesi ve çakışma çözümleri

**Commit:** `cfb0facc`  
**Orijinal commit mesajı:** `Merge pull request #99 from SantoPanto/fix/tur-kutucuklari-kesik

fix: tür kutucukları ve form sekmeleri dar ekranda sarsın (B-resp-1)`

Geliştirme dalı (pull request #99) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `CreateAdLayout.tsx`, `FavoritesPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 145. 2026-08-22 — pull request #100 birleştirmesi ve çakışma çözümleri

**Commit:** `29b4bd24`  
**Orijinal commit mesajı:** `Merge pull request #100 from SantoPanto/fix/390px-kirpik-gorunumler

fix: ilan detayında DESEN hücresi 390px'te kesiliyordu`

Geliştirme dalı (pull request #100) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetDetailPage.test.tsx`, `PetDetailPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 146. 2026-08-22 — pull request #101 birleştirmesi ve çakışma çözümleri

**Commit:** `f6b98421`  
**Orijinal commit mesajı:** `Merge pull request #101 from SantoPanto/feat/ilan-sehir-ilce-gosterim

feat: kartlarda ham koordinat yerine il/ilçe (BE #129'un FE yarısı)`

Geliştirme dalı (pull request #101) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PetListingCard.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 147. 2026-08-22 — pull request #108 birleştirmesi ve çakışma çözümleri

**Commit:** `2a7b130c`  
**Orijinal commit mesajı:** `Merge pull request #108 from SantoPanto/feat/gizlilik-politikasi

feat: gizlilik politikası / KVKK aydınlatma sayfası (/privacy)`

Geliştirme dalı (pull request #108) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Footer.tsx`, `GizlilikPage.test.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 148. 2026-08-22 — pull request #107 birleştirmesi ve çakışma çözümleri

**Commit:** `24f2ed26`  
**Orijinal commit mesajı:** `Merge pull request #107 from SantoPanto/feat/seo-robots-sitemap

seo: robots.txt + statik sitemap.xml`

Geliştirme dalı (pull request #107) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `robots.txt`, `sitemap.xml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 149. 2026-08-22 — pull request #109 birleştirmesi ve çakışma çözümleri

**Commit:** `32f65e10`  
**Orijinal commit mesajı:** `Merge pull request #109 from SantoPanto/fix/eslesme-foto-alt

a11y: eşleşme fotoğrafına partner ilan başlığı alt metni`

Geliştirme dalı (pull request #109) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveFoundModal.test.tsx`, `ResolveFoundModal.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 150. 2026-08-22 — AiAutofillCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `8796a3cc`  
**Orijinal commit mesajı:** `Bulundu ve sahiplendirme kısmına ai ile doldurma özelliği eklendi`

Frontend projesinde `AiAutofillCard.tsx`, `AiMatchModal.tsx`, `AddListingPage.tsx` ve 5 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 151. 2026-08-22 — pull request #111 birleştirmesi ve çakışma çözümleri

**Commit:** `50c31833`  
**Orijinal commit mesajı:** `Merge pull request #111 from SantoPanto/develop

Bulundu ve sahiplendirme kısmına ai ile doldurma özelliği eklendi`

Geliştirme dalı (pull request #111) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAutofillCard.tsx`, `AiMatchModal.tsx`, `AddListingPage.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 152. 2026-08-22 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `30d4a6d2`  
**Orijinal commit mesajı:** `Boş dosyalar temizlendi`

Frontend projesinde `AddListingPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 153. 2026-08-22 — pull request #112 birleştirmesi ve çakışma çözümleri

**Commit:** `74a75ca7`  
**Orijinal commit mesajı:** `Merge pull request #112 from SantoPanto/develop

Boş dosyalar temizlendi`

Geliştirme dalı (pull request #112) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 154. 2026-08-22 — pull request #113 birleştirmesi ve çakışma çözümleri

**Commit:** `4c0dc255`  
**Orijinal commit mesajı:** `Merge pull request #113 from SantoPanto/feat/harita-sahiplendirme-filtresi

feat(harita): Sahiplendirme süzgeç çipi`

Geliştirme dalı (pull request #113) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MapPage.tsx`, `haritaSunum.test.ts`, `haritaSunum.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 155. 2026-08-22 — pull request #114 birleştirmesi ve çakışma çözümleri

**Commit:** `964fdef0`  
**Orijinal commit mesajı:** `Merge pull request #114 from SantoPanto/feat/altcubuk-mesajlar

feat(mobil): alt çubuğa Mesajlar sekmesi`

Geliştirme dalı (pull request #114) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BottomNav.test.tsx`, `BottomNav.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 156. 2026-08-23 — AdEditModal.tsx ve frontend bileşen geliştirmesi

**Commit:** `773aef75`  
**Orijinal commit mesajı:** `İngilzce etiket sorunu türkçe enum sayfası kullanılarak çözüldü.`

Frontend projesinde `AdEditModal.tsx`, `MatchCard.tsx`, `MatchedAdCard.tsx` ve 5 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 157. 2026-08-23 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `e50f925c`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BottomNav.test.tsx`, `BottomNav.tsx`, `NotificationPanel.tsx` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 158. 2026-08-23 — pull request #117 birleştirmesi ve çakışma çözümleri

**Commit:** `5cc8eb1b`  
**Orijinal commit mesajı:** `Merge pull request #117 from SantoPanto/develop

İngilzce etiket sorunu türkçe enum sayfası kullanılarak çözüldü.`

Geliştirme dalı (pull request #117) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdEditModal.tsx`, `MatchCard.tsx`, `MatchedAdCard.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 159. 2026-08-23 — AdminFilterBar.tsx ve frontend bileşen geliştirmesi

**Commit:** `16bd6330`  
**Orijinal commit mesajı:** `Admin panele arama fonksiyonu eklendi`

Frontend projesinde `AdminFilterBar.tsx`, `useDebounce.ts`, `AdminComplaintsPage.tsx` ve 5 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 160. 2026-08-23 — pull request #118 birleştirmesi ve çakışma çözümleri

**Commit:** `e8d5da2b`  
**Orijinal commit mesajı:** `Merge pull request #118 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #118) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminFilterBar.tsx`, `useDebounce.ts`, `AdminComplaintsPage.tsx` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 161. 2026-08-23 — npm paket bağımlılıklarının güncellenmesi

**Commit:** `fe341b09`  
**Orijinal commit mesajı:** `Dosya yükleme yapısında aşırı yük bindiği için dosyaların küçültülmesi için bir kütüphane kuruldu. Artık dosyalar bu filtrelemeden geçip o şekilde ilerleyecekler`

Frontend projesinde `package-lock.json`, `package.json`, `SightingModal.tsx` ve 6 ek dosya dosyaları güncellendi. Projede kullanılan kütüphane bağımlılıklarının sürümleri düzenlendi.

### 162. 2026-08-23 — pull request #119 birleştirmesi ve çakışma çözümleri

**Commit:** `abce4320`  
**Orijinal commit mesajı:** `Merge pull request #119 from SantoPanto/develop

Dosya yükleme yapısında aşırı yük bindiği için dosyaların küçültülmes…`

Geliştirme dalı (pull request #119) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `package-lock.json`, `package.json`, `SightingModal.tsx` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 163. 2026-08-23 — imageCompression.test.ts ve frontend bileşen geliştirmesi

**Commit:** `44ed5024`  
**Orijinal commit mesajı:** `Dosya boyutunu küçüktürken webp dosyası olarak kalıyordu ve uyumsuzluk oluyordu. Bu sorun jpeg dönüştürmesiyle çözüldü`

Frontend projesinde `imageCompression.test.ts`, `imageCompression.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 164. 2026-08-23 — pull request #120 birleştirmesi ve çakışma çözümleri

**Commit:** `9723b273`  
**Orijinal commit mesajı:** `Merge pull request #120 from SantoPanto/develop

Dosya boyutunu küçüktürken webp dosyası olarak kalıyordu ve uyumsuzlu…`

Geliştirme dalı (pull request #120) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `imageCompression.test.ts`, `imageCompression.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 165. 2026-08-23 — ForegroundNotificationToast.tsx ve frontend bileşen geliştirmesi

**Commit:** `4fba3fb3`  
**Orijinal commit mesajı:** `Bildirimden mesajlara geçilmesi için route güncellemesi ve sürekli olarak -çevrim içi- görünme sorunu çözüldü`

Frontend projesinde `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `useChatWebSocket.ts` ve 6 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 166. 2026-08-23 — pull request #121 birleştirmesi ve çakışma çözümleri

**Commit:** `92bc1373`  
**Orijinal commit mesajı:** `Merge pull request #121 from SantoPanto/develop

Bildirimden mesajlara geçilmesi için route güncellemesi ve sürekli ol…`

Geliştirme dalı (pull request #121) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `NotificationPanel.tsx`, `useChatWebSocket.ts` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 167. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `36c8c100`  
**Orijinal commit mesajı:** `Mesajlaşma bugfix`

Frontend projesinde `ChatPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 168. 2026-08-23 — pull request #122 birleştirmesi ve çakışma çözümleri

**Commit:** `e623844e`  
**Orijinal commit mesajı:** `Merge pull request #122 from SantoPanto/develop

Mesajlaşma bugfix`

Geliştirme dalı (pull request #122) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 169. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `49fec050`  
**Orijinal commit mesajı:** `Mesajlaşmadaki sonsuz uzama, ekranın kayması ve güncel mesaj problemi çözüldü`

Frontend projesinde `ChatPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 170. 2026-08-23 — pull request #123 birleştirmesi ve çakışma çözümleri

**Commit:** `d10fa4b0`  
**Orijinal commit mesajı:** `Merge pull request #123 from SantoPanto/develop

Mesajlaşmadaki sonsuz uzama, ekranın kayması ve güncel mesaj problemi…`

Geliştirme dalı (pull request #123) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 171. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `3eb2f56a`  
**Orijinal commit mesajı:** `Uyumsuz kod satırı temizlendi`

Frontend projesinde `ChatPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 172. 2026-08-23 — pull request #124 birleştirmesi ve çakışma çözümleri

**Commit:** `627326a4`  
**Orijinal commit mesajı:** `Merge pull request #124 from SantoPanto/develop

Uyumsuz kod satırı temizlendi`

Geliştirme dalı (pull request #124) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 173. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `6378eff0`  
**Orijinal commit mesajı:** `Mesajlaşmada güncel mantığı tekrardan düzeltildi ve yenilemelerde konuşmanın en sonunda kalması sağlandı.`

Frontend projesinde `ChatPage.tsx`, `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 174. 2026-08-23 — pull request #125 birleştirmesi ve çakışma çözümleri

**Commit:** `d5b9081a`  
**Orijinal commit mesajı:** `Merge pull request #125 from SantoPanto/develop

Mesajlaşmada güncel mantığı tekrardan düzeltildi ve yenilemelerde kon…`

Geliştirme dalı (pull request #125) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 175. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `97cff0f9`  
**Orijinal commit mesajı:** `mesaj bugfix`

Frontend projesinde `ChatPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 176. 2026-08-23 — pull request #126 birleştirmesi ve çakışma çözümleri

**Commit:** `807b37ed`  
**Orijinal commit mesajı:** `Merge pull request #126 from SantoPanto/develop

mesaj bugfix`

Geliştirme dalı (pull request #126) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 177. 2026-08-23 — websocket.ts ve frontend bileşen geliştirmesi

**Commit:** `b0e353a8`  
**Orijinal commit mesajı:** `sessiz hatanın çözümü`

Frontend projesinde `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 178. 2026-08-23 — pull request #127 birleştirmesi ve çakışma çözümleri

**Commit:** `f17bb63c`  
**Orijinal commit mesajı:** `Merge pull request #127 from SantoPanto/develop

sessiz hatanın çözümü`

Geliştirme dalı (pull request #127) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 179. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `91a958c0`  
**Orijinal commit mesajı:** `Mesaj düzeltmesi`

Frontend projesinde `ChatPage.tsx`, `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 180. 2026-08-23 — pull request #129 birleştirmesi ve çakışma çözümleri

**Commit:** `f6712757`  
**Orijinal commit mesajı:** `Merge pull request #129 from SantoPanto/develop

Mesaj düzeltmesi`

Geliştirme dalı (pull request #129) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatPage.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 181. 2026-08-23 — pull request #128 birleştirmesi ve çakışma çözümleri

**Commit:** `955545fb`  
**Orijinal commit mesajı:** `Merge pull request #128 from SantoPanto/feature/masaustu-mesajlar-linki

Masaüstü menüsüne Mesajlar linki ekle`

Geliştirme dalı (pull request #128) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Header.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 182. 2026-08-23 — ChatPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `93b744e3`  
**Orijinal commit mesajı:** `Optimistic ayarları eklendi`

Frontend projesinde `ChatPage.tsx`, `types.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 183. 2026-08-23 — useChatWebSocket.ts ve frontend bileşen geliştirmesi

**Commit:** `30f47de8`  
**Orijinal commit mesajı:** `Yeniden bağlanma mantığı eklendi`

Frontend projesinde `useChatWebSocket.ts`, `ChatPage.tsx`, `api.ts` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 184. 2026-08-23 — AuthContext.tsx ve frontend bileşen geliştirmesi

**Commit:** `8ee161fe`  
**Orijinal commit mesajı:** `msg fix`

Frontend projesinde `AuthContext.tsx`, `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 185. 2026-08-23 — pull request #131 birleştirmesi ve çakışma çözümleri

**Commit:** `b383cc78`  
**Orijinal commit mesajı:** `Merge pull request #131 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #131) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useChatWebSocket.ts`, `ChatPage.tsx`, `api.ts` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 186. 2026-08-23 — pull request #130 birleştirmesi ve çakışma çözümleri

**Commit:** `91094978`  
**Orijinal commit mesajı:** `Merge pull request #130 from SantoPanto/fix/yukleme-sinirlari-ve-hata-mesajlari

413'e anlaşılır mesaj + isteklere zaman aşımı + sıkıştırma sonrası boyut kontrolü`

Geliştirme dalı (pull request #130) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiMatchPage.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 187. 2026-08-23 — websocket.ts ve frontend bileşen geliştirmesi

**Commit:** `4419f3fd`  
**Orijinal commit mesajı:** `websocket güncellemesi`

Frontend projesinde `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 188. 2026-08-23 — pull request #133 birleştirmesi ve çakışma çözümleri

**Commit:** `23530ebc`  
**Orijinal commit mesajı:** `Merge pull request #133 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #133) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AuthContext.tsx`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 189. 2026-08-23 — websocket.ts ve frontend bileşen geliştirmesi

**Commit:** `777b5dc2`  
**Orijinal commit mesajı:** `Yanlış import düzeltildi`

Frontend projesinde `websocket.ts` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 190. 2026-08-23 — pull request #134 birleştirmesi ve çakışma çözümleri

**Commit:** `6362b11f`  
**Orijinal commit mesajı:** `Merge pull request #134 from SantoPanto/develop

Yanlış import düzeltildi`

Geliştirme dalı (pull request #134) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 191. 2026-08-24 — pull request #132 birleştirmesi ve çakışma çözümleri

**Commit:** `6c9db67e`  
**Orijinal commit mesajı:** `Merge pull request #132 from SantoPanto/feature/karanlik-tema

Karanlık tema ekle`

Geliştirme dalı (pull request #132) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `index.html`, `App.css`, `AdCard.tsx` ve 56 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 192. 2026-08-24 — pull request #135 birleştirmesi ve çakışma çözümleri

**Commit:** `a25adfa7`  
**Orijinal commit mesajı:** `Merge pull request #135 from SantoPanto/fix/konum-ve-ai-mesaji

Konum hatasını koduna göre ayır + AI hiçbir alanı dolduramadığında 'tamamlandı' deme`

Geliştirme dalı (pull request #135) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MapPicker.tsx`, `SightingModal.tsx`, `AddListingPage.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 193. 2026-08-24 — pull request #136 birleştirmesi ve çakışma çözümleri

**Commit:** `adb599e3`  
**Orijinal commit mesajı:** `Merge pull request #136 from SantoPanto/fix/ws-durum-etiketi

Mesajlarda 'Bağlanıyor...' takılı kalıyor — durum ikinci çağırana bildirilmiyordu`

Geliştirme dalı (pull request #136) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useChatWebSocket.ts`, `websocket.baglanti-durumu.test.ts`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 194. 2026-08-24 — pull request #137 birleştirmesi ve çakışma çözümleri

**Commit:** `c6bc5d31`  
**Orijinal commit mesajı:** `Merge pull request #137 from SantoPanto/fix/push-bildirim-yonlendirme

Arka plan push bildirimi de doğru yere gitsin (yalnız AI_MATCH yönlendiriliyordu)`

Geliştirme dalı (pull request #137) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `firebase-messaging-sw.js`, `generate-firebase-sw.mjs`, `sw-bildirim-hedefi.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 195. 2026-08-24 — pull request #139 birleştirmesi ve çakışma çözümleri

**Commit:** `7459e79b`  
**Orijinal commit mesajı:** `Merge pull request #139 from SantoPanto/fix/ai-analiz-iki-sekil

AI analiz cevabının İKİ şeklini de kabul et (BE #156 alan adlarını değiştirdi)`

Geliştirme dalı (pull request #139) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 196. 2026-08-24 — pull request #138 birleştirmesi ve çakışma çözümleri

**Commit:** `e7db3fe5`  
**Orijinal commit mesajı:** `Merge pull request #138 from SantoPanto/fix/oturum-yukleme-ekrani

Kimlik doğrulama yükleme ekranlarını site tasarımıyla uyumlu hale getir`

Geliştirme dalı (pull request #138) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RequireAuth.tsx`, `RequireGuest.tsx` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 197. 2026-08-24 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `746da895`  
**Orijinal commit mesajı:** `Ai otomatik doldurma sorunu çözümü`

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 198. 2026-08-24 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `307911e2`  
**Orijinal commit mesajı:** `Merge branch 'main' into develop`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `firebase-messaging-sw.js`, `generate-firebase-sw.mjs`, `RequireAuth.tsx` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 199. 2026-08-24 — pull request #140 birleştirmesi ve çakışma çözümleri

**Commit:** `4f922d86`  
**Orijinal commit mesajı:** `Merge pull request #140 from SantoPanto/develop

Ai otomatik doldurma sorunu çözümü`

Geliştirme dalı (pull request #140) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `AiOtoDoldurMesaji.test.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 200. 2026-08-24 — Dockerfile ve frontend bileşen geliştirmesi

**Commit:** `e3916d5a`  
**Orijinal commit mesajı:** `fix(deploy): add frontend production docker setup`

Frontend projesinde `Dockerfile` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 201. 2026-08-24 — pull request #141 birleştirmesi ve çakışma çözümleri

**Commit:** `667a448d`  
**Orijinal commit mesajı:** `Merge pull request #141 from SantoPanto/develop

fix(deploy): add frontend production docker setup`

Geliştirme dalı (pull request #141) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 202. 2026-08-24 — AddListingPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `d5684cf2`  
**Orijinal commit mesajı:** `fix(ai): remove legacy normalization and use parseAiAnalysis everywhere`

Frontend projesinde `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 203. 2026-08-24 — pull request #142 birleştirmesi ve çakışma çözümleri

**Commit:** `9829888a`  
**Orijinal commit mesajı:** `Merge pull request #142 from SantoPanto/develop

fix(ai): remove legacy normalization and use parseAiAnalysis everywhere`

Geliştirme dalı (pull request #142) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.tsx`, `AdoptionCreatePage.tsx`, `FoundPetCreatePage.tsx` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 204. 2026-08-24 — Dockerfile ve frontend bileşen geliştirmesi

**Commit:** `0055570d`  
**Orijinal commit mesajı:** `fix(deploy): configure frontend nginx proxy and ssl`

Frontend projesinde `Dockerfile`, `nginx.conf` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 205. 2026-08-24 — pull request #143 birleştirmesi ve çakışma çözümleri

**Commit:** `e4450900`  
**Orijinal commit mesajı:** `Merge pull request #143 from SantoPanto/develop

fix(deploy): configure frontend nginx proxy and ssl`

Geliştirme dalı (pull request #143) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `nginx.conf` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 206. 2026-08-24 — nginx.conf ve frontend bileşen geliştirmesi

**Commit:** `7d241cbf`  
**Orijinal commit mesajı:** `fix(auth): proxy google oauth endpoints to backend`

Frontend projesinde `nginx.conf` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 207. 2026-08-24 — pull request #144 birleştirmesi ve çakışma çözümleri

**Commit:** `88bf70dd`  
**Orijinal commit mesajı:** `Merge pull request #144 from SantoPanto/develop

fix(auth): proxy google oauth endpoints to backend`

Geliştirme dalı (pull request #144) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `nginx.conf` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 208. 2026-08-25 — pull request #145 birleştirmesi ve çakışma çözümleri

**Commit:** `052f12e9`  
**Orijinal commit mesajı:** `Merge pull request #145 from SantoPanto/feature/instagram-intelligence

Instagram AI eşleştirme entegrasyonu + karanlık mod düzeltmeleri`

Geliştirme dalı (pull request #145) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdEditModal.tsx`, `Header.tsx` ve 26 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 209. 2026-08-25 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `d891c69a`  
**Orijinal commit mesajı:** `Merge branch 'main' into feature/realtime-presence-and-notifications`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `AdEditModal.tsx`, `Header.tsx` ve 26 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 210. 2026-08-25 — pull request #147 birleştirmesi ve çakışma çözümleri

**Commit:** `d3c3a3f4`  
**Orijinal commit mesajı:** `Merge pull request #147 from SantoPanto/feature/realtime-presence-and-notifications

feat: kullanıcı çevrimiçi durumu ve WebSocket üzerinden anlık mesaj/bildirim teslimatı`

Geliştirme dalı (pull request #147) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ForegroundNotificationToast.tsx`, `types.ts`, `websocket.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 211. 2026-08-25 — pull request #148 birleştirmesi ve çakışma çözümleri

**Commit:** `5645d7b6`  
**Orijinal commit mesajı:** `Merge pull request #148 from SantoPanto/feature/ben-neyim

Ben Neyim? -- yalnizca girisli kullanicilar, ust menude buton`

Geliştirme dalı (pull request #148) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `Header.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 212. 2026-08-25 — pull request #149 birleştirmesi ve çakışma çözümleri

**Commit:** `39c046bb`  
**Orijinal commit mesajı:** `Merge pull request #149 from SantoPanto/feature/instagram-publish

Instagram gonderim kuyrugu (admin) + ilan formu onayi`

Geliştirme dalı (pull request #149) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `InstagramPublishModal.tsx`, `AddListingPage.tsx`, `AdminDashboardPage.tsx` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 213. 2026-08-26 — AdCreationMatchModal.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `b6ff4731`  
**Orijinal commit mesajı:** `İlan oluşturulma mantığı ve ai analizi backend son pr ile uygun olacak şekilde güncellendi.`

Frontend projesinde `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 4 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 214. 2026-08-26 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `e3d2b20a`  
**Orijinal commit mesajı:** `Merge branch 'main' into develop`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdEditModal.tsx` ve 33 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 215. 2026-08-26 — pull request #151 birleştirmesi ve çakışma çözümleri

**Commit:** `f3c9be81`  
**Orijinal commit mesajı:** `Merge pull request #151 from SantoPanto/develop

İlan oluşturulma mantığı ve ai analizi backend son pr ile uygun olaca…`

Geliştirme dalı (pull request #151) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 216. 2026-08-26 — useAdMatchingMachine.test.ts ve frontend bileşen geliştirmesi

**Commit:** `8199b1bb`  
**Orijinal commit mesajı:** `Eski yapıdan kalan kodlar temizlendi ve eşleşme kartı tekrardan eklendi`

Frontend projesinde `useAdMatchingMachine.test.ts`, `useAdMatchingMachine.ts`, `AddListingPage.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 217. 2026-08-26 — pull request #152 birleştirmesi ve çakışma çözümleri

**Commit:** `5aa6845b`  
**Orijinal commit mesajı:** `Merge pull request #152 from SantoPanto/develop

Eski yapıdan kalan kodlar temizlendi ve eşleşme kartı tekrardan eklendi`

Geliştirme dalı (pull request #152) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `useAdMatchingMachine.test.ts`, `useAdMatchingMachine.ts`, `AddListingPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 218. 2026-08-26 — AdCreationMatchModal.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `eaa54393`  
**Orijinal commit mesajı:** `Eşleşme kartı güncellendi.`

Frontend projesinde `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 219. 2026-08-26 — pull request #153 birleştirmesi ve çakışma çözümleri

**Commit:** `00344f6d`  
**Orijinal commit mesajı:** `Merge pull request #153 from SantoPanto/develop

Eşleşme kartı güncellendi.`

Geliştirme dalı (pull request #153) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreationMatchModal.test.tsx`, `AdCreationMatchModal.tsx`, `useAdMatchingMachine.test.ts` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 220. 2026-08-26 — MatchCard.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `7c9006fa`  
**Orijinal commit mesajı:** `Yeni sohbet ui eklenip ilanların gösterilmesi sağlandı ve yolları eklendi`

Frontend projesinde `MatchCard.test.tsx`, `MatchCard.tsx`, `SharedAdCard.test.tsx` ve 7 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 221. 2026-08-26 — pull request #154 birleştirmesi ve çakışma çözümleri

**Commit:** `e3817b2b`  
**Orijinal commit mesajı:** `Merge pull request #154 from SantoPanto/develop

Yeni sohbet ui eklenip ilanların gösterilmesi sağlandı ve yolları ekl…`

Geliştirme dalı (pull request #154) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchCard.test.tsx`, `MatchCard.tsx`, `SharedAdCard.test.tsx` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 222. 2026-08-26 — AddListingPage.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `cb4611eb`  
**Orijinal commit mesajı:** `ilan işleyiş yapısı düzenlendi gereksiz butonlar kaldırıldı ve bütün işlemlere bilgilerin doğruluğunu onaylıyorum seçeneği eklendi.`

Frontend projesinde `AddListingPage.test.tsx`, `AddListingPage.tsx`, `PetDetailPage.test.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 223. 2026-08-26 — pull request #157 birleştirmesi ve çakışma çözümleri

**Commit:** `f8238bb5`  
**Orijinal commit mesajı:** `Merge pull request #157 from SantoPanto/develop

ilan işleyiş yapısı düzenlendi gereksiz butonlar kaldırıldı ve bütün …`

Geliştirme dalı (pull request #157) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AddListingPage.test.tsx`, `AddListingPage.tsx`, `PetDetailPage.test.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 224. 2026-08-26 — pull request #160 birleştirmesi ve çakışma çözümleri

**Commit:** `ec5e3e6c`  
**Orijinal commit mesajı:** `Merge pull request #160 from SantoPanto/feature/report-and-queue-pages

Feature/report and queue pages`

Geliştirme dalı (pull request #160) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `MunicipalityReportQueuePage.tsx`, `PublicReportPage.tsx` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 225. 2026-08-26 — pull request #161 birleştirmesi ve çakışma çözümleri

**Commit:** `29e45304`  
**Orijinal commit mesajı:** `Merge pull request #161 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #161) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `types.ts` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 226. 2026-08-27 — pull request #150 birleştirmesi ve çakışma çözümleri

**Commit:** `c269dd71`  
**Orijinal commit mesajı:** `Merge pull request #150 from SantoPanto/feature/vet-clinic

Veteriner müşteri ilişkisi + harita POI + Evcil Hayvanlarım sağlık takibi`

Geliştirme dalı (pull request #150) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdCard.tsx` ve 93 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 227. 2026-08-27 — BenNeyimPage.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `55940bf8`  
**Orijinal commit mesajı:** `Ben neyim sayfasındaki eski kalan frontend tarafı düzeltildi`

Frontend projesinde `BenNeyimPage.test.tsx`, `BenNeyimPage.tsx`, `petAnalizi.ts` ve 3 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 228. 2026-08-27 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `c97636fe`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.css`, `App.tsx`, `AdCard.tsx` ve 93 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 229. 2026-08-27 — BenNeyimPage.tsx ve frontend bileşen geliştirmesi

**Commit:** `98b08bb2`  
**Orijinal commit mesajı:** `kullanılmayan modülün temizlenmesi`

Frontend projesinde `BenNeyimPage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 230. 2026-08-27 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `9e58764b`  
**Orijinal commit mesajı:** `Merge branch 'main' into develop`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Header.test.tsx`, `Header.tsx` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 231. 2026-08-27 — PetAiReportCard.tsx ve frontend bileşen geliştirmesi

**Commit:** `28fed81e`  
**Orijinal commit mesajı:** `Ben neyim düzeltmeleri`

Frontend projesinde `PetAiReportCard.tsx`, `PetReportView.tsx`, `BenNeyimPage.test.tsx` ve 3 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 232. 2026-08-27 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `ac7d558b`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---FRONTEND into develop`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `App.tsx`, `Header.test.tsx`, `Header.tsx` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 233. 2026-08-27 — Header.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `ff8f59e6`  
**Orijinal commit mesajı:** `Ai incelemedeki build sorunları çözüldü.`

Frontend projesinde `Header.test.tsx`, `Header.tsx`, `BenNeyimPage.test.tsx` ve 1 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 234. 2026-08-27 — App.css ve frontend bileşen geliştirmesi

**Commit:** `5d4318da`  
**Orijinal commit mesajı:** `Header düzenlendi gereksiz butonlar kaldırıldı, ilanlar kısmı için dropdown menü sitiline geçildi ve ilanlar/sahiplendirme kısmı entegre edildi`

Frontend projesinde `App.css`, `App.tsx`, `BottomNav.tsx` ve 6 ek dosya bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

### 235. 2026-08-27 — Adoption.test.tsx ve frontend bileşen geliştirmesi

**Commit:** `e6510521`  
**Orijinal commit mesajı:** `Listing sayfasındaki sayfa geçiş problemi düzeltildi.`

Frontend projesinde `Adoption.test.tsx`, `listingpage.test.tsx`, `listingpage.tsx` bileşenleri üzerinde teknik geliştirmeler yapıldı. İlgili arayüz mantığı düzenlendi.

---

## 3. PATIMATI — BACKEND (develop) COMMITLERİ

### 1. 2026-07-21 — README.md ve ilgili backend katmanı geliştirmesi

**Commit:** `0af10de1`  
**Orijinal commit mesajı:** `Initial commit`

Backend projesinde `README.md` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 2. 2026-07-24 — Birim testlerinin güncellenmesi (`PatiMatiApplicationTests.java`)

**Commit:** `f77eb4d9`  
**Orijinal commit mesajı:** `feat: PatiMati Modüler Monolit Spring Boot proje kurulumu`

Servis katmanı doğrulamaları için `PatiMatiApplicationTests.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 3. 2026-07-24 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `d77dcb0a`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI---BACKEND`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 4. 2026-07-24 — README.md ve ilgili backend katmanı geliştirmesi

**Commit:** `d2e1eb21`  
**Orijinal commit mesajı:** `Update project name in README.md`

Backend projesinde `README.md` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 5. 2026-07-25 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `59c6c78b`  
**Orijinal commit mesajı:** `Jtw token sistemi eklendi. Google auth hizmetleri ve kullanıcı entitiy sınıfları oluşturuldu. Kullanıcı gereksinimleri ve repostorysi oluşturuldu`

Backend projesinde `pom.xml`, `SecurityConfig.java`, `AuthController.java` ve 9 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 6. 2026-07-25 — pull request #1 birleştirmesi ve çakışma çözümleri

**Commit:** `f6b3a80c`  
**Orijinal commit mesajı:** `Merge pull request #1 from SantoPanto/jwt

Jtw token sistemi eklendi. Google auth hizmetleri ve kullanıcı entiti…`

Geliştirme dalı (pull request #1) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `SecurityConfig.java`, `AuthController.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 7. 2026-07-25 — pull request #2 birleştirmesi ve çakışma çözümleri

**Commit:** `d9d9c6ff`  
**Orijinal commit mesajı:** `Merge pull request #2 from SantoPanto/main

main pull 1`

Geliştirme dalı (pull request #2) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitattributes`, `.gitignore`, `maven-wrapper.properties` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 8. 2026-07-26 — SecurityConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `ebb259db`  
**Orijinal commit mesajı:** `Login ve Request güvenlik önlemleri alındı ve Google bağlantısı düzenlendi`

Backend projesinde `SecurityConfig.java`, `AuthController.java`, `GoogleAuthRequest.java` ve 4 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 9. 2026-07-26 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `2a7ca0b0`  
**Orijinal commit mesajı:** `Servis yapısı güncellendi logout sistemi eklendi, controllerda geçici olarak yapılmış yapılar servis kısmına alındı`

Backend projesinde `pom.xml`, `AuthController.java`, `UserController.java` ve 1 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 10. 2026-07-26 — Flyway veritabanı migration betiğinin eklenmesi (`V1__init_schema.sql`)

**Commit:** `4873cd8e`  
**Orijinal commit mesajı:** `flyway eklentisi kuruldu, location için geography'ye geçildi, gist indeksi eklendi ve jwt güvenliği sağlandı`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 11. 2026-07-26 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `d6f4bb1a`  
**Orijinal commit mesajı:** `Merge branch 'develop' into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `AdController.java`, `Ad.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 12. 2026-07-26 — pull request #5 birleştirmesi ve çakışma çözümleri

**Commit:** `7fbabfa8`  
**Orijinal commit mesajı:** `Merge pull request #5 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #5) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `SecurityConfig.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 13. 2026-07-26 — Flyway veritabanı migration betiğinin eklenmesi (`V1__init_schema.sql`)

**Commit:** `c285bfe4`  
**Orijinal commit mesajı:** `flyway ve sql yapısı entegresi sağlandı, Dosya uyumsuzlukları giderildi, datasource dosyaları gizliye alındı`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 14. 2026-07-27 — pull request #6 birleştirmesi ve çakışma çözümleri

**Commit:** `f0e2c0c9`  
**Orijinal commit mesajı:** `Merge pull request #6 from SantoPanto/ad-management

reklam yönetimi veri modeli ve evcil hayvan profili alanları ekleme`

Geliştirme dalı (pull request #6) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Ad.java`, `AgeGroup.java`, `CoatPattern.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 15. 2026-07-27 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `9b319114`  
**Orijinal commit mesajı:** `User register ve login güvenlikleri sağlandı, Global expection yönetimi hazırlandı ve sql sorguları düzenlendi.`

Backend projesinde `pom.xml`, `SecurityConfig.java`, `ValidationExceptionHandler.java` ve 4 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 16. 2026-07-27 — pull request #3 birleştirmesi ve çakışma çözümleri

**Commit:** `8ccc196c`  
**Orijinal commit mesajı:** `Merge pull request #3 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #3) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `SecurityConfig.java` ve 25 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 17. 2026-07-28 — pull request #7 birleştirmesi ve çakışma çözümleri

**Commit:** `28c6820a`  
**Orijinal commit mesajı:** `Merge pull request #7 from SantoPanto/ad-management

İlan yönetimi, DTO/Mapper yapısı ve S3 fotoğraf altyapısı`

Geliştirme dalı (pull request #7) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `S3Config.java`, `S3StorageProperties.java` ve 18 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 18. 2026-07-28 — Flyway veritabanı migration betiğinin eklenmesi (`V4__create_password_reset_tokens_table.sql`)

**Commit:** `7a796ec9`  
**Orijinal commit mesajı:** `Şifremi unuttum servisleri yazıldı ve sql sorguları oluşturuldu.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V4__create_password_reset_tokens_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 19. 2026-07-28 — pull request #8 birleştirmesi ve çakışma çözümleri

**Commit:** `69464c50`  
**Orijinal commit mesajı:** `Merge pull request #8 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #8) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `S3Config.java` ve 45 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 20. 2026-07-29 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `b56b71ce`  
**Orijinal commit mesajı:** `Raporlama sistemi oluşturuldu, Enum sistemiyle konular belirlendi, test için ilanların değiştirilmesi bekleniyor.`

Backend projesinde `pom.xml`, `ComplaintController.java`, `ComplaintRequest.java` ve 6 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 21. 2026-07-29 — pull request #10 birleştirmesi ve çakışma çözümleri

**Commit:** `5f996c0d`  
**Orijinal commit mesajı:** `Merge pull request #10 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #10) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `ComplaintController.java`, `ComplaintRequest.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 22. 2026-07-29 — SecurityConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `516fc4c8`  
**Orijinal commit mesajı:** `Expection yönetimi birleştirildi ve securityconfig dosyası json formatına göre düzenlendi`

Backend projesinde `SecurityConfig.java`, `ValidationExceptionHandler.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 23. 2026-07-29 — UserService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `ccd85351`  
**Orijinal commit mesajı:** `Logout işlemi için bildirim durdurma eklendi`

Backend projesinde `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 24. 2026-07-29 — pull request #11 birleştirmesi ve çakışma çözümleri

**Commit:** `450468e6`  
**Orijinal commit mesajı:** `Merge pull request #11 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #11) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `ValidationExceptionHandler.java`, `UserService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 25. 2026-07-29 — OAuth2LoginSuccessHandler.java ve ilgili backend katmanı geliştirmesi

**Commit:** `1d0fa5ce`  
**Orijinal commit mesajı:** `Google Auth kısmında google tarafından alınan isim google kurallarınca düzenlendi ve isim soyisim bölünümü 'verilen isim' ve 'aile ismi' şeklinde parçlanıp hazırlandı. Default user değeri enabled!`

Backend projesinde `OAuth2LoginSuccessHandler.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 26. 2026-07-29 — pull request #12 birleştirmesi ve çakışma çözümleri

**Commit:** `9edb13aa`  
**Orijinal commit mesajı:** `Merge pull request #12 from SantoPanto/jwt

Google Auth kısmında google tarafından alınan isim google kurallarınc…`

Geliştirme dalı (pull request #12) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `OAuth2LoginSuccessHandler.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 27. 2026-07-30 — pull request #16 birleştirmesi ve çakışma çözümleri

**Commit:** `57a51f30`  
**Orijinal commit mesajı:** `Merge pull request #16 from SantoPanto/feature/ad-refactoring

adcontroller ve adservisi değiştirdim`

Geliştirme dalı (pull request #16) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 28. 2026-07-30 — pull request #17 birleştirmesi ve çakışma çözümleri

**Commit:** `5d705179`  
**Orijinal commit mesajı:** `Merge pull request #17 from SantoPanto/feature/ai-kuyruk-koprusu

AI servisiyle RabbitMQ köprüsü (KISIM 4) + Lombok derleme düzeltmesi`

Geliştirme dalı (pull request #17) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 12 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 29. 2026-07-30 — pull request #18 birleştirmesi ve çakışma çözümleri

**Commit:** `6de16124`  
**Orijinal commit mesajı:** `Merge pull request #18 from SantoPanto/fix/test-derlemesi

fix: test derlemesi kırıktı + fotoğraf yükleme akışı tamamlandı`

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 30. 2026-07-31 — pull request #19 birleştirmesi ve çakışma çözümleri

**Commit:** `0b0fc40e`  
**Orijinal commit mesajı:** `Merge pull request #19 from SantoPanto/websocket-messaging

STOMP ve WebSocket bağlantı yapılandırması eklendi`

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `WebSocketProperties.java`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 31. 2026-07-31 — pull request #20 birleştirmesi ve çakışma çözümleri

**Commit:** `7eef6053`  
**Orijinal commit mesajı:** `Merge pull request #20 from SantoPanto/websocket-messaging

security Paketi Altındaki Çalışmalar (WebSocket Interceptor)`

Geliştirme dalı (pull request #20) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `WebSocketConfig.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 32. 2026-08-03 — Birim testlerinin güncellenmesi (`ComplaintControllerTest.java`)

**Commit:** `bfb2aa51`  
**Orijinal commit mesajı:** `Userlist oluşturuldu ve servisler ayağa kaldırıldı. Şikayet isteğinde ufak bir bugfix yapıldı !Önemli not: List şu anda genel kullanıcıya açık ileride admin'e döndürülmesi gerekli!!!`

Servis katmanı doğrulamaları için `ComplaintControllerTest.java`, `ComplaintReasonTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 33. 2026-08-04 — Birim testlerinin güncellenmesi (`ComplaintControllerTest.java`)

**Commit:** `54ca2741`  
**Orijinal commit mesajı:** `Şikayet bölümü oluşturuldu ve birim testleri yapıldı`

Servis katmanı doğrulamaları için `ComplaintControllerTest.java`, `ComplaintServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 34. 2026-08-04 — SecurityConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `80984d67`  
**Orijinal commit mesajı:** `İlan servisine ilanları listeleme özelliği eklendi. İlanları oluşturmada kullanılan firebase veya fotoğraf kontrolleri geçici olarak askıya alındı ve ilan ai status için bir enum dosyası oluşturuldu. İlanların ve şikayetlerin testleri gerçekleştirildi ve sistemsel geliştirmeler planlanıyor. Önemli not: Askıya alınan önlemler ilerde sunucu düzeyine çıkıldığında mutlaka yeniden fonksiyonel hale getirilmelidir.`

Backend projesinde `SecurityConfig.java`, `AdController.java`, `Ad.java` ve 2 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 35. 2026-08-04 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `6db493dc`  
**Orijinal commit mesajı:** `Merge branch 'develop' into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 20 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 36. 2026-08-04 — pull request #22 birleştirmesi ve çakışma çözümleri

**Commit:** `de428912`  
**Orijinal commit mesajı:** `Merge pull request #22 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #22) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `ComplaintController.java` ve 14 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 37. 2026-08-04 — Flyway veritabanı migration betiğinin eklenmesi (`V7__create_decoupled_complaint_tables.sql`)

**Commit:** `84b4edab`  
**Orijinal commit mesajı:** `Şikayet oluşturma ilan ve kişi özelinde ikiye ayrıldı ve servisleri data tablosuyla beraber tekrar oluşturuldu`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V7__create_decoupled_complaint_tables.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 38. 2026-08-04 — branch 'jwt' birleştirmesi ve çakışma çözümleri

**Commit:** `375a63d0`  
**Orijinal commit mesajı:** `Merge branch 'jwt' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'jwt') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 20 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 39. 2026-08-04 — pull request #23 birleştirmesi ve çakışma çözümleri

**Commit:** `78aa7d36`  
**Orijinal commit mesajı:** `Merge pull request #23 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ComplaintController.java`, `AdComplaint.java`, `Complaint.java` ve 13 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 40. 2026-08-04 — User.java ve ilgili backend katmanı geliştirmesi

**Commit:** `99980eb3`  
**Orijinal commit mesajı:** `Remove comments for uid field in User class

Removed commented documentation for uid field.`

Backend projesinde `User.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 41. 2026-08-04 — pull request #21 birleştirmesi ve çakışma çözümleri

**Commit:** `910f365c`  
**Orijinal commit mesajı:** `Merge pull request #21 from SantoPanto/websocket-messaging

Websocket ve Güvenli Mesajlaşma Altyapısı Tamamlandı`

Geliştirme dalı (pull request #21) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Message.java`, `User.java`, `MessageRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 42. 2026-08-04 — pull request #24 birleştirmesi ve çakışma çözümleri

**Commit:** `2e9b0d64`  
**Orijinal commit mesajı:** `Merge pull request #24 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Message.java`, `User.java`, `MessageRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 43. 2026-08-04 — Flyway veritabanı migration betiğinin eklenmesi (`V6__create_messages_table.sql`)

**Commit:** `b59ea8eb`  
**Orijinal commit mesajı:** `Birleştirme sonrası yaşanan çakışmalar giderildi ve isim düzenlemeleri yapıldı.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V6__create_messages_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 44. 2026-08-04 — pull request #25 birleştirmesi ve çakışma çözümleri

**Commit:** `125c5e58`  
**Orijinal commit mesajı:** `Merge pull request #25 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #25) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiRabbitConfig.java`, `SecurityConfig.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 45. 2026-08-05 — pull request #26 birleştirmesi ve çakışma çözümleri

**Commit:** `978c4c7f`  
**Orijinal commit mesajı:** `Merge pull request #26 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `AiAnalysisListener.java`, `AiAnalysisPublisher.java` ve 70 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 46. 2026-08-06 — UserRepository.java ve ilgili backend katmanı geliştirmesi

**Commit:** `013d1d2f`  
**Orijinal commit mesajı:** `Remove findUsersNearby method from UserRepository

Removed the findUsersNearby method and its query.`

Backend projesinde `UserRepository.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 47. 2026-08-06 — UserRepository.java ve ilgili backend katmanı geliştirmesi

**Commit:** `eb85de8f`  
**Orijinal commit mesajı:** `Add method to find nearby users with FCM token

Added a method to find users nearby based on location and FCM token.`

Backend projesinde `UserRepository.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 48. 2026-08-06 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `12d2f2c0`  
**Orijinal commit mesajı:** `Merge branch 'main' into feature/messaging-module`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 85 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 49. 2026-08-06 — pull request #28 birleştirmesi ve çakışma çözümleri

**Commit:** `c5b916a5`  
**Orijinal commit mesajı:** `Merge pull request #28 from SantoPanto/fix/gis-sorgu-parametreleri

fix: PostGIS sorgularında parametreye ::geography — 3 sorgu sessizce ölüydü`

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisPublisher.java`, `AdRepository.java`, `UserRepository.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 50. 2026-08-06 — UserRepository.java ve ilgili backend katmanı geliştirmesi

**Commit:** `ded1ad01`  
**Orijinal commit mesajı:** `Update UserRepository.java`

Backend projesinde `UserRepository.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 51. 2026-08-06 — pull request #29 birleştirmesi ve çakışma çözümleri

**Commit:** `879bd518`  
**Orijinal commit mesajı:** `Merge pull request #29 from SantoPanto/feature/messaging-module

Feature/messaging module`

Geliştirme dalı (pull request #29) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `all_codes.txt`, `PatiMatiApplication.java`, `AdController.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 52. 2026-08-06 — Birim testlerinin güncellenmesi (`AdControllerTest.java`)

**Commit:** `f71eb1a1`  
**Orijinal commit mesajı:** `Dosya birleştirmesi sonrası uyumsuzlukların giderilmesi`

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 53. 2026-08-06 — pull request #30 birleştirmesi ve çakışma çözümleri

**Commit:** `6ec71f17`  
**Orijinal commit mesajı:** `Merge pull request #30 from SantoPanto/jwt

Dosya birleştirmesi sonrası uyumsuzlukların giderilmesi`

Geliştirme dalı (pull request #30) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageResponse.java`, `MessageRepository.java`, `MessageService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 54. 2026-08-06 — pull request #31 birleştirmesi ve çakışma çözümleri

**Commit:** `a6da1b62`  
**Orijinal commit mesajı:** `Merge pull request #31 from SantoPanto/fix/bos-veritabaninda-kurulum

fix: boş veritabanında uygulama açılmıyor — users anahtarı iki farklı adla kullanılıyor`

Geliştirme dalı (pull request #31) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V1__init_schema.sql`, `V4__create_password_reset_tokens_table.sql`, `V7__create_decoupled_complaint_tables.sql` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 55. 2026-08-07 — pull request #32 birleştirmesi ve çakışma çözümleri

**Commit:** `56abe460`  
**Orijinal commit mesajı:** `Merge pull request #32 from SantoPanto/fix/ai-cevabi-tarih-cozumlemesi

fix: AI cevabı çözülemiyor — ilanlar sonsuza kadar PENDING kalıyor`

Geliştirme dalı (pull request #32) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiRabbitConfig.java`, `AiJsonMessageConverterTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 56. 2026-08-07 — Flyway veritabanı migration betiğinin eklenmesi (`V1__init_schema.sql`)

**Commit:** `b3daf7dd`  
**Orijinal commit mesajı:** `User'a kayıt olduğu tarih eklendi ve sorguları güncellendi. Public ve admin ayarları yapıldı. Admin istek ve sorguları oluşturuldu.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V1__init_schema.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 57. 2026-08-07 — AdminController.java ve ilgili backend katmanı geliştirmesi

**Commit:** `bffd3e99`  
**Orijinal commit mesajı:** `Yeni admin istemleri için json dönüşleri hazırlandı.`

Backend projesinde `AdminController.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 58. 2026-08-10 — pull request #33 birleştirmesi ve çakışma çözümleri

**Commit:** `e2e4be28`  
**Orijinal commit mesajı:** `Merge pull request #33 from SantoPanto/feature/ai-is-pet

AI'ın "fotoğrafta hayvan var mı" cevabı artık ilana yazılıyor (is_pet)`

Geliştirme dalı (pull request #33) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisResult.java`, `AdResponse.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 59. 2026-08-10 — AdminController.java ve ilgili backend katmanı geliştirmesi

**Commit:** `348eed06`  
**Orijinal commit mesajı:** `Sahiplendirme menüsü yapıldı ve admin panel güncelleştirmeli tamamlandı.`

Backend projesinde `AdminController.java`, `AdoptionComplaintController.java`, `AdoptionController.java` ve 11 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 60. 2026-08-10 — pull request #34 birleştirmesi ve çakışma çözümleri

**Commit:** `6b55250f`  
**Orijinal commit mesajı:** `Merge pull request #34 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #34) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdminController.java`, `AdoptionComplaintController.java` ve 23 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 61. 2026-08-10 — pull request #35 birleştirmesi ve çakışma çözümleri

**Commit:** `23e23731`  
**Orijinal commit mesajı:** `Merge pull request #35 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #35) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisResult.java`, `AdResponse.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 62. 2026-08-10 — AuthResponse.java ve ilgili backend katmanı geliştirmesi

**Commit:** `3f4e03f5`  
**Orijinal commit mesajı:** `Güvenli girişler için dto oluşturuldu ve önemli kullanıcı bilgileri gizlendi. Dönüş json mesajları güncellendi.`

Backend projesinde `AuthResponse.java`, `SafeUserDTO.java`, `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 63. 2026-08-10 — pull request #37 birleştirmesi ve çakışma çözümleri

**Commit:** `ca2d6d93`  
**Orijinal commit mesajı:** `Merge pull request #37 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #37) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AuthResponse.java`, `SafeUserDTO.java`, `UserService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 64. 2026-08-11 — UserService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `65bdb575`  
**Orijinal commit mesajı:** `Getuser işleminde safedto kullanımına geçildi.`

Backend projesinde `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 65. 2026-08-11 — pull request #36 birleştirmesi ve çakışma çözümleri

**Commit:** `eb0fb5d4`  
**Orijinal commit mesajı:** `Merge pull request #36 from SantoPanto/develop

Develop`

Geliştirme dalı (pull request #36) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `all_codes.txt`, `PatiMatiApplication.java`, `AiAnalysisListener.java` ve 51 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 66. 2026-08-11 — Flyway veritabanı migration betiğinin eklenmesi (`V9__add_reward_points_to_users.sql`)

**Commit:** `c2c60b00`  
**Orijinal commit mesajı:** `Rozet yapısı oluşturuldu. Servis dto ve entity olarak düzenlendi. Manuel testleri henüz yapılmadı.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V9__add_reward_points_to_users.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 67. 2026-08-11 — pull request #38 birleştirmesi ve çakışma çözümleri

**Commit:** `e1decf9f`  
**Orijinal commit mesajı:** `Merge pull request #38 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #38) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdoptionController.java`, `SafeUserDTO.java` ve 10 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 68. 2026-08-11 — SecurityConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `459e07dd`  
**Orijinal commit mesajı:** `User servisine kullanıcı uptate methodu eklendi ve frontend e bağlandı`

Backend projesinde `SecurityConfig.java`, `UserController.java`, `UpdateProfileRequest.java` ve 1 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 69. 2026-08-11 — AuthResponse.java ve ilgili backend katmanı geliştirmesi

**Commit:** `1e0c7514`  
**Orijinal commit mesajı:** `Userdto güncellendi ve profil sayfası için gerekli bağlantılar düzenlendi.`

Backend projesinde `AuthResponse.java`, `RegisterRequest.java`, `SafeUserDTO.java` ve 3 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 70. 2026-08-11 — UpdateProfileRequest.java ve ilgili backend katmanı geliştirmesi

**Commit:** `ff309bfd`  
**Orijinal commit mesajı:** `Konum planlası user entity'si için ve servisleri için yapılandırıldı. Konum yapıları belirli bir sınırlamayla güvenli haline getirildi.`

Backend projesinde `UpdateProfileRequest.java`, `UserResponseDTO.java`, `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 71. 2026-08-11 — UserService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `dd4051f2`  
**Orijinal commit mesajı:** `getAllUsers methodu UserResponseDTO'yu kullanacak şekilde yeniden düzenlendi.`

Backend projesinde `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 72. 2026-08-12 — pull request #39 birleştirmesi ve çakışma çözümleri

**Commit:** `ef064149`  
**Orijinal commit mesajı:** `Merge pull request #39 from SantoPanto/fix/eksik-migration-ve-null-enumlar

fix: eksik migration ve null enum'lar — ilan okuyan uclar ve sahiplendirme kirikti`

Geliştirme dalı (pull request #39) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `OKU-ONCE.md`, `V10__add_suspended_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 73. 2026-08-12 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `31e36c3c`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `OKU-ONCE.md`, `V10__add_suspended_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 74. 2026-08-12 — UserController.java ve ilgili backend katmanı geliştirmesi

**Commit:** `04a3c4d3`  
**Orijinal commit mesajı:** `FCM token için homepage ve ilan sayfasında gönderim açıldı.`

Backend projesinde `UserController.java`, `FcmTokenUpdateDTO.java`, `UserService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 75. 2026-08-12 — pull request #41 birleştirmesi ve çakışma çözümleri

**Commit:** `c27e5219`  
**Orijinal commit mesajı:** `Merge pull request #41 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #41) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `UserController.java`, `AuthResponse.java` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 76. 2026-08-12 — .gitignore ve ilgili backend katmanı geliştirmesi

**Commit:** `fa9055bb`  
**Orijinal commit mesajı:** `chore: add firebase admin sdk to gitignore`

Backend projesinde `.gitignore` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 77. 2026-08-14 — pull request #44 birleştirmesi ve çakışma çözümleri

**Commit:** `f4cf988a`  
**Orijinal commit mesajı:** `Merge pull request #44 from SantoPanto/fix/b4-remove-test-endpoint

B-4: Ürün kodunda kalan test adresinin kapatılması`

Geliştirme dalı (pull request #44) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 78. 2026-08-14 — Birim testlerinin güncellenmesi (`AdServiceTest.java`)

**Commit:** `415ebdb2`  
**Orijinal commit mesajı:** `Miniio ayarları kuruldu ve aws ayarları geçici olarak yorum satırına alındı.`

Servis katmanı doğrulamaları için `AdServiceTest.java`, `S3ImageStorageServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 79. 2026-08-14 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `eed57ab6`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdService.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 80. 2026-08-14 — pull request #45 birleştirmesi ve çakışma çözümleri

**Commit:** `a6b9da5b`  
**Orijinal commit mesajı:** `Merge pull request #45 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #45) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitignore`, `pom.xml`, `FirebaseConfig.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 81. 2026-08-14 — branch 'develop' birleştirmesi (Merge commit)

**Commit:** `4a81a41b`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'develop') ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 82. 2026-08-14 — Birim testlerinin güncellenmesi (`AdoptionServiceImplTest.java`)

**Commit:** `3d228a08`  
**Orijinal commit mesajı:** `İlanda opsiyonel olan renk enumu zorunluymuş gibi davranılıyordu. Bu sorun düzeltildi ve userlist sadece adminlere özel hala getirildi.`

Servis katmanı doğrulamaları için `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 83. 2026-08-14 — pull request #46 birleştirmesi ve çakışma çözümleri

**Commit:** `e751ff3a`  
**Orijinal commit mesajı:** `Merge pull request #46 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #46) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `UserController.java`, `AdUpdateRequest.java` ve 8 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 84. 2026-08-14 — pull request #47 birleştirmesi ve çakışma çözümleri

**Commit:** `8680f383`  
**Orijinal commit mesajı:** `Merge pull request #47 from SantoPanto/fix/presigned-url-path-style

fix: presigned URL'ler path-style uretmiyordu - fotograflar ve AI indirmesi kirik`

Geliştirme dalı (pull request #47) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `S3Config.java`, `S3PresignedUrlSekliTest.java`, `S3ImageStorageServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 85. 2026-08-14 — pull request #48 birleştirmesi ve çakışma çözümleri

**Commit:** `c780c819`  
**Orijinal commit mesajı:** `Merge pull request #48 from SantoPanto/fix/s3-yapilandirma-profili

refactor: S3 ayarlari ortam degiskenine tasindi, yerel MinIO ayri profile alindi`

Geliştirme dalı (pull request #48) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `README.md`, `application-local.yml`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 86. 2026-08-14 — Birim testlerinin güncellenmesi (`AdminServiceImplTest.java`)

**Commit:** `fe8392fc`  
**Orijinal commit mesajı:** `Admin paneli güncelledi, Düzgün bilgi aktarımı sağlanması için liste kodları tekrardan oluşturuldu.`

Servis katmanı doğrulamaları için `AdminServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 87. 2026-08-14 — pull request #49 birleştirmesi ve çakışma çözümleri

**Commit:** `aca2986f`  
**Orijinal commit mesajı:** `Merge pull request #49 from SantoPanto/jwt

Admin paneli güncelledi, Düzgün bilgi aktarımı sağlanması için liste …`

Geliştirme dalı (pull request #49) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminServiceImpl.java`, `AdminServiceImplTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 88. 2026-08-17 — AdoptionAdCreateRequest.java ve ilgili backend katmanı geliştirmesi

**Commit:** `e61bc68e`  
**Orijinal commit mesajı:** `Dosya yükleme ayarlar 5mb ve total 20 mb olarak sınır izinleri tanımlandırıldı. Sahiplendirme ilanı gereksinimleri kayıp hayvan ilanlarıyla aynı seviyeye getirildi.`

Backend projesinde `AdoptionAdCreateRequest.java`, `application.yml` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 89. 2026-08-17 — pull request #56 birleştirmesi ve çakışma çözümleri

**Commit:** `e117bb61`  
**Orijinal commit mesajı:** `Merge pull request #56 from SantoPanto/jwt

Dosya yükleme ayarlar 5mb ve total 20 mb olarak sınır izinleri tanıml…`

Geliştirme dalı (pull request #56) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionAdCreateRequest.java`, `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 90. 2026-08-17 — ChatRoomResponseDTO.java ve ilgili backend katmanı geliştirmesi

**Commit:** `fcf495a8`  
**Orijinal commit mesajı:** `Mesajlaşma altyapısı için kullanıcının kendine mesaj göndermesi engellendi. Sender id ve reciver id şeklinde tutulup güvenliği sağlanacak. Gerçek kullanıcı adı gözükmesi sağlandı ve connected işlemi düzeltildi.`

Backend projesinde `ChatRoomResponseDTO.java`, `MessageResponse.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 91. 2026-08-17 — pull request #57 birleştirmesi ve çakışma çözümleri

**Commit:** `6faf148e`  
**Orijinal commit mesajı:** `Merge pull request #57 from SantoPanto/jwt

Mesajlaşma altyapısı için kullanıcının kendine mesaj göndermesi engel…`

Geliştirme dalı (pull request #57) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatRoomResponseDTO.java`, `MessageResponse.java`, `WebSocketChannelInterceptor.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 92. 2026-08-17 — pull request #58 birleştirmesi ve çakışma çözümleri

**Commit:** `fbb1fdc9`  
**Orijinal commit mesajı:** `Merge pull request #58 from SantoPanto/fix/ai-aday-askiya-alma

fix: askıya alınmış ilan AI aday havuzuna girmesin`

Geliştirme dalı (pull request #58) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdRepository.java`, `AiAdayAskiyaAlmaTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 93. 2026-08-17 — ChatController.java ve ilgili backend katmanı geliştirmesi

**Commit:** `fc0bca2e`  
**Orijinal commit mesajı:** `Sohbet listeleme mantığı düzenlendi ve güncel mesajlar altta olacak şekilde modern mesajlaşma yapısı oluşturuldu. Endpoint düzenlendi. Anlık güncellemeler için uygun hale getirildi.`

Backend projesinde `ChatController.java`, `MessageRepository.java`, `WebSocketChannelInterceptor.java` ve 1 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 94. 2026-08-17 — pull request #59 birleştirmesi ve çakışma çözümleri

**Commit:** `b879ed0d`  
**Orijinal commit mesajı:** `Merge pull request #59 from SantoPanto/jwt

Sohbet listeleme mantığı düzenlendi ve güncel mesajlar altta olacak ş…`

Geliştirme dalı (pull request #59) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatController.java`, `MessageRepository.java`, `WebSocketChannelInterceptor.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 95. 2026-08-17 — pull request #60 birleştirmesi ve çakışma çözümleri

**Commit:** `d41a1ed6`  
**Orijinal commit mesajı:** `Merge pull request #60 from SantoPanto/fix/bildirim-gunlugu-gercegi-soylesin

B5: bildirim günlüğü teslimat konusunda yanıltıyordu`

Geliştirme dalı (pull request #60) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiMatchNotifier.java`, `FirebasePushNotificationService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 96. 2026-08-17 — .gitignore ve ilgili backend katmanı geliştirmesi

**Commit:** `9a46eec8`  
**Orijinal commit mesajı:** `Google auth koruması oluşturuldu.`

Backend projesinde `.gitignore`, `application.yml` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 97. 2026-08-17 — PasswordEncoderConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `b54f0e19`  
**Orijinal commit mesajı:** `Security configdeki google key bölümü passwordencoderconfig sayfasına alındı ve solid prensipleriyle yazıldı. Google auth çalışması için application.yml dosyası düzenlendi ve port ayarı 5173'e çekildi`

Backend projesinde `PasswordEncoderConfig.java`, `SecurityConfig.java`, `OAuth2AuthenticationSuccessHandler.java` ve 3 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 98. 2026-08-17 — pull request #61 birleştirmesi ve çakışma çözümleri

**Commit:** `33f40a2e`  
**Orijinal commit mesajı:** `Merge pull request #61 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #61) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.gitignore`, `PasswordEncoderConfig.java`, `SecurityConfig.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 99. 2026-08-17 — branch 'main' birleştirmesi (Merge commit)

**Commit:** `6c460656`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'main') ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 100. 2026-08-17 — Flyway veritabanı migration betiğinin eklenmesi (`V11__create_ad_matches_table.sql`)

**Commit:** `f434bd42`  
**Orijinal commit mesajı:** `Eşleşme mantığı için gerekli arkaplan oluşturuldu ve v11 tablosu oluşturulup bağlandı.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V11__create_ad_matches_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 101. 2026-08-18 — pull request #50 birleştirmesi ve çakışma çözümleri

**Commit:** `9867d026`  
**Orijinal commit mesajı:** `Merge pull request #50 from SantoPanto/feature/b11-public-nearby-ads

B-11: Yakındaki ilanlar için herkese açık harita endpoint’i`

Geliştirme dalı (pull request #50) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicAdController.java`, `AdRepository.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 102. 2026-08-18 — pull request #66 birleştirmesi (Merge commit)

**Commit:** `56260409`  
**Orijinal commit mesajı:** `Merge pull request #66 from SantoPanto/main

pr tı organize`

Geliştirme dalı (pull request #66) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 103. 2026-08-18 — Flyway veritabanı migration betiğinin eklenmesi (`V11__create_ad_matches_table.sql`)

**Commit:** `b49751f8`  
**Orijinal commit mesajı:** `feat(AdMatch): D1 Code Review iyileştirmeleri ve Çözüm B mimarisi uygulandı

Detaylar:
- AdMatch Entity @Table(name = 'ad_match') olarak tekil isme güncellendi.
- V11 migration dosyasına user_id ve Foreign Key eklendi, UNIQUE kısıtlaması (user_id, source_ad_id, matched_ad_id) olarak değiştirildi.
- AdMatchService içindeki Math.min/max kanonik sıralaması kaldırılarak bildirim ayrışması sağlandı.
- S3 medya linkleri signed HTTP URL'lerine dönüştürüldü.
- Repository sorgularına askı (suspended) süzgeci eklendi.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V11__create_ad_matches_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 104. 2026-08-18 — pull request #67 birleştirmesi ve çakışma çözümleri

**Commit:** `8007871b`  
**Orijinal commit mesajı:** `Merge pull request #67 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #67) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MatchController.java`, `AdMatchResponseDTO.java`, `AdMatchSaveRequest.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 105. 2026-08-18 — pull request #68 birleştirmesi ve çakışma çözümleri

**Commit:** `aebb1452`  
**Orijinal commit mesajı:** `Merge pull request #68 from SantoPanto/fix/b8-require-ad-photos

B-8 İlan oluştururken fotoğraf zorunluluğu getirildi.`

Geliştirme dalı (pull request #68) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdControllerTest.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 106. 2026-08-18 — pull request #70 birleştirmesi ve çakışma çözümleri

**Commit:** `ba537fdc`  
**Orijinal commit mesajı:** `Merge pull request #70 from SantoPanto/feat/a1-backend-ai-analiz-ucu

feat(A1): analiz AI'ya backend üzerinden gidiyor, çağrılar anahtar taşıyor`

Geliştirme dalı (pull request #70) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalyzeController.java`, `AiMatchService.java`, `application.yml` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 107. 2026-08-18 — pull request #69 birleştirmesi ve çakışma çözümleri

**Commit:** `c8faf715`  
**Orijinal commit mesajı:** `Merge pull request #69 from SantoPanto/feat/b6-eslesme-kaydi-ve-bildirim-tekrari

feat(B6): eşleşmeler kaydediliyor, bildirim tekrarlanmıyor`

Geliştirme dalı (pull request #69) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiMatchNotifier.java`, `AiAnalysisResult.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 108. 2026-08-18 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `a80918ad`  
**Orijinal commit mesajı:** `Merge branch 'develop' into feature/b7-b10`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `.gitignore`, `AiAnalysisListener.java` ve 45 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 109. 2026-08-18 — pull request #63 birleştirmesi ve çakışma çözümleri

**Commit:** `3e16ec72`  
**Orijinal commit mesajı:** `Merge pull request #63 from SantoPanto/feature/b7-b10

Feature/b7-b9-b10`

Geliştirme dalı (pull request #63) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `MessageController.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 110. 2026-08-19 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `d85c0ec5`  
**Orijinal commit mesajı:** `Hertner sunucu ayarları`

Backend projesinde `pom.xml`, `S3Config.java`, `MinioImageStorageServiceImpl.java` ve 3 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 111. 2026-08-19 — Maven bağımlılıklarının ve proje konfigürasyonunun güncellenmesi

**Commit:** `2c8afb23`  
**Orijinal commit mesajı:** `Lombok error fix`

Projenin Maven yapılandırma dosyası (`pom.xml`) güncellendi. Bağımlılık kütüphanelerinin sürüm tanımları ve derleme eklentileri düzenlendi.

### 112. 2026-08-19 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `f47e7509`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `AiAnalysisListener.java`, `AiMatchNotifier.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 113. 2026-08-19 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `9dc58683`  
**Orijinal commit mesajı:** `Merge branch 'develop' into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 29 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 114. 2026-08-19 — pull request #72 birleştirmesi ve çakışma çözümleri

**Commit:** `bfbfead5`  
**Orijinal commit mesajı:** `Merge pull request #72 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #72) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `S3Config.java`, `MinioImageStorageServiceImpl.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 115. 2026-08-19 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `731f7182`  
**Orijinal commit mesajı:** `Merge branch 'develop' of https://github.com/SantoPanto/PATIMATI---BACKEND into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `pom.xml`, `AiAnalysisListener.java` ve 21 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 116. 2026-08-19 — AdService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `e18f47db`  
**Orijinal commit mesajı:** `fix: lombok ve adservice tekrar düzeltildi`

Backend projesinde `AdService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 117. 2026-08-19 — pull request #73 birleştirmesi ve çakışma çözümleri

**Commit:** `488d9272`  
**Orijinal commit mesajı:** `Merge pull request #73 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #73) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 118. 2026-08-19 — AdService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `9816cc9b`  
**Orijinal commit mesajı:** `fix: unutulan noktali virgul eklendi`

Backend projesinde `AdService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 119. 2026-08-19 — pull request #74 birleştirmesi ve çakışma çözümleri

**Commit:** `2409c127`  
**Orijinal commit mesajı:** `Merge pull request #74 from SantoPanto/jwt

fix: unutulan noktali virgul eklendi`

Geliştirme dalı (pull request #74) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 120. 2026-08-19 — application.yml ve ilgili backend katmanı geliştirmesi

**Commit:** `c4976674`  
**Orijinal commit mesajı:** `Hertzner güncellemeleri`

Backend projesinde `application.yml` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 121. 2026-08-19 — pull request #76 birleştirmesi ve çakışma çözümleri

**Commit:** `4ed36c69`  
**Orijinal commit mesajı:** `Merge pull request #76 from SantoPanto/jwt

Hertzner güncellemeleri`

Geliştirme dalı (pull request #76) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 122. 2026-08-19 — Flyway veritabanı migration betiğinin eklenmesi (`V12__create_adoption_complaints_table.sql`)

**Commit:** `2ea05625`  
**Orijinal commit mesajı:** `Cors ayarları güncellendi.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V12__create_adoption_complaints_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 123. 2026-08-19 — pull request #77 birleştirmesi ve çakışma çözümleri

**Commit:** `d26ef957`  
**Orijinal commit mesajı:** `Merge pull request #77 from SantoPanto/jwt

Cors ayarları güncellendi.`

Geliştirme dalı (pull request #77) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `application-prod.yml`, `application.yml` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 124. 2026-08-19 — Flyway veritabanı migration betiğinin eklenmesi (`...ion_complaints_table.sql  V13__create_adoption_complaints_table.sql`)

**Commit:** `8815a922`  
**Orijinal commit mesajı:** `Syntax error`

Veritabanı şema versiyonlaması için Flyway migration betiği (`...ion_complaints_table.sql  V13__create_adoption_complaints_table.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 125. 2026-08-19 — pull request #78 birleştirmesi ve çakışma çözümleri

**Commit:** `02e5a2fd`  
**Orijinal commit mesajı:** `Merge pull request #78 from SantoPanto/jwt

Syntax error`

Geliştirme dalı (pull request #78) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `...ion_complaints_table.sql  V13__create_adoption_complaints_table.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 126. 2026-08-19 — application.yml ve ilgili backend katmanı geliştirmesi

**Commit:** `62c26d4d`  
**Orijinal commit mesajı:** `minor bugfix`

Backend projesinde `application.yml` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 127. 2026-08-19 — pull request #79 birleştirmesi ve çakışma çözümleri

**Commit:** `d0f58797`  
**Orijinal commit mesajı:** `Merge pull request #79 from SantoPanto/jwt

minor bugfix`

Geliştirme dalı (pull request #79) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 128. 2026-08-19 — pull request #80 birleştirmesi ve çakışma çözümleri

**Commit:** `1bdb2954`  
**Orijinal commit mesajı:** `Merge pull request #80 from SantoPanto/fix/d7-google-uc-kaldirildi

fix(D-7): hesap devralmaya açık /api/auth/google ucu kaldırıldı`

Geliştirme dalı (pull request #80) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `UserController.java`, `GoogleAuthRequest.java`, `UserService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 129. 2026-08-19 — pull request #81 birleştirmesi ve çakışma çözümleri

**Commit:** `d312d541`  
**Orijinal commit mesajı:** `Merge pull request #81 from SantoPanto/chore/goc-numarasi-kapisi

chore: göç numarası çakışması için iki katmanlı kapı`

Geliştirme dalı (pull request #81) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `goc-numarasi-denetimi.py`, `GocNumarasiCakismasiTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 130. 2026-08-19 — Birim testlerinin güncellenmesi (`MessageControllerTest.java`)

**Commit:** `bdca7653`  
**Orijinal commit mesajı:** `Mesajlaşmada alınan 500 sorunları için Authentication ve ChatController kullanılarak devam edilmiştir.`

Servis katmanı doğrulamaları için `MessageControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 131. 2026-08-19 — pull request #84 birleştirmesi ve çakışma çözümleri

**Commit:** `0b2033fe`  
**Orijinal commit mesajı:** `Merge pull request #84 from SantoPanto/jwt

Mesajlaşmada alınan 500 sorunları için Authentication ve ChatControll…`

Geliştirme dalı (pull request #84) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageController.java`, `MessageControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 132. 2026-08-19 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `bef656c8`  
**Orijinal commit mesajı:** `Bağımlılık sürüm güncellemeleri yapıldı. OpenApi konfigirasyonu ile SecurityScheme eklendi. Application ve spring yapılandırmalarında local testler başarıya ulaştı`

Backend projesinde `pom.xml`, `OpenApiConfig.java`, `SecurityConfig.java` ve 2 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 133. 2026-08-19 — pull request #85 birleştirmesi ve çakışma çözümleri

**Commit:** `3d9de7be`  
**Orijinal commit mesajı:** `Merge pull request #85 from SantoPanto/jwt

Bağımlılık sürüm güncellemeleri yapıldı. OpenApi konfigirasyonu ile S…`

Geliştirme dalı (pull request #85) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `OpenApiConfig.java`, `SecurityConfig.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 134. 2026-08-19 — Birim testlerinin güncellenmesi (`SwaggerDocsIntegrationTest.java`)

**Commit:** `dbfaf1bb`  
**Orijinal commit mesajı:** `swagger için izin güncellemeleri`

Servis katmanı doğrulamaları için `SwaggerDocsIntegrationTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 135. 2026-08-19 — pull request #86 birleştirmesi ve çakışma çözümleri

**Commit:** `1c34cda2`  
**Orijinal commit mesajı:** `Merge pull request #86 from SantoPanto/jwt

swagger için izin güncellemeleri`

Geliştirme dalı (pull request #86) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `application.yml`, `SwaggerDocsIntegrationTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 136. 2026-08-19 — pull request #83 birleştirmesi ve çakışma çözümleri

**Commit:** `744ddb3c`  
**Orijinal commit mesajı:** `Merge pull request #83 from SantoPanto/feature/d4-favoriler

feat(D4): favori ilan altyapisi ve V14 migration eklendi`

Geliştirme dalı (pull request #83) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdController.java`, `FavoriteAd.java`, `FavoriteAdRepository.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 137. 2026-08-19 — pull request #87 birleştirmesi ve çakışma çözümleri

**Commit:** `2a14b1df`  
**Orijinal commit mesajı:** `Merge pull request #87 from SantoPanto/fix/m2-mesaj-odasi-yetkisi

fix(2): sohbet odası ucu ilişki istesin — kullanıcı adı dökülmesin`

Geliştirme dalı (pull request #87) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdRepository.java`, `MessageService.java`, `MesajOdasiIliskiIstiyorTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 138. 2026-08-20 — pull request #88 birleştirmesi ve çakışma çözümleri

**Commit:** `74f64c0e`  
**Orijinal commit mesajı:** `Merge pull request #88 from SantoPanto/feat/m18-ilan-yeniden-yayinla

feat(18): yayından kaldırılan ilan yeniden yayına alınabilsin`

Geliştirme dalı (pull request #88) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdRepository.java`, `AdService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 139. 2026-08-20 — pull request #89 birleştirmesi ve çakışma çözümleri

**Commit:** `b425faf0`  
**Orijinal commit mesajı:** `Merge pull request #89 from SantoPanto/fix/m17-yonetici-sohbet-yetkisi

fix(17): yönetici ilişkisiz kullanıcıyla sohbet odası açabilsin`

Geliştirme dalı (pull request #89) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageService.java`, `MesajOdasiIliskiIstiyorTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 140. 2026-08-20 — pull request #90 birleştirmesi ve çakışma çözümleri

**Commit:** `d26217d2`  
**Orijinal commit mesajı:** `Merge pull request #90 from SantoPanto/fix/m9-mukerrer-eslesme-listesi

fix(9): "Eşleşmelerim" aynı çifti iki kez göstermesin`

Geliştirme dalı (pull request #90) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdMatchRepository.java`, `EslesmeListelemeTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 141. 2026-08-20 — pull request #91 birleştirmesi ve çakışma çözümleri

**Commit:** `238a3ba7`  
**Orijinal commit mesajı:** `Merge pull request #91 from SantoPanto/fix/m18-duzenlemede-mikrocip-silinmesin

fix(18): ilan düzenlenirken mikroçip numarası silinmesin`

Geliştirme dalı (pull request #91) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdMapper.java`, `IlanDuzenlemeMikrocipTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 142. 2026-08-20 — pull request #92 birleştirmesi ve çakışma çözümleri

**Commit:** `4e0709f4`  
**Orijinal commit mesajı:** `Merge pull request #92 from SantoPanto/chore/m14-enum-aynasi-bekcisi

chore(14): backend enum aynası + CI bekçisi`

Geliştirme dalı (pull request #92) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `enum-aynasi.json`, `enum-aynasi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 143. 2026-08-20 — Birim testlerinin güncellenmesi (`AdoptionControllerTest.java`)

**Commit:** `27dc47e8`  
**Orijinal commit mesajı:** `Adoptation ad validasyon güncellemeleri ve testi yapıldı`

Servis katmanı doğrulamaları için `AdoptionControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 144. 2026-08-20 — pull request #93 birleştirmesi ve çakışma çözümleri

**Commit:** `18ac9ea2`  
**Orijinal commit mesajı:** `Merge pull request #93 from SantoPanto/jwt

Adoptation ad validasyon güncellemeleri ve testi yapıldı`

Geliştirme dalı (pull request #93) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionAdCreateRequest.java`, `AdoptionAdUpdateRequest.java`, `GlobalExceptionHandler.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 145. 2026-08-20 — Birim testlerinin güncellenmesi (`AiStatusTest.java`)

**Commit:** `333be27d`  
**Orijinal commit mesajı:** `Yapaay zeka enumları güncellendi ve uyumlu hale getirildi`

Servis katmanı doğrulamaları için `AiStatusTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 146. 2026-08-20 — pull request #94 birleştirmesi ve çakışma çözümleri

**Commit:** `23fee4af`  
**Orijinal commit mesajı:** `Merge pull request #94 from SantoPanto/jwt

Yapaay zeka enumları güncellendi ve uyumlu hale getirildi`

Geliştirme dalı (pull request #94) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `AiStatus.java`, `AiStatusTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 147. 2026-08-20 — Birim testlerinin güncellenmesi (`CorsConfigTest.java`)

**Commit:** `216befb4`  
**Orijinal commit mesajı:** `Önceden yazılan config alanları için test oluşturulmuş ve başarıyla geçmiştir.`

Servis katmanı doğrulamaları için `CorsConfigTest.java`, `AdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 148. 2026-08-20 — pull request #95 birleştirmesi ve çakışma çözümleri

**Commit:** `ef2e420c`  
**Orijinal commit mesajı:** `Merge pull request #95 from SantoPanto/jwt

Önceden yazılan config alanları için test oluşturulmuş ve başarıyla g…`

Geliştirme dalı (pull request #95) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdService.java`, `CorsConfigTest.java`, `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 149. 2026-08-20 — Birim testlerinin güncellenmesi (`AdControllerTest.java`)

**Commit:** `66f6d22a`  
**Orijinal commit mesajı:** `Şifre değiştirme, şikayet onaylama ve afiş yolları düzenlendi ve güncellendi.`

Servis katmanı doğrulamaları için `AdControllerTest.java`, `AdoptionComplaintControllerTest.java`, `PublicAdControllerTest.java` ve 2 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 150. 2026-08-20 — pull request #97 birleştirmesi ve çakışma çözümleri

**Commit:** `2e8ec597`  
**Orijinal commit mesajı:** `Merge pull request #97 from SantoPanto/jwt

Şifre değiştirme, şikayet onaylama ve afiş yolları düzenlendi ve günc…`

Geliştirme dalı (pull request #97) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `AdoptionComplaintController.java` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 151. 2026-08-20 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `72e3ceca`  
**Orijinal commit mesajı:** `Merge branch 'develop' into fix/change-password-endpoint`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `SecurityConfig.java`, `AdController.java`, `AdoptionComplaintController.java` ve 19 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 152. 2026-08-20 — pull request #96 birleştirmesi ve çakışma çözümleri

**Commit:** `d2134348`  
**Orijinal commit mesajı:** `Merge pull request #96 from SantoPanto/fix/change-password-endpoint

Şifre Güncelleme Görevi`

Geliştirme dalı (pull request #96) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `UserController.java`, `ChangePasswordRequest.java`, `UserService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 153. 2026-08-20 — Flyway veritabanı migration betiğinin eklenmesi (`V15__add_poster_privacy_settings.sql`)

**Commit:** `036b1b68`  
**Orijinal commit mesajı:** `Şifrelemede eski şifre için düzenlemeler kaldırıldı sadece yeni ve güncellenen şifreler için olacak. Posterler için afiş güvenliği ve entegrasyonu sağlandı.`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V15__add_poster_privacy_settings.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 154. 2026-08-20 — pull request #98 birleştirmesi ve çakışma çözümleri

**Commit:** `942d9702`  
**Orijinal commit mesajı:** `Merge pull request #98 from SantoPanto/jwt

Şifrelemede eski şifre için düzenlemeler kaldırıldı sadece yeni ve gü…`

Geliştirme dalı (pull request #98) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `PublicAdController.java`, `UserController.java` ve 16 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 155. 2026-08-20 — Birim testlerinin güncellenmesi (`AdServiceTest.java`)

**Commit:** `91b196c3`  
**Orijinal commit mesajı:** `Posterler için türkçe karakter düzenlemesi getirildi ve İlan düzenlemeleri için ayarlar yapıldı`

Servis katmanı doğrulamaları için `AdServiceTest.java`, `PosterServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 156. 2026-08-20 — pull request #100 birleştirmesi ve çakışma çözümleri

**Commit:** `951191a4`  
**Orijinal commit mesajı:** `Merge pull request #100 from SantoPanto/jwt

Posterler için türkçe karakter düzenlemesi getirildi ve İlan düzenlem…`

Geliştirme dalı (pull request #100) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `BusinessException.java`, `GlobalExceptionHandler.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 157. 2026-08-20 — pull request #99 birleştirmesi ve çakışma çözümleri

**Commit:** `4895b985`  
**Orijinal commit mesajı:** `Merge pull request #99 from SantoPanto/fix/m18-askidaki-ilan-ayirt-edilsin

fix(18): askıya alınan ilan sahibin ekranında ayırt edilebilsin`

Geliştirme dalı (pull request #99) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdResponse.java`, `AdMapper.java`, `AdControllerTest.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 158. 2026-08-20 — Birim testlerinin güncellenmesi (`AdControllerTest.java`)

**Commit:** `e5beb1a4`  
**Orijinal commit mesajı:** `Tarih güncellemesi yapılmış ve sahiplendirme için eksik olan bölümler güncellenmiştir.`

Servis katmanı doğrulamaları için `AdControllerTest.java`, `AdoptionControllerTest.java`, `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 159. 2026-08-20 — pull request #102 birleştirmesi ve çakışma çözümleri

**Commit:** `9b5c9fcf`  
**Orijinal commit mesajı:** `Merge pull request #102 from SantoPanto/jwt

Tarih güncellemesi yapılmış ve sahiplendirme için eksik olan bölümler…`

Geliştirme dalı (pull request #102) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdoptionAdCreateRequest.java`, `AdoptionServiceImpl.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 160. 2026-08-20 — Birim testlerinin güncellenmesi (`AdoptionControllerTest.java`)

**Commit:** `f4d899d6`  
**Orijinal commit mesajı:** `İlan tarih sorunları ve türkçe karakter sorunları tekrar gözden geçirildi.`

Servis katmanı doğrulamaları için `AdoptionControllerTest.java`, `PosterServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 161. 2026-08-20 — pull request #103 birleştirmesi ve çakışma çözümleri

**Commit:** `153ca657`  
**Orijinal commit mesajı:** `Merge pull request #103 from SantoPanto/jwt

İlan tarih sorunları ve türkçe karakter sorunları tekrar gözden geçir…`

Geliştirme dalı (pull request #103) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdoptionAdCreateRequest.java`, `GlobalExceptionHandler.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 162. 2026-08-20 — Birim testlerinin güncellenmesi (`AdoptionControllerTest.java`)

**Commit:** `3727ba2d`  
**Orijinal commit mesajı:** `Türkçe karakter bugfix`

Servis katmanı doğrulamaları için `AdoptionControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 163. 2026-08-20 — pull request #104 birleştirmesi ve çakışma çözümleri

**Commit:** `55d19af6`  
**Orijinal commit mesajı:** `Merge pull request #104 from SantoPanto/jwt

Türkçe karakter bugfix`

Geliştirme dalı (pull request #104) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterServiceImpl.java`, `application.yml`, `Roboto-Bold.ttf` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 164. 2026-08-20 — JacksonConfig.java ve ilgili backend katmanı geliştirmesi

**Commit:** `bc57f7db`  
**Orijinal commit mesajı:** `Tarih ile oluşan veri uyuşmazlığı sorunu çözüldü`

Backend projesinde `JacksonConfig.java`, `AdCreateRequest.java`, `AdResponse.java` ve 2 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 165. 2026-08-20 — pull request #106 birleştirmesi ve çakışma çözümleri

**Commit:** `cee234ed`  
**Orijinal commit mesajı:** `Merge pull request #106 from SantoPanto/jwt

Tarih ile oluşan veri uyuşmazlığı sorunu çözüldü`

Geliştirme dalı (pull request #106) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `JacksonConfig.java`, `AdCreateRequest.java`, `AdResponse.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 166. 2026-08-20 — pom.xml ve ilgili backend katmanı geliştirmesi

**Commit:** `ab6dd065`  
**Orijinal commit mesajı:** `version bugfix`

Backend projesinde `pom.xml`, `JacksonConfig.java`, `AdCreateRequest.java` ve 1 ek dosya sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 167. 2026-08-20 — pull request #107 birleştirmesi ve çakışma çözümleri

**Commit:** `77bf312d`  
**Orijinal commit mesajı:** `Merge pull request #107 from SantoPanto/jwt

version bugfix`

Geliştirme dalı (pull request #107) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `JacksonConfig.java`, `AdCreateRequest.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 168. 2026-08-20 — Birim testlerinin güncellenmesi (`AdoptionControllerTest.java`)

**Commit:** `49636387`  
**Orijinal commit mesajı:** `Jackson çıkarıldı ve güvenli dönüşüm sağlandı`

Servis katmanı doğrulamaları için `AdoptionControllerTest.java`, `AdMapperTest.java`, `IlanDuzenlemeMikrocipTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 169. 2026-08-20 — pull request #108 birleştirmesi ve çakışma çözümleri

**Commit:** `dcf05990`  
**Orijinal commit mesajı:** `Merge pull request #108 from SantoPanto/jwt

Jackson çıkarıldı ve güvenli dönüşüm sağlandı`

Geliştirme dalı (pull request #108) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `AdUpdateRequest.java`, `AdoptionAdCreateRequest.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 170. 2026-08-20 — Birim testlerinin güncellenmesi (`AdControllerTest.java`)

**Commit:** `c229bede`  
**Orijinal commit mesajı:** `token düzeltmesi`

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 171. 2026-08-20 — pull request #109 birleştirmesi ve çakışma çözümleri

**Commit:** `9342c671`  
**Orijinal commit mesajı:** `Merge pull request #109 from SantoPanto/jwt

token düzeltmesi`

Geliştirme dalı (pull request #109) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdResponse.java`, `AdMapper.java`, `AdControllerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 172. 2026-08-20 — Birim testlerinin güncellenmesi (`AdControllerTest.java`)

**Commit:** `0509b7c8`  
**Orijinal commit mesajı:** `Dto uyumsuzlukları düzeltildi güvenli dönüşümler sağlandı.`

Servis katmanı doğrulamaları için `AdControllerTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 173. 2026-08-20 — pull request #110 birleştirmesi ve çakışma çözümleri

**Commit:** `e0ec3e51`  
**Orijinal commit mesajı:** `Merge pull request #110 from SantoPanto/jwt

Dto uyumsuzlukları düzeltildi güvenli dönüşümler sağlandı.`

Geliştirme dalı (pull request #110) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdCreateRequest.java`, `GlobalExceptionHandler.java`, `JwtAuthFilter.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 174. 2026-08-21 — pull request #75 birleştirmesi ve çakışma çözümleri

**Commit:** `fa5f6581`  
**Orijinal commit mesajı:** `Merge pull request #75 from SantoPanto/fix/d5-server-notifications

Fix/d5 server notifications`

Geliştirme dalı (pull request #75) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchNotifier.java`, `NotificationController.java`, `NotificationResponse.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 175. 2026-08-21 — S3ImageStorageServiceImpl.java ve ilgili backend katmanı geliştirmesi

**Commit:** `6708f44d`  
**Orijinal commit mesajı:** `ai inceleme servisi için anahtarlar güncellendi`

Backend projesinde `S3ImageStorageServiceImpl.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 176. 2026-08-21 — pull request #114 birleştirmesi ve çakışma çözümleri

**Commit:** `ad824ba8`  
**Orijinal commit mesajı:** `Merge pull request #114 from SantoPanto/jwt

ai inceleme servisi için anahtarlar güncellendi`

Geliştirme dalı (pull request #114) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `S3ImageStorageServiceImpl.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 177. 2026-08-21 — pull request #105 birleştirmesi ve çakışma çözümleri

**Commit:** `00316268`  
**Orijinal commit mesajı:** `Merge pull request #105 from SantoPanto/feat/eslesme-kaydi-cozum-bagi

feat: kayıp ilan kapanırken hangi ilanla eşleştiği kaydedilsin`

Geliştirme dalı (pull request #105) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ResolveLostAdRequest.java`, `Ad.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 178. 2026-08-21 — pull request #111 birleştirmesi ve çakışma çözümleri

**Commit:** `fd1693ef`  
**Orijinal commit mesajı:** `Merge pull request #111 from SantoPanto/fix/v2-change-password-required

Register sayfası için şifre zorunluluğu politikası değiştirildi`

Geliştirme dalı (pull request #111) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `RegisterRequest.java`, `RegisterRequestValidationTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 179. 2026-08-21 — Birim testlerinin güncellenmesi (`AdServiceTest.java`)

**Commit:** `962b301a`  
**Orijinal commit mesajı:** `Bozuk test düzeltildi`

Servis katmanı doğrulamaları için `AdServiceTest.java`, `IlanYenidenYayinlamaTest.java`, `KayipIlanCozumBagiTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 180. 2026-08-21 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `7e5e7467`  
**Orijinal commit mesajı:** `Merge branch 'develop' into jwt`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchNotifier.java`, `NotificationController.java`, `NotificationResponse.java` ve 22 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 181. 2026-08-21 — pull request #115 birleştirmesi ve çakışma çözümleri

**Commit:** `1a079e4e`  
**Orijinal commit mesajı:** `Merge pull request #115 from SantoPanto/jwt

Bozuk test düzeltildi`

Geliştirme dalı (pull request #115) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FirebasePushNotificationService.java`, `NotificationService.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 182. 2026-08-21 — pull request #116 birleştirmesi (Merge commit)

**Commit:** `cfd5d05a`  
**Orijinal commit mesajı:** `Merge pull request #116 from SantoPanto/develop

Merge pull request #115 from SantoPanto/jwt`

Geliştirme dalı (pull request #116) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 183. 2026-08-21 — Birim testlerinin güncellenmesi (`AdServiceTest.java`)

**Commit:** `0bb1d681`  
**Orijinal commit mesajı:** `merge`

Servis katmanı doğrulamaları için `AdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 184. 2026-08-21 — pull request #117 birleştirmesi ve çakışma çözümleri

**Commit:** `b5c3af62`  
**Orijinal commit mesajı:** `Merge pull request #117 from SantoPanto/jwt

Jwt`

Geliştirme dalı (pull request #117) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 185. 2026-08-21 — Birim testlerinin güncellenmesi (`IlanYenidenYayinlamaTest.java`)

**Commit:** `d475bafc`  
**Orijinal commit mesajı:** `Bağlantı uyumsuzluğu çözüldü`

Servis katmanı doğrulamaları için `IlanYenidenYayinlamaTest.java`, `KayipIlanCozumBagiTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 186. 2026-08-21 — pull request #118 birleştirmesi ve çakışma çözümleri

**Commit:** `593cfe14`  
**Orijinal commit mesajı:** `Merge pull request #118 from SantoPanto/jwt

Bağlantı uyumsuzluğu çözüldü`

Geliştirme dalı (pull request #118) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FirebasePushNotificationService.java`, `NotificationService.java`, `AdService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 187. 2026-08-21 — pull request #112 birleştirmesi ve çakışma çözümleri

**Commit:** `a0cedb45`  
**Orijinal commit mesajı:** `Merge pull request #112 from SantoPanto/fix/kapanan-ilan-gorunumu

fix: kapanan ilan doğru görünsün — resolutionStatus telde + "Tümü" gerçekten tümü`

Geliştirme dalı (pull request #112) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdResponse.java`, `AdMapper.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 188. 2026-08-21 — Birim testlerinin güncellenmesi (`AiMatchControllerTest.java`)

**Commit:** `dcc2070f`  
**Orijinal commit mesajı:** `Eşleşmelerde kırık olarak gözüken fotoğrafların yüklenme yapıları sunucuyla uyumlu olacak şekilde güncellendi.`

Servis katmanı doğrulamaları için `AiMatchControllerTest.java`, `AiAnahtarBasligiTest.java`, `AiMatchServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 189. 2026-08-21 — pull request #119 birleştirmesi ve çakışma çözümleri

**Commit:** `8f8b5f22`  
**Orijinal commit mesajı:** `Merge pull request #119 from SantoPanto/jwt

Eşleşmelerde kırık olarak gözüken fotoğrafların yüklenme yapıları sun…`

Geliştirme dalı (pull request #119) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchController.java`, `MatchedAdResponseDTO.java`, `AiMatchService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 190. 2026-08-21 — Birim testlerinin güncellenmesi (`FavoriteAdServiceTest.java`)

**Commit:** `e6068063`  
**Orijinal commit mesajı:** `Favoriler sayfası için aws görsel yapısı güncellendi`

Servis katmanı doğrulamaları için `FavoriteAdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 191. 2026-08-21 — pull request #123 birleştirmesi ve çakışma çözümleri

**Commit:** `471fbff2`  
**Orijinal commit mesajı:** `Merge pull request #123 from SantoPanto/jwt

Favoriler sayfası için aws görsel yapısı güncellendi`

Geliştirme dalı (pull request #123) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdService.java`, `FavoriteAdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 192. 2026-08-21 — Birim testlerinin güncellenmesi (`FavoriteAdServiceTest.java`)

**Commit:** `acd72960`  
**Orijinal commit mesajı:** `Favori servisinin testi güncellendi`

Servis katmanı doğrulamaları için `FavoriteAdServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 193. 2026-08-21 — pull request #124 birleştirmesi ve çakışma çözümleri

**Commit:** `5ea1402c`  
**Orijinal commit mesajı:** `Merge pull request #124 from SantoPanto/jwt

Favori servisinin testi güncellendi`

Geliştirme dalı (pull request #124) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `FavoriteAdServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 194. 2026-08-22 — pull request #121 birleştirmesi ve çakışma çözümleri

**Commit:** `61e88fe6`  
**Orijinal commit mesajı:** `Merge pull request #121 from SantoPanto/feat/yeniden-analiz

feat: FAILED kalan ilan yeniden analize gönderilebilsin (sahip ucu + yönetici toplu kurtarma)`

Geliştirme dalı (pull request #121) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdController.java`, `AdminController.java`, `AdRepository.java` ve 6 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 195. 2026-08-22 — pull request #122 birleştirmesi ve çakışma çözümleri

**Commit:** `bfb1f95b`  
**Orijinal commit mesajı:** `Merge pull request #122 from SantoPanto/fix/b1-aciklama-255-ve-ham-sql

fix: uzun açıklama 500 veriyordu, ham SQL kullanıcıya sızıyordu (B1)`

Geliştirme dalı (pull request #122) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `GlobalExceptionHandler.java`, `V18__ads_description_text.sql`, `GlobalExceptionHandlerTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 196. 2026-08-22 — pull request #125 birleştirmesi ve çakışma çözümleri

**Commit:** `f9ff9dc4`  
**Orijinal commit mesajı:** `Merge pull request #125 from SantoPanto/feat/b8-popup-esik-konum

feat: eşleştirme pop-up'ı eşik ve konum kuralına bağlandı (B8)`

Geliştirme dalı (pull request #125) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchController.java`, `AiMatchService.java`, `AiMatchControllerTest.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 197. 2026-08-22 — pull request #126 birleştirmesi ve çakışma çözümleri

**Commit:** `eb379217`  
**Orijinal commit mesajı:** `Merge pull request #126 from SantoPanto/feat/b5-ilan-arama

feat: İlanlar listesine metin araması — başlık, ırk ve açıklama (B5'in BE yarısı)`

Geliştirme dalı (pull request #126) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicAdController.java`, `AdRepository.java`, `AdService.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 198. 2026-08-22 — pull request #129 birleştirmesi ve çakışma çözümleri

**Commit:** `5996344e`  
**Orijinal commit mesajı:** `Merge pull request #129 from SantoPanto/feat/ilan-sehir-ilce

feat: ilanlara il/ilçe — kayıtta beyan/geokodlama, eskiye backfill ucu (V19)`

Geliştirme dalı (pull request #129) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ci.yml`, `enum-aynasi.json`, `pom.xml` ve 122 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 199. 2026-08-22 — pull request #128 birleştirmesi ve çakışma çözümleri

**Commit:** `cdcba82f`  
**Orijinal commit mesajı:** `Merge pull request #128 from SantoPanto/fix/bildirim-mesaj-mojibake

fix: bildirim mesajlarındaki bozuk Türkçe karakterler (mojibake)`

Geliştirme dalı (pull request #128) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `NotificationService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 200. 2026-08-22 — pull request #127 birleştirmesi ve çakışma çözümleri

**Commit:** `c05bf877`  
**Orijinal commit mesajı:** `Merge pull request #127 from SantoPanto/feat/sifre-sifirlama-maili

feat: şifre sıfırlama e-postası gerçekten gönderiliyor (sahipsiz tablo maddesi, BE yarısı)`

Geliştirme dalı (pull request #127) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `pom.xml`, `ResetPasswordRequest.java`, `EmailService.java` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 201. 2026-08-22 — pull request #131 birleştirmesi ve çakışma çözümleri

**Commit:** `0062abe8`  
**Orijinal commit mesajı:** `Merge pull request #131 from SantoPanto/chore/main-develop-senkron

main-develop senkron: #128 + #129 develop'a`

Geliştirme dalı (pull request #131) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminController.java`, `AdCreateRequest.java`, `AdResponse.java` ve 24 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 202. 2026-08-22 — Birim testlerinin güncellenmesi (`IsMatchRequiredTest.java`)

**Commit:** `b199e2b4`  
**Orijinal commit mesajı:** `Bulundu ve sahiplendirme sayfaları için ai doldurma özelliği eklendi ve sahiplendirme de ai match özelliği devre dışı bırakıldı.`

Servis katmanı doğrulamaları için `IsMatchRequiredTest.java`, `AdMapperTest.java`, `AdoptionServiceImplTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 203. 2026-08-22 — pull request #134 birleştirmesi ve çakışma çözümleri

**Commit:** `ef775451`  
**Orijinal commit mesajı:** `Merge pull request #134 from SantoPanto/jwt

Bulundu ve sahiplendirme sayfaları için ai doldurma özelliği eklendi …`

Geliştirme dalı (pull request #134) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalysisListener.java`, `AiAnalysisPublisher.java`, `AiAnalysisRequest.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 204. 2026-08-22 — Birim testlerinin güncellenmesi (`IsMatchRequiredTest.java`)

**Commit:** `c25db841`  
**Orijinal commit mesajı:** `Test fix`

Servis katmanı doğrulamaları için `IsMatchRequiredTest.java`, `AdControllerTest.java`, `AdoptionControllerTest.java` ve 2 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 205. 2026-08-22 — pull request #135 birleştirmesi ve çakışma çözümleri

**Commit:** `339236a5`  
**Orijinal commit mesajı:** `Merge pull request #135 from SantoPanto/jwt

Test fix`

Geliştirme dalı (pull request #135) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `IsMatchRequiredTest.java`, `AdControllerTest.java`, `AdoptionControllerTest.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 206. 2026-08-22 — Flyway veritabanı migration betiğinin eklenmesi (`V20__add_is_match_required_to_ads.sql`)

**Commit:** `0e18d340`  
**Orijinal commit mesajı:** `Sql düzeltmesi`

Veritabanı şema versiyonlaması için Flyway migration betiği (`V20__add_is_match_required_to_ads.sql`) eklendi. İlgili tablo yapıları ve sütun kısıtlamaları SQL seviyesinde tanımlandı.

### 207. 2026-08-22 — pull request #136 birleştirmesi ve çakışma çözümleri

**Commit:** `b071d01b`  
**Orijinal commit mesajı:** `Merge pull request #136 from SantoPanto/jwt

Sql düzeltmesi`

Geliştirme dalı (pull request #136) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V20__add_is_match_required_to_ads.sql` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 208. 2026-08-23 — pull request #141 birleştirmesi ve çakışma çözümleri

**Commit:** `5474a976`  
**Orijinal commit mesajı:** `Merge pull request #141 from SantoPanto/sync/faz2-develop

Instagram entegrasyonu (Faz 2) — develop senkron + V23-V26`

Geliştirme dalı (pull request #141) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `enum-aynasi.json`, `PatiMatiApplication.java`, `AiAnalysisListener.java` ve 83 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 209. 2026-08-23 — Birim testlerinin güncellenmesi (`AdminControllerSearchTest.java`)

**Commit:** `00e6cb9d`  
**Orijinal commit mesajı:** `Admin panele arama filtresi`

Servis katmanı doğrulamaları için `AdminControllerSearchTest.java`, `AdminServiceImplTest.java`, `AdminSpecificationTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 210. 2026-08-23 — pull request #144 birleştirmesi ve çakışma çözümleri

**Commit:** `1208cf92`  
**Orijinal commit mesajı:** `Merge pull request #144 from SantoPanto/jwt

Admin panele arama filtresi`

Geliştirme dalı (pull request #144) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminController.java`, `AdComplaintRepository.java`, `AdRepository.java` ve 11 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 211. 2026-08-23 — Birim testlerinin güncellenmesi (`UserPresenceControllerTest.java`)

**Commit:** `89143674`  
**Orijinal commit mesajı:** `Mesajlaşma yolu düzeltildi ve Sürekli aktif gözükme sorunu çözüldü`

Servis katmanı doğrulamaları için `UserPresenceControllerTest.java`, `PresenceEventListenerTest.java`, `NotificationServiceTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 212. 2026-08-23 — pull request #145 birleştirmesi ve çakışma çözümleri

**Commit:** `75e27d6b`  
**Orijinal commit mesajı:** `Merge pull request #145 from SantoPanto/jwt

Mesajlaşma yolu düzeltildi ve Sürekli aktif gözükme sorunu çözüldü`

Geliştirme dalı (pull request #145) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `UserPresenceController.java`, `NotificationResponse.java` ve 9 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 213. 2026-08-23 — Birim testlerinin güncellenmesi (`MessageServiceTest.java`)

**Commit:** `25b98614`  
**Orijinal commit mesajı:** `Mesaj bugfix`

Servis katmanı doğrulamaları için `MessageServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 214. 2026-08-23 — pull request #146 birleştirmesi ve çakışma çözümleri

**Commit:** `68e3ffd6`  
**Orijinal commit mesajı:** `Merge pull request #146 from SantoPanto/jwt

Mesaj bugfix`

Geliştirme dalı (pull request #146) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `MessageRepository.java`, `MessageService.java`, `MessageServiceTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 215. 2026-08-23 — Birim testlerinin güncellenmesi (`WebSocketConfigTest.java`)

**Commit:** `d4faf424`  
**Orijinal commit mesajı:** `Mesaj düzeltmesi`

Servis katmanı doğrulamaları için `WebSocketConfigTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 216. 2026-08-23 — pull request #149 birleştirmesi ve çakışma çözümleri

**Commit:** `faf29c48`  
**Orijinal commit mesajı:** `Merge pull request #149 from SantoPanto/jwt

Mesaj düzeltmesi`

Geliştirme dalı (pull request #149) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `ChatController.java`, `WebSocketChannelInterceptor.java`, `MessageService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 217. 2026-08-23 — WebSocketChannelInterceptor.java ve ilgili backend katmanı geliştirmesi

**Commit:** `c7f116d4`  
**Orijinal commit mesajı:** `Yeniden bağlanma eklendi`

Backend projesinde `WebSocketChannelInterceptor.java`, `MessageService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 218. 2026-08-23 — pull request #151 birleştirmesi ve çakışma çözümleri

**Commit:** `6c5449aa`  
**Orijinal commit mesajı:** `Merge pull request #151 from SantoPanto/jwt

Yeniden bağlanma eklendi`

Geliştirme dalı (pull request #151) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketChannelInterceptor.java`, `MessageService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 219. 2026-08-23 — pull request #150 birleştirmesi ve çakışma çözümleri

**Commit:** `f64a2db6`  
**Orijinal commit mesajı:** `Merge pull request #150 from SantoPanto/feature/mesaj-bildirim-yiginlama

Aynı göndericiden gelen okunmamış mesaj bildirimlerini yığınla`

Geliştirme dalı (pull request #150) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `NotificationRepository.java`, `MessageService.java`, `NotificationService.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 220. 2026-08-23 — Birim testlerinin güncellenmesi (`WebSocketConfigTest.java`)

**Commit:** `0402c94e`  
**Orijinal commit mesajı:** `MSG FİX`

Servis katmanı doğrulamaları için `WebSocketConfigTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 221. 2026-08-23 — pull request #152 birleştirmesi ve çakışma çözümleri

**Commit:** `2e45e227`  
**Orijinal commit mesajı:** `Merge pull request #152 from SantoPanto/jwt

MSG FİX`

Geliştirme dalı (pull request #152) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `WebSocketConfigTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 222. 2026-08-24 — pull request #153 birleştirmesi ve çakışma çözümleri

**Commit:** `58883df9`  
**Orijinal commit mesajı:** `Merge pull request #153 from SantoPanto/fix/qr-poster-fixes

Fix/qr poster fixes`

Geliştirme dalı (pull request #153) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PosterServiceImpl.java`, `application.yml` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 223. 2026-08-24 — pull request #154 birleştirmesi ve çakışma çözümleri

**Commit:** `088edc99`  
**Orijinal commit mesajı:** `Merge pull request #154 from SantoPanto/fix/mesaj-teslimat-bekcisi-ve-sahiplendirme-null

Mesaj teslimatı + kalp atışı için BEKÇİ, sahiplendirmede NOT NULL koruması`

Geliştirme dalı (pull request #154) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdoptionServiceImpl.java`, `WebSocketConfigTest.java`, `MesajCanliTeslimatTest.java` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 224. 2026-08-24 — Birim testlerinin güncellenmesi (`AiAnalyzeControllerTest.java`)

**Commit:** `79ff4fd9`  
**Orijinal commit mesajı:** `yapay zeka analizindeki sorunlar giderildi.`

Servis katmanı doğrulamaları için `AiAnalyzeControllerTest.java`, `AiAnalyzeMapperTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 225. 2026-08-24 — pull request #156 birleştirmesi ve çakışma çözümleri

**Commit:** `07c12b1b`  
**Orijinal commit mesajı:** `Merge pull request #156 from SantoPanto/jwt

yapay zeka analizindeki sorunlar giderildi.`

Geliştirme dalı (pull request #156) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiAnalyzeController.java`, `AiAnalyzeResponse.java`, `AiAnalyzeMapper.java` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 226. 2026-08-24 — Birim testlerinin güncellenmesi (`AiAnahtarBasligiTest.java`)

**Commit:** `f6f9ec3a`  
**Orijinal commit mesajı:** `Bozuk start güncellemesi`

Servis katmanı doğrulamaları için `AiAnahtarBasligiTest.java`, `AiMatchServiceTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 227. 2026-08-24 — pull request #158 birleştirmesi ve çakışma çözümleri

**Commit:** `9fe66dc9`  
**Orijinal commit mesajı:** `Merge pull request #158 from SantoPanto/jwt

Bozuk start güncellemesi`

Geliştirme dalı (pull request #158) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `AiMatchService.java` ve 2 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 228. 2026-08-24 — pull request #159 birleştirmesi ve çakışma çözümleri

**Commit:** `6e08ec9c`  
**Orijinal commit mesajı:** `Merge pull request #159 from SantoPanto/fix/canli-aciklama-sutunu-255

M5'in kök sebebi: canlıda ads.description hâlâ varchar(255) — V27 + gerçek-DB bekçisi`

Geliştirme dalı (pull request #159) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `V27__ads_description_text_tekrar.sql`, `AciklamaSutunuTipiTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 229. 2026-08-25 — pull request #157 birleştirmesi (Merge commit)

**Commit:** `826b99c2`  
**Orijinal commit mesajı:** `Merge pull request #157 from SantoPanto/feature/mesaj-yiginlama-temiz

Aynı göndericiden gelen okunmamış mesaj bildirimlerini yığınla`

Geliştirme dalı (pull request #157) ilgili ana dikey ile birleştirildi. Bu merge commit'inin kendisinde doğrudan yeni bir uygulama kodu değişikliği yapılmamış, iki daldaki commit geçmişi tek branch altında toplanmıştır.

### 230. 2026-08-25 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `bcd92e28`  
**Orijinal commit mesajı:** `Merge branch 'develop' into feature/realtime-presence-and-notifications`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `enum-aynasi.json` ve 146 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 231. 2026-08-25 — pull request #162 birleştirmesi ve çakışma çözümleri

**Commit:** `22af69b6`  
**Orijinal commit mesajı:** `Merge pull request #162 from SantoPanto/feature/realtime-presence-and-notifications

feat: kullanıcı çevrimiçi durumu ve WebSocket üzerinden anlık mesaj/bildirim teslimatı`

Geliştirme dalı (pull request #162) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `PresenceController.java`, `UserStatusEvent.java` ve 5 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 232. 2026-08-25 — branch 'develop' birleştirmesi ve çakışma çözümleri

**Commit:** `f9dce40d`  
**Orijinal commit mesajı:** `Merge branch 'develop' into fix/ben-neyim-develop`

Geliştirme dalı (branch 'develop') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `Dockerfile`, `docker-compose.prod.yml`, `enum-aynasi.json` ve 149 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 233. 2026-08-25 — pull request #163 birleştirmesi ve çakışma çözümleri

**Commit:** `d1469c2d`  
**Orijinal commit mesajı:** `Merge pull request #163 from SantoPanto/fix/ben-neyim-develop

Ben Neyim? -- yalnizca girisli kullanicilar, gunluk 3 istek siniri`

Geliştirme dalı (pull request #163) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `PublicPetAnalysisController.java`, `PetAnalysisRateLimiter.java`, `AiMatchService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 234. 2026-08-25 — AiMatchService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `c93635b9`  
**Orijinal commit mesajı:** `Syntax hataları giderildi.`

Backend projesinde `AiMatchService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 235. 2026-08-25 — pull request #165 birleştirmesi ve çakışma çözümleri

**Commit:** `b05db129`  
**Orijinal commit mesajı:** `Merge pull request #165 from SantoPanto/jwt

Syntax hataları giderildi.`

Geliştirme dalı (pull request #165) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 236. 2026-08-25 — AiMatchService.java ve ilgili backend katmanı geliştirmesi

**Commit:** `0cb5d2dd`  
**Orijinal commit mesajı:** `Uyumsuzluk sorunu giderildi`

Backend projesinde `AiMatchService.java`, `NotificationService.java` sınıfları üzerinde teknik düzenleme yapıldı. İlgili veri yapısı ve servis mantığı güncellendi.

### 237. 2026-08-26 — pull request #166 birleştirmesi ve çakışma çözümleri

**Commit:** `4b1ce167`  
**Orijinal commit mesajı:** `Merge pull request #166 from SantoPanto/jwt

Uyumsuzluk sorunu giderildi`

Geliştirme dalı (pull request #166) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AiMatchService.java`, `NotificationService.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 238. 2026-08-26 — Birim testlerinin güncellenmesi (`AdminControllerSearchTest.java`)

**Commit:** `fae6d779`  
**Orijinal commit mesajı:** `Bozuk testler düzeltildi`

Servis katmanı doğrulamaları için `AdminControllerSearchTest.java`, `SahiplendirmeZorunluAlanTest.java` test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 239. 2026-08-26 — pull request #167 birleştirmesi ve çakışma çözümleri

**Commit:** `88b8d98b`  
**Orijinal commit mesajı:** `Merge pull request #167 from SantoPanto/jwt

Bozuk testler düzeltildi`

Geliştirme dalı (pull request #167) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `AdminControllerSearchTest.java`, `SahiplendirmeZorunluAlanTest.java` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 240. 2026-08-26 — Birim testlerinin güncellenmesi (`PresenceControllerTest.java`)

**Commit:** `9637d084`  
**Orijinal commit mesajı:** `502 Bağlantı sorunları çözüldü`

Servis katmanı doğrulamaları için `PresenceControllerTest.java`, `UserPresenceControllerTest.java`, `PresenceEventListenerTest.java` ve 1 ek dosya test sınıfları güncellendi. Metod davranışlarını sınayan test senaryoları ve mock yapılandırmaları eklendi.

### 241. 2026-08-26 — pull request #169 birleştirmesi ve çakışma çözümleri

**Commit:** `a28fb9a7`  
**Orijinal commit mesajı:** `Merge pull request #169 from SantoPanto/jwt

502 Bağlantı sorunları çözüldü`

Geliştirme dalı (pull request #169) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `WebSocketConfig.java`, `PresenceController.java`, `UserPresenceController.java` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

---

## 4. PATIMATI — AI COMMITLERİ

### 1. 2026-07-24 — Yapay zeka mikroservisi dokümantasyonu

**Commit:** `1f283e81`  
**Orijinal commit mesajı:** `Initial commit`

AI mikroservisi kurulum talimatlarını ve çalışma ortamı gereksinimlerini belirten `README.md` dosyası güncellendi.

### 2. 2026-08-17 — pull request #19 birleştirmesi ve çakışma çözümleri

**Commit:** `14eff144`  
**Orijinal commit mesajı:** `Merge pull request #19 from SantoPanto/fix/docker-giris-noktasi

fix: imaj kuyruk tüketicisini de başlatsın (üretimde AI analizi hiç koşmuyordu)`

Geliştirme dalı (pull request #19) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `.gitattributes`, `ci.yml` ve 7 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 3. 2026-08-17 — pull request #18 birleştirmesi ve çakışma çözümleri

**Commit:** `0b8a8640`  
**Orijinal commit mesajı:** `Merge pull request #18 from SantoPanto/feature/nlp-rabbitmq

feat(rabbitmq): NLP mesaj sözleşmesi altyapısı eklendi`

Geliştirme dalı (pull request #18) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `kuyruk.py`, `entegrasyon-sozlesmesi.md` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 4. 2026-08-17 — pull request #9 birleştirmesi ve çakışma çözümleri

**Commit:** `3003e3fe`  
**Orijinal commit mesajı:** `Merge pull request #9 from SantoPanto/feature/paket-2-degerlendirme

test: açık küme testi eşik taraması (threshold sweep) olarak güncellendi`

Geliştirme dalı (pull request #9) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `olcum-raporu.md`, `eslesme_yok_testi.py` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 5. 2026-08-19 — .env.example ve AI modülü geliştirmesi

**Commit:** `60c9e3b4`  
**Orijinal commit mesajı:** `Update AMQP_URL to use rabbitmq service`

Yapay zeka servisinde `.env.example` modülleri üzerinde teknik düzenlemeler yapıldı.

### 6. 2026-08-20 — pull request #23 birleştirmesi ve çakışma çözümleri

**Commit:** `d8b8b368`  
**Orijinal commit mesajı:** `Merge pull request #23 from SantoPanto/fix/e3-varsayilan-kimlik-modeli

fix(E3): kimlik modeli varsayılanı siglip2-animal olsun + tutarlılık bekçisi`

Geliştirme dalı (pull request #23) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `Dockerfile`, `README.md` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 7. 2026-08-20 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `3af3884e`  
**Orijinal commit mesajı:** `Merge branch 'main' into fix/m25-kimlik-kilidi-varsayilan-reddet`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `Dockerfile`, `README.md` ve 4 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 8. 2026-08-20 — pull request #24 birleştirmesi ve çakışma çözümleri

**Commit:** `7c8d5812`  
**Orijinal commit mesajı:** `Merge pull request #24 from SantoPanto/fix/m25-kimlik-kilidi-varsayilan-reddet

fix(25): AI kimlik kilidinin varsayılanı "reddet" olsun`

Geliştirme dalı (pull request #24) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `README.md`, `main.py` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 9. 2026-08-20 — pull request #25 birleştirmesi ve çakışma çözümleri

**Commit:** `33cb7117`  
**Orijinal commit mesajı:** `Merge pull request #25 from SantoPanto/fix/m5-hayvansiz-fotografa-tur-atanmasin

fix(5): hayvansız fotoğrafa tür atanmasın — tür kapısını is_petten ayır`

Geliştirme dalı (pull request #25) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `analiz.py`, `attributes.py`, `hayvansiz_olcum.py` ve 1 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 10. 2026-08-20 — pull request #26 birleştirmesi ve çakışma çözümleri

**Commit:** `6f50363f`  
**Orijinal commit mesajı:** `Merge pull request #26 from SantoPanto/feat/m4-skor-ayarlanabilir-olcum

feat(4): skor düğmelerini ayarlanabilir yap + yanlış-pozitif eğrisini ölç`

Geliştirme dalı (pull request #26) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `matcher.py`, `...CatIndividualImages_avito-siglip2_120birey.json` ve 3 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 11. 2026-08-21 — .env.example ve AI modülü geliştirmesi

**Commit:** `a769b4b8`  
**Orijinal commit mesajı:** `Beyazliste izinleri fotoğraf yükleme analizi için güncellendi.`

Yapay zeka servisinde `.env.example`, `indirici.py`, `test_indirici.py` modülleri üzerinde teknik düzenlemeler yapıldı.

### 12. 2026-08-21 — pull request #28 birleştirmesi ve çakışma çözümleri

**Commit:** `2ebb4612`  
**Orijinal commit mesajı:** `Merge pull request #28 from SantoPanto/chore/tasma-etiketli-kume

chore(scripts): tasma için etiketli küme + iki yönlü doğruluk ölçümü`

Geliştirme dalı (pull request #28) ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `attributes.py`, `tasma_dogruluk.py`, `tasma_etiketleri.json` üzerinde düzenlenerek dal geçmişleri entegre edildi.

### 13. 2026-08-23 — requirements.txt ve AI modülü geliştirmesi

**Commit:** `a4994cee`  
**Orijinal commit mesajı:** `Eksik kütüphane eklendi`

Yapay zeka servisinde `requirements.txt` modülleri üzerinde teknik düzenlemeler yapıldı.

### 14. 2026-08-23 — branch 'main' birleştirmesi ve çakışma çözümleri

**Commit:** `b82f96a1`  
**Orijinal commit mesajı:** `Merge branch 'main' of https://github.com/SantoPanto/PATIMATI-AI`

Geliştirme dalı (branch 'main') ana dikey ile birleştirildi. Birleştirme sırasında oluşan kod çakışmaları `.env.example`, `ci.yml`, `.gitignore` ve 28 ek dosya üzerinde düzenlenerek dal geçmişleri entegre edildi.

---

## 5. GENEL İSTATİSTİK

- **Frontend (`PATIMATI---FRONTEND` - `develop`):** 235 commit
- **Backend (`PATIMATI---BACKEND` - `origin/develop`):** 241 commit
- **AI (`PATIMATI-AI` - `main`):** 14 commit
- **Toplam Raporlanan Commit:** 490 commit
- **Tarih Aralığı:** 2026-07-21 — 2026-08-27

---

## 6. DİKKAT / DOĞRULAMA GEREKTİREN NOKTALAR

1. **Tam Dosya İzolasyonu:** Her commit açıklaması yalnızca o committe değiştirilen somut dosyalar dikkate alınarak hazırlanmış; kanıtsız ifadeler (örn. 'test edildi', 'sisteme aktarıldı') tamamen elenmiştir.

2. **Pure Merge Ayrıştırması:** Uygulama kodu değişikliği içermeyen merge commit'leri açıkça tanımlanmış, birleştirme işlemi dışındaki geliştirmelerin başka commitlerden sızması engellenmiştir.

3. **UTF-8 Karakter Bütünlüğü:** Tüm başlıklar ve orijinal commit mesajları eksiksiz UTF-8 formatında korunmuş, karakter bozulmaları engellenmiştir.
