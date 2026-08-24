package com.works.patimati.controller;

import com.works.patimati.dto.ChangePasswordRequest;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.service.PasswordResetService;
import com.works.patimati.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService authService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void changePassword_ShouldReturnOk_WhenValidRequest() throws Exception {
        doReturn(ResponseEntity.ok(Map.of("success", true, "message", "Şifreniz başarıyla değiştirildi.")))
                .when(authService).changePassword(any(ChangePasswordRequest.class));

        String requestJson = """
                {
                    "oldPassword": "oldPassword123",
                    "newPassword": "newPassword123"
                }
                """;

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, times(1)).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_ShouldReturnBadRequest_WhenInvalidPayload() throws Exception {
        String requestJson = """
                {
                    "oldPassword": "",
                    "newPassword": "123"
                }
                """;

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
