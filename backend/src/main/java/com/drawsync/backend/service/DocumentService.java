package com.drawsync.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
    private final Cloudinary cloudinary;

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
}