package com.works.patimati.controller;

import com.works.patimati.dto.UserStatusResponse;
import com.works.patimati.service.UserPresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserPresenceControllerTest {

    private UserPresenceService userPresenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userPresenceService = mock(UserPresenceService.class);
        UserPresenceController controller = new UserPresenceController(userPresenceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsUserPresenceStatus() throws Exception {
        when(userPresenceService.getUserStatus(5L))
                .thenReturn(new UserStatusResponse(5L, true));

        mockMvc.perform(get("/api/users/5/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.isOnline").value(true));
    }
}
