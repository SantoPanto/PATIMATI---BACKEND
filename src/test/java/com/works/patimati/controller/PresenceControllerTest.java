package com.works.patimati.controller;

import com.works.patimati.presence.PresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PresenceControllerTest {

    private PresenceService presenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        presenceService = mock(PresenceService.class);
        PresenceController controller = new PresenceController(presenceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsOnlineStatusForApiUsers() throws Exception {
        when(presenceService.isOnline(5L)).thenReturn(true);

        mockMvc.perform(get("/api/users/5/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.online").value(true))
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.lastSeen").doesNotExist());
    }

    @Test
    void returnsOfflineStatusForApiV1Users() throws Exception {
        Instant now = Instant.parse("2026-08-26T10:00:00Z");
        when(presenceService.isOnline(10L)).thenReturn(false);
        when(presenceService.getLastSeen(10L)).thenReturn(now);

        mockMvc.perform(get("/api/v1/users/10/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.online").value(false))
                .andExpect(jsonPath("$.status").value("OFFLINE"))
                .andExpect(jsonPath("$.lastSeen").value("2026-08-26T10:00:00Z"));
    }
}
