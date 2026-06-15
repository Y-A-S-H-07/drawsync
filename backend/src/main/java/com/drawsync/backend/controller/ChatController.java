package com.drawsync.backend.controller;

import com.drawsync.backend.model.ChatMessage;
import com.drawsync.backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/{roomId}")
    public List<ChatMessage> getMessages(
            @PathVariable String roomId
    ) {
        return chatMessageRepository.findByRoomId(roomId);
    }
}