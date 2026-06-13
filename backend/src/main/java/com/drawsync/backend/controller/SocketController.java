package com.drawsync.backend.controller;

import com.drawsync.backend.model.Room;
import com.drawsync.backend.model.UserInfo;
import com.drawsync.backend.repository.RoomRepository;
import com.drawsync.backend.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/board")
    public void handleBoardUpdate(java.util.Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Object boardData = payload.get("boardData");

        messagingTemplate.convertAndSend(
                "/topic/board/" + roomId,
                boardData
        );

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        try {
            String jsonString = new ObjectMapper().writeValueAsString(boardData);
            room.setBoardData(jsonString);
            roomRepository.save(room);
        } catch (Exception e) {
            System.err.println("Error saving board data: " + e.getMessage());
        }
    }

    @MessageMapping("/join")
    public void joinRoom(java.util.Map<String, Object> payload, java.security.Principal principal) {
        String roomId = (String) payload.get("roomId");

        var roomOptional = roomRepository.findByRoomId(roomId);
        if (roomOptional.isEmpty()) {
            System.err.println("⚠️ Client tried to join non-existent room ID: " + roomId);

            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Room does not exist");

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId,
                    (Object) errorResponse
            );
            return;
        }
        Room room = roomOptional.get();

        String userEmail = principal != null ? principal.getName() : "Anonymous";

        String fullName = userRepository.findByEmail(userEmail)
                .map(com.drawsync.backend.model.User::getFullName)
                .orElse(userEmail.split("@")[0]);

        boolean exists = room.getUsers().stream()
                .anyMatch(u -> u.getEmail().equals(userEmail));

        if (!exists) {
            room.getUsers().add(
                    new UserInfo(
                            userEmail,
                            fullName,
                            ""
                    )
            );

            roomRepository.save(room);
        }
        java.util.Map<String, Object> currentUser = new java.util.HashMap<>();
        currentUser.put("userId", userEmail);
        currentUser.put("name", fullName);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("type", "JOINED");
        response.put("currentUser", currentUser);
        java.util.List<java.util.Map<String, Object>> users = room.getUsers()
                .stream()
                .map(u -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("userId", u.getEmail());
                    map.put("name", u.getName());
                    return map;
                })
                .toList();
        System.out.println("ROOM USERS COUNT = " + room.getUsers().size());
        response.put("users", users);
        response.put("host", room.getHostEmail());
        response.put("boardData", room.getBoardData());

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                (Object)response
        );
    }

    @MessageMapping("/permission")
    public void handlePermissionUpdate(java.util.Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        messagingTemplate.convertAndSend(
                "/topic/permission/" + roomId,
                (Object) payload
        );
    }

    @MessageMapping("/chat")
    public void handleChatMessage(java.util.Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Object messageData = payload.get("message"); // Extracts text, userName, userId

        System.out.println("" +
                "Broad0casting message to room: " + roomId);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                messageData
        );
    }
}