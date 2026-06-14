package com.drawsync.backend.service;

import com.drawsync.backend.model.Document;
import com.drawsync.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public Document uploadPdf(String roomId,
                              String uploadedBy,
                              MultipartFile file) throws Exception {

        PDDocument pdf = Loader.loadPDF(file.getBytes());

        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(pdf);

        pdf.close();

        Document document = new Document();
        document.setRoomId(roomId);
        document.setFileName(file.getOriginalFilename());
        document.setUploadedBy(uploadedBy);
        document.setContent(content);

        return documentRepository.save(document);
    }

    public List<Document> getDocumentsByRoom(String roomId) {
        return documentRepository.findByRoomId(roomId);
    }
}