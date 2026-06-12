package com.drawsync.backend.controller;

import com.drawsync.backend.model.User;
import com.drawsync.backend.service.JwtService;
import com.drawsync.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody User user) {
        User savedUser = userService.register(user);

        // ✅ FIX: Extract the first role name string from the Set collection safely
        String roleStr = savedUser.getRoles().stream()
                .map(Enum::name)
                .findFirst()
                .orElse("EDITOR");

        String token = jwtService.generateToken(savedUser.getEmail(), roleStr);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", savedUser);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        User existingUser = userService.login(user.getEmail(), user.getPassword());

        // ✅ FIX: Extract the first role name string from the Set collection safely
        String roleStr = existingUser.getRoles().stream()
                .map(Enum::name)
                .findFirst()
                .orElse("EDITOR");

        String token = jwtService.generateToken(existingUser.getEmail(), roleStr);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", existingUser);
        return response;
    }
    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        return "User: " + request.getAttribute("userEmail");
    }
}