package com.works.patimati.dto.message;

public record ChatRoomWithAdResponseDTO(
        ChatRoomResponseDTO room,
        MessageResponse sharedMessage
) {
}
