# 🚀 DrawSync - AI-Powered Collaborative Workspace

## 📌 Overview

DrawSync is a full-stack collaborative workspace platform designed to enable real-time teamwork through a shared digital whiteboard, live chat, document collaboration, and AI-assisted knowledge extraction.

The platform allows multiple users to join virtual rooms, collaborate on a synchronized whiteboard, communicate through real-time messaging, upload PDF documents, ask AI-powered questions about uploaded content, and generate intelligent summaries of entire collaboration sessions.

Built using React, Spring Boot, PostgreSQL, WebSockets, Spring Security, JWT Authentication, Spring AI, and Cloudinary, DrawSync combines collaborative drawing, communication, and AI capabilities into a single workspace.

---

## ✨ Key Features

### 🔐 Authentication & Security

* User Registration and Login
* JWT-Based Authentication
* Spring Security Integration
* Role-Based Access Control (RBAC)
* WebSocket Authentication
* Secure Protected APIs
* Viewer and Editor Permissions

---

### 🎨 Real-Time Collaborative Whiteboard

Multiple users can work on the same canvas simultaneously.

Supported tools:

* Select Tool
* Pencil Tool
* Rectangle Tool
* Circle Tool
* Line Tool
* Text Tool
* Eraser Tool
* Highlighter Tool

Customization Features:

* Stroke Color Selection
* Fill Color Selection
* Stroke Width Adjustment

Advanced Editing:

* Undo
* Redo
* Duplicate Objects
* Delete Objects
* Group Objects
* Ungroup Objects
* Object Selection & Modification

---

### 👥 Room-Based Collaboration

Users collaborate inside dedicated rooms.

Features include:

* Create Room
* Join Room
* Room Validation
* Unique Room IDs
* Host Management
* User Presence Tracking
* Shared Collaboration Sessions

---

### ⚡ Real-Time Synchronization

All whiteboard actions are synchronized instantly using STOMP WebSockets.

Whenever a user:

* Draws
* Edits
* Deletes
* Creates shapes
* Adds text

the changes are broadcast to every participant in the room in real time.

---

### 💬 Live Chat System

Integrated room-based chat allows participants to communicate while collaborating.

Features:

* Real-Time Messaging
* Persistent Chat Storage
* Message History Retrieval
* Sender Identification
* Timestamp Tracking

---

### 💾 Persistent Whiteboard Storage

DrawSync stores the complete canvas state in PostgreSQL.

Benefits:

* Session Recovery
* Room Restoration
* Whiteboard Persistence
* Rejoining Previous Sessions

The canvas is serialized into JSON and restored whenever users rejoin a room.

---

### 📄 Document Collaboration

Users can upload PDF documents directly into collaboration rooms.

Features:

* PDF Upload
* Cloudinary File Storage
* Document Repository
* Room-Specific Document Collections
* Multi-Document Support

---

### 🤖 AI-Powered Document Question Answering

Uploaded PDF content is extracted using PDFBox and processed through Spring AI.

Users can ask:

> "What are the key concepts in this document?"

> "Explain chapter 3."

> "Summarize the project requirements."

The AI generates answers using the uploaded document content as context.

---

### 🧠 AI Collaboration Summary

DrawSync can generate an intelligent summary of an entire collaboration session.

The summary combines:

* Uploaded Documents
* Chat Discussions
* Whiteboard Content

Generated summaries include:

* Main Topics Discussed
* Important Decisions
* Key Information
* Document Insights
* Whiteboard Activities

---

### 📜 Session History

Users can access:

* Rooms Created
* Rooms Joined
* Previous Collaboration Sessions

This enables quick navigation between historical workspaces.

---

### ☁️ Cloud Storage Integration

PDF documents are stored securely using Cloudinary.

Benefits:

* Reliable File Storage
* Fast Retrieval
* Secure Access
* Scalable Infrastructure

---

## 🏗️ System Architecture

