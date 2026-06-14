package com.drawsync.backend.repository;

import com.drawsync.backend.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByRoomId(String roomId);
}