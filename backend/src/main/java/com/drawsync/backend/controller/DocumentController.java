package com.drawsync.backend.controller;

import com.drawsync.backend.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{roomId}")
    public Object uploadPdf(
            @PathVariable String roomId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) throws Exception {

        String email = (String) request.getAttribute("userEmail");

        return documentService.uploadPdf(
                roomId,
                email,
                file
        );
    }

    @GetMapping("/{roomId}")
    public Object getDocuments(@PathVariable String roomId) {
        return documentService.getDocumentsByRoom(roomId);
    }
}