package com.works.patimati.controller;

import com.works.patimati.dto.ChangePasswordRequest;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.service.PasswordResetService;
import com.works.patimati.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Şifre değiştirme endpointinin HTTP sözleşmesini test eder.
 */
class UserControllerPasswordChangeTest {

    private static final String USER_EMAIL = "kullanici@patimati.me";

    private UserService userService;
    private PasswordResetService passwordResetService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        passwordResetService = mock(PasswordResetService.class);

        UserController controller = new UserController(
                userService,
                passwordResetService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnNoContentWhenPasswordIsChanged() throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        USER_EMAIL,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        mockMvc.perform(
                        put("/api/auth/change-password")
                                // Controller, kullanıcı kimliğini bu Authentication'dan alır.
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "currentPassword": "MevcutSifre1",
                                          "newPassword": "YeniSifre1",
                                          "confirmPassword": "YeniSifre1"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        ArgumentCaptor<ChangePasswordRequest> requestCaptor =
                ArgumentCaptor.forClass(ChangePasswordRequest.class);

        verify(userService).changePassword(
                eq(USER_EMAIL),
                requestCaptor.capture()
        );

        ChangePasswordRequest capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getCurrentPassword())
                .isEqualTo("MevcutSifre1");

        assertThat(capturedRequest.getNewPassword())
                .isEqualTo("YeniSifre1");

        assertThat(capturedRequest.getConfirmPassword())
                .isEqualTo("YeniSifre1");
    }

    @Test
    void shouldRejectInvalidPasswordBeforeCallingService() throws Exception {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        USER_EMAIL,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        mockMvc.perform(
                        put("/api/auth/change-password")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "currentPassword": "MevcutSifre1",
                                          "newPassword": "zayif",
                                          "confirmPassword": "zayif"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        // DTO doğrulaması başarısızsa servis katmanına ulaşılmamalıdır.
        verifyNoInteractions(userService);
    }
}