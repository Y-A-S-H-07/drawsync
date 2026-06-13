package com.drawsync.backend.controller;

import com.drawsync.backend.service.HistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public Object getHistory(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        return historyService.getHistory(email);
    }

    @GetMapping("/{roomId}")
    public Object getRoomHistory(@PathVariable String roomId) {
        return historyService.getRoomHistory(roomId);
    }
}