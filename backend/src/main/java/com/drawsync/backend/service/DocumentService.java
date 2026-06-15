package com.drawsync.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.drawsync.backend.model.ChatMessage;
import com.drawsync.backend.model.Document;
import com.drawsync.backend.model.Room;
import com.drawsync.backend.repository.ChatMessageRepository;
import com.drawsync.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drawsync.backend.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final Cloudinary cloudinary;
    private final AiService aiService;

    private final ChatMessageRepository chatMessageRepository;

    private final RoomRepository roomRepository;




    public Document uploadPdf(
            String roomId,
            String uploadedBy,
            MultipartFile file
    ) throws Exception {

        // Extract PDF text
        PDDocument pdf = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(pdf);

        pdf.close();

        System.out.println("Uploading to Cloudinary...");

        String fileUrl;

        try {

            var uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "drawsync-pdfs"
                    )
            );

            System.out.println("Cloudinary Upload Success:");
            System.out.println(uploadResult);

            fileUrl = uploadResult.get("secure_url").toString();

        } catch (Exception e) {

            System.out.println("Cloudinary Upload Failed:");
            e.printStackTrace();

            throw e;
        }

        Document document = new Document();
        document.setRoomId(roomId);
        document.setFileName(file.getOriginalFilename());
        document.setUploadedBy(uploadedBy);
        document.setContent(content);
        document.setFileUrl(fileUrl);

        return documentRepository.save(document);
    }

    public List<Document> getDocumentsByRoom(String roomId) {
        return documentRepository.findByRoomId(roomId);
    }

    public String askQuestion(String roomId, String question) {

        List<Document> documents = documentRepository.findByRoomId(roomId);

        if (documents.isEmpty()) {
            return "No documents found in this room.";
        }

        StringBuilder context = new StringBuilder();

        for (Document document : documents) {
            context.append(document.getContent())
                    .append("\n\n");
        }

        String prompt = """
            Answer the question only using the document content below.

            DOCUMENT:
            %s

            QUESTION:
            %s
            """
                .formatted(context.toString(), question);

        return aiService.ask(prompt);
    }

    public String generateSummary(String roomId) {

        List<ChatMessage> messages =
                chatMessageRepository.findByRoomId(roomId);


        List<Document> documents =
                documentRepository.findByRoomId(roomId);

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));



        String boardData = room.getBoardData();

        if (documents.isEmpty()) {
            return "No documents found.";
        }

        StringBuilder context = new StringBuilder();

        for (Document document : documents) {
            context.append(document.getContent())
                    .append("\n\n");
        }

        StringBuilder chatContext = new StringBuilder();

        for (ChatMessage message : messages) {

            chatContext.append(message.getSender())
                    .append(": ")
                    .append(message.getMessage())
                    .append("\n");
        }

        String prompt = """
Summarize this collaboration session.

Include:

1. Important document information
2. Important chat discussion
3. Main topics discussed
4. Whiteboard activity

DOCUMENTS:
%s

CHAT:
%s

WHITEBOARD DATA:
%s
"""
                .formatted(
                        context.toString(),
                        chatContext.toString(),
                        boardData
                );

        return aiService.ask(prompt);
    }



}