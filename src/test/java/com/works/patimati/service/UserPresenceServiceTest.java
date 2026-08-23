package com.works.patimati.service;

import com.works.patimati.dto.UserStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPresenceServiceTest {

    private UserPresenceService userPresenceService;

    @BeforeEach
    void setUp() {
        userPresenceService = new UserPresenceService();
    }

    @Test
    void userIsOfflineByDefault() {
        assertThat(userPresenceService.isUserOnline(1L)).isFalse();
        UserStatusResponse status = userPresenceService.getUserStatus(1L);
        assertThat(status.userId()).isEqualTo(1L);
        assertThat(status.isOnline()).isFalse();
    }

    @Test
    void markConnectedTransitionsStatusToOnlineOnFirstSession() {
        boolean stateChanged = userPresenceService.markConnected(1L, "session-1");
        assertThat(stateChanged).isTrue();
        assertThat(userPresenceService.isUserOnline(1L)).isTrue();
    }

    @Test
    void markConnectedDoesNotChangeStatusOnAdditionalSessions() {
        userPresenceService.markConnected(1L, "session-1");
        boolean stateChanged = userPresenceService.markConnected(1L, "session-2");
        assertThat(stateChanged).isFalse();
        assertThat(userPresenceService.isUserOnline(1L)).isTrue();
    }

    @Test
    void markDisconnectedKeepsUserOnlineIfOtherSessionsExist() {
        userPresenceService.markConnected(1L, "session-1");
        userPresenceService.markConnected(1L, "session-2");

        boolean stateChanged = userPresenceService.markDisconnected(1L, "session-1");
        assertThat(stateChanged).isFalse();
        assertThat(userPresenceService.isUserOnline(1L)).isTrue();
    }

    @Test
    void markDisconnectedTransitionsStatusToOfflineWhenLastSessionClosed() {
        userPresenceService.markConnected(1L, "session-1");

        boolean stateChanged = userPresenceService.markDisconnected(1L, "session-1");
        assertThat(stateChanged).isTrue();
        assertThat(userPresenceService.isUserOnline(1L)).isFalse();
    }

    @Test
    void handlesNullInputsGracefully() {
        assertThat(userPresenceService.markConnected(null, "session-1")).isFalse();
        assertThat(userPresenceService.markConnected(1L, null)).isFalse();
        assertThat(userPresenceService.markDisconnected(null, "session-1")).isFalse();
        assertThat(userPresenceService.markDisconnected(1L, null)).isFalse();
        assertThat(userPresenceService.isUserOnline(null)).isFalse();
    }
}
