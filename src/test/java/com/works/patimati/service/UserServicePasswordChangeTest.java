package com.works.patimati.service;

import com.works.patimati.dto.ChangePasswordRequest;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Şifre değiştirme işleminin iş kurallarını test eder.
 * Bu testler gerçek veritabanı kullanmaz. Repository ve şifreleme
 * bileşeni Mockito ile taklit edilerek yalnızca UserService davranışı sınanır.
 */
@ExtendWith(MockitoExtension.class)
class UserServicePasswordChangeTest {

    private static final String USER_EMAIL = "kullanici@patimati.me";
    private static final String CURRENT_PASSWORD = "MevcutSifre1";
    private static final String NEW_PASSWORD = "YeniSifre1";
    private static final String ENCODED_CURRENT_PASSWORD = "encoded-current-password";
    private static final String ENCODED_NEW_PASSWORD = "encoded-new-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldChangePasswordWhenRequestIsValid() {
        User user = userWithPassword();
        ChangePasswordRequest request = validRequest();

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        // Mevcut şifre doğru kabul edilir.
        when(passwordEncoder.matches(
                CURRENT_PASSWORD,
                ENCODED_CURRENT_PASSWORD
        )).thenReturn(true);

        // Yeni şifrenin mevcut şifreden farklı olduğu doğrulanır.
        when(passwordEncoder.matches(
                NEW_PASSWORD,
                ENCODED_CURRENT_PASSWORD
        )).thenReturn(false);

        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(ENCODED_NEW_PASSWORD);

        userService.changePassword(USER_EMAIL, request);

        // Veritabanına düz metin yerine hash'lenmiş şifre yazılmalıdır.
        assertThat(user.getPassword())
                .isEqualTo(ENCODED_NEW_PASSWORD);

        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectRequestWhenCurrentPasswordIsWrong() {
        User user = userWithPassword();
        ChangePasswordRequest request = validRequest();

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                CURRENT_PASSWORD,
                ENCODED_CURRENT_PASSWORD
        )).thenReturn(false);

        assertThatThrownBy(() ->
                userService.changePassword(USER_EMAIL, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mevcut şifre hatalı.");

        // Hatalı mevcut şifrede yeni şifre üretilmemeli ve kayıt yapılmamalıdır.
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectRequestWhenPasswordConfirmationDoesNotMatch() {
        ChangePasswordRequest request = validRequest();
        request.setConfirmPassword("FarkliSifre1");

        assertThatThrownBy(() ->
                userService.changePassword(USER_EMAIL, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Yeni şifre ve şifre tekrarı birbiriyle eşleşmiyor."
                );

        /*
         * Şifreler birbiriyle eşleşmiyorsa veritabanına gitmeden
         * işlem sonlandırılmalıdır.
         */
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectRequestWhenNewPasswordIsSameAsCurrentPassword() {
        User user = userWithPassword();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(CURRENT_PASSWORD);
        request.setNewPassword(CURRENT_PASSWORD);
        request.setConfirmPassword(CURRENT_PASSWORD);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        /*
         * Aynı düz metin şifre hem mevcut şifre kontrolünde hem de
         * yeni şifrenin farklılığı kontrolünde eşleşir.
         */
        when(passwordEncoder.matches(
                CURRENT_PASSWORD,
                ENCODED_CURRENT_PASSWORD
        )).thenReturn(true);

        assertThatThrownBy(() ->
                userService.changePassword(USER_EMAIL, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Yeni şifre mevcut şifreden farklı olmalıdır."
                );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectPasswordChangeForGoogleOnlyAccount() {
        User googleUser = User.builder()
                .email(USER_EMAIL)
                .googleId("google-user-id")
                .password(null)
                .build();

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() ->
                userService.changePassword(USER_EMAIL, validRequest())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Google ile oluşturulan hesaplarda mevcut şifre değiştirilemez."
                );

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    private User userWithPassword() {
        return User.builder()
                .email(USER_EMAIL)
                .password(ENCODED_CURRENT_PASSWORD)
                .build();
    }

    private ChangePasswordRequest validRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(CURRENT_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD);
        return request;
    }
}