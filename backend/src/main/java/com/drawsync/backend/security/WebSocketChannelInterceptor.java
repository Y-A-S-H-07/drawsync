package com.drawsync.backend.security;

import com.drawsync.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Check if this is an incoming client transmission connection or action publish frame
        if (accessor != null) {

            // 1. Parse connection headers to authenticate the user's role on WebSocket upgrade
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        String email = jwtService.extractEmail(token);
                        String role = jwtService.extractRole(token);

                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        UsernamePasswordAuthenticationToken userAuth =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);

                        // Attach the credential context directly to the active STOMP pipeline session
                        accessor.setUser(userAuth);
                    } catch (Exception e) {
                        throw new AccessDeniedException("Invalid Token Signature. WebSocket connection rejected.");
                    }
                }
            }

            // 2. ENFORCE RBAC ON INCOMING FRAMES: Block read-only VIEWERS from writing to endpoints
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                java.security.Principal userPrincipal = accessor.getUser();

                if (userPrincipal instanceof UsernamePasswordAuthenticationToken) {
                    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) userPrincipal;

                    // Look up assigned authority roles
                    boolean isViewer = authToken.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER"));

                    // ✅ If a client is a read-only VIEWER, block them from publishing drawings or text chat!
                    if (isViewer && destination != null && (destination.startsWith("/app/board") || destination.startsWith("/app/chat"))) {
                        System.err.println("❌ RBAC Violation: Read-only user tried to broadcast modifications!");
                        throw new AccessDeniedException("Access Denied: Read-only viewers cannot modify the workspace canvas.");
                    }
                }
            }
        }

        return message;
    }
}