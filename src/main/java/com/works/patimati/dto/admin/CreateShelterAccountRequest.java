package com.works.patimati.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Admin'in bir barınak hesabı açması için gönderdiği istek --
 * {@link CreatePetShopAccountRequest}'in birebir kopyası. Self-servis kayıt
 * YOK, barınak hesapları yalnızca bu uçtan (bkz. AdminController) açılır.
 * Barınak bilgi kartı burada OLUŞTURULMAZ -- adres/telefon gibi zorunlu
 * alanları olduğu için sahibi ilk girişte {@code /barinak/panel}'den
 * kendisi girer.
 */
public record CreateShelterAccountRequest(

        @NotBlank(message = "Ad zorunludur")
        @Size(min = 2, max = 40, message = "Ad 2 ile 40 karakter arasında olmalıdır")
        String firstName,

        @NotBlank(message = "Soyad zorunludur")
        @Size(min = 2, max = 40, message = "Soyad 2 ile 40 karakter arasında olmalıdır")
        String lastName,

        @NotBlank(message = "E-posta zorunludur")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        @Size(max = 50, message = "E-posta en fazla 50 karakter olabilir")
        String email,

        @NotBlank(message = "Şifre zorunludur")
        @Size(min = 8, max = 20, message = "Şifre 8 ile 20 karakter arasında olmalıdır")
        @Pattern(
                regexp = "^(?=.*[A-ZÇĞİÖŞÜ])(?=.*[a-zçğıöşü])(?=.*\\d).+$",
                message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
        )
        String password
) {
}