```text
                    +------------------+
                    |      React       |
                    |     Frontend     |
                    +---------+--------+
                              |
                              |
                   REST APIs & JWT
                              |
                              v
+---------------------------------------------------+
|               Spring Boot Backend                 |
|---------------------------------------------------|
| Authentication Service                            |
| Room Service                                      |
| Whiteboard Service                                |
| Chat Service                                      |
| History Service                                   |
| Document Service                                  |
| AI Service                                        |
| WebSocket Security Layer                          |
+---------------------------------------------------+
                              |
                              |
                  STOMP WebSocket Broker
                              |
                              |
                              v
                     Real-Time Collaboration

                              |
                              |
                              v

+---------------------------------------------------+
|                    PostgreSQL                     |
|---------------------------------------------------|
| Users                                             |
| Rooms                                             |
| Documents                                         |
| Chat Messages                                     |
| Whiteboard Data                                   |
+---------------------------------------------------+

                              |
                              |
                              v

                     Cloudinary Storage
                              |
                              |
                              v

                       PDF Documents
```

---

## 🛠️ Technology Stack

### Frontend

* React 19
* Vite
* Fabric.js
* STOMP.js
* SockJS
* Axios
* Tailwind CSS
* Lucide React

### Backend

* Java 17
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring WebSocket
* Spring AI

### Database

* PostgreSQL

### Authentication

* JWT Authentication
* Spring Security

### AI

* Spring AI
* OpenAI Compatible Models

### Cloud Storage

* Cloudinary

### PDF Processing

* Apache PDFBox

---

## 🗄️ Database Design

### User

```java
id
fullName
email
password
roles
```

### Room

```java
id
roomName
roomId
hostEmail
users
boardData
createdAt
isActive
```

### Document

```java
id
roomId
fileName
uploadedBy
uploadedAt
content
fileUrl
```

### ChatMessage

```java
id
roomId
sender
message
createdAt
```

---

## 🔄 Whiteboard Synchronization Flow

```text
User Draws
     ↓
Fabric.js Canvas
     ↓
Canvas Serialized to JSON
     ↓
STOMP WebSocket
     ↓
Spring Boot
     ↓
Broadcast to Room
     ↓
All Connected Clients Updated
```

---

## 📄 Document AI Workflow

```text
Upload PDF
     ↓
PDFBox Extracts Text
     ↓
Store Content in PostgreSQL
     ↓
Ask Question
     ↓
Build AI Prompt
     ↓
Spring AI
     ↓
Generate Response
```

---

## 🧠 Collaboration Summary Workflow

```text
Documents
      +
Chat Messages
      +
Whiteboard Data
      ↓
Summary Generator
      ↓
Spring AI
      ↓
Session Summary
```

---

## 🔐 Security Architecture

### HTTP Security

* JWT Authentication
* Request Filtering
* Protected Endpoints
* Role-Based Authorization

### WebSocket Security

* JWT Validation During Connection
* STOMP Authentication
* Permission Enforcement
* Viewer Restrictions

Example:

Viewer users cannot:

* Modify Whiteboard
* Send Chat Messages
* Broadcast Workspace Changes

---

## 🚀 Installation

### Backend

```bash
git clone <repository-url>

cd backend

mvn clean install

mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

### Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## 🎯 Use Cases

* Online Education
* Technical Interviews
* Team Brainstorming
* Remote Collaboration
* Project Planning
* Design Discussions
* Agile Meetings
* Student Learning Sessions
* Knowledge Sharing
* Document Analysis

---

## 📈 Future Enhancements

* Microservices Architecture
* Redis Caching
* Kafka Event Streaming
* Docker Deployment
* Kubernetes Orchestration
* AWS Cloud Deployment
* Voice Chat
* Video Conferencing
* Collaborative Notes
* AI Meeting Assistant
* Vector Database Search
* Full RAG Pipeline

---

## 👨‍💻 Author

**Yash Dabhekar**

Backend Developer | Java Developer | Spring Boot Developer

---

## Resume Description

Developed DrawSync, a full-stack collaborative workspace platform using React, Spring Boot, PostgreSQL, Fabric.js, STOMP WebSockets, Spring Security, JWT Authentication, Spring AI, and Cloudinary. Implemented real-time multi-user whiteboard synchronization, persistent canvas storage, role-based access control, live chat, PDF document management, AI-powered document question answering, collaboration summary generation, and session history tracking.
