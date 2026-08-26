package com.works.patimati.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yöneticinin bir kullanıcıyı kurum (belediye) hesabına yükseltirken verdiği bilgiler.
 *
 * <p><b>Neden serbest kayıt değil de yönetici eli:</b> kendini belediye ilan
 * edebilen bir hesap, o ilçenin bütün ihbarlarını ve ilan verisini okurdu.
 * Doğrulama formu bu turun kapsamı dışında bırakıldı (plan §8), yerine elle
 * onay kondu.
 *
 * <p><b>İlçe yazımı önemlidir:</b> panel sorguları bu değeri {@code ads.district}
 * ile kıyaslayacak; oradaki değerleri Nominatim Türkçe olarak dolduruyor
 * ("Nilüfer", "Çankaya"). Kıyas büyük/küçük harf duyarsız yapılıyor, ama
 * "Nilufer" gibi harfi değişmiş bir yazım eşleşmez.
 */
public record InstitutionAssignmentRequest(

        @NotBlank(message = "Kurum adı boş olamaz")
        @Size(max = 150, message = "Kurum adı en fazla 150 karakter olabilir")
        String institutionName,

        @NotBlank(message = "İl boş olamaz")
        @Size(max = 100, message = "İl en fazla 100 karakter olabilir")
        String institutionCity,

        @NotBlank(message = "İlçe boş olamaz — panelin kapsamı bu alandan türetiliyor")
        @Size(max = 100, message = "İlçe en fazla 100 karakter olabilir")
        String institutionDistrict
) {
}
