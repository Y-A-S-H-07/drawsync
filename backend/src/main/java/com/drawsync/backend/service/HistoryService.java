package com.drawsync.backend.service;

import com.drawsync.backend.model.Room;
import com.drawsync.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final RoomRepository roomRepository;

    public Map<String, Object> getHistory(String email) {

        List<Room> allRooms = roomRepository.findAll();

        List<Room> roomsCreated = allRooms.stream()
                .filter(room -> email.equals(room.getHostEmail()))
                .toList();

        List<Room> roomsJoined = allRooms.stream()
                .filter(room ->
                        room.getUsers().stream()
                                .anyMatch(user -> email.equals(user.getEmail())))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("roomsCreated", roomsCreated);
        response.put("roomsJoined", roomsJoined);

        return response;
    }

    public Map<String, Object> getRoomHistory(String roomId) {

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("objectData", room);

        return response;
    }
}