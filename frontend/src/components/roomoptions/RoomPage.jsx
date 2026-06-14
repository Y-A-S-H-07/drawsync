import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Whiteboard from "../WhiteBoardLibrary/WhiteBoard";
import api from "../../API/axios";
import { connectSocket, getStompClient } from "../../Socket/stomp";

function RoomPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("members");
  const [micOn, setMicOn] = useState(true);
  const [members, setMembers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [initialBoard, setInitialBoard] = useState(null);
  const hasJoined = useRef(false);
  const [hostName, setHostName] = useState("");
  const [summary, setSummary] = useState('');
  const [permittedMember, setPermittedMember] = useState([]);

  useEffect(() => {
    if (!roomId || hasJoined.current) return;

    hasJoined.current = true;

    connectSocket(() => {
      const client = getStompClient();

      const user = JSON.parse(localStorage.getItem("userDetails"));

      console.log("Joining room:", roomId);

      
      client.publish({
        destination: "/app/join",
        body: JSON.stringify({
          roomId,
          userId: user?.id,
          userName: user?.fullName || user?.name
        }),
      });
      

      client.subscribe("/topic/room/" + roomId, (msg) => {

        const data = JSON.parse(msg.body);
        console.log("JOIN DATA =", JSON.stringify(data, null, 2));

        if (data.type === "ERROR") {
          alert("This room does not exist! Redirecting you back home...");
          getStompClient().disconnect();
          hasJoined.current = false;
          navigate("/"); // Send them back to the main lobby page
          return;
        }

       if (data.type === "JOINED") {
          setMembers(data.users || []);
          setHostName(data.host);
          setInitialBoard(data.boardData);

          const loggedInUser = JSON.parse(localStorage.getItem("userDetails"));

          setCurrentUser({
            userId: loggedInUser.email,
            name: loggedInUser.fullName || loggedInUser.name
          });
        }
        
        

        if (data.type === "USER_JOINED") {
          setMembers((prev) => [...prev, data.user]);
        }

        if (data.type === "USER_LEFT") {
          setMembers((prev) =>
            prev.filter((u) => u.userId !== data.user.userId)
          );
        }
      });

      client.subscribe("/topic/permission/" + roomId, (msg) => {
        const data = JSON.parse(msg.body);
        console.log("Received Permission Update:", data);
        setPermittedMember(data.permitted || []);
      });
    });

  }, [roomId]);



  const handleLeave = () => {
    getStompClient().disconnect();
    hasJoined.current = false;
    navigate("/");
  };


  const handlePermission = (id) => {
    const updated = permittedMember.includes(id)
      ? permittedMember.filter(x => x !== id)
      : [...permittedMember, id];

    // Update host UI immediately
    setPermittedMember(updated);

    // Broadcast to room
    getStompClient().publish({
      destination: "/app/permission",
      body: JSON.stringify({
        roomId,
        permitted: updated
      }),
    });
  }


  return (
    <div style={styles.page}>
      {/* LEFT – WHITEBOARD */}
      <div style={styles.boardArea}>
        <div style={styles.roomHeader}>
          <div style={styles.roomIdBox}>
            <span style={styles.roomText}>Room ID: {roomId}</span>
            <button
              style={styles.copyIcon}
              onClick={() => {
                navigator.clipboard.writeText(roomId);
                toast.success("Copied to clipboard");
              }}
            >
              📋
            </button>
            <button
              style={styles.copyIcon}
              onClick={() => {
                navigator.clipboard.writeText(`${window.location.origin}/joinRoom?rid=${roomId}`);
                toast.success("Link to clipboard");
              }}
            >
              🔗
            </button>
          </div>

          <div style={styles.headerActions}>
            <p>Host Name: {hostName}</p>
            <button
              style={micOn ? styles.voiceBtn : styles.voiceBtnOff}
              onClick={() => setMicOn(!micOn)}
            >
              <MicIcon isOn={micOn} />
            </button>

            <button style={styles.leaveBtn} onClick={handleLeave}>
              Leave
            </button>
          </div>
        </div>
        {
          currentUser &&
          <Whiteboard roomId={roomId} initialBoard={initialBoard} permittedMember={permittedMember} currentUser={currentUser} hostName={hostName} />
        }
      </div>

      {/* RIGHT PANEL */}
      <div style={styles.rightPanel}>
        <div style={styles.tabs}>
          <button
            style={activeTab === "members" ? styles.activeTab : styles.tab}
            onClick={() => setActiveTab("members")}
          >
            Members ({members.length})
          </button>
          <button
            style={activeTab === "chat" ? styles.activeTab : styles.tab}
            onClick={() => setActiveTab("chat")}
          >
            ChatBox
          </button>


          <button
            style={activeTab === "documents" ? styles.activeTab : styles.tab}
            onClick={() => setActiveTab("documents")}
          >
            Documents
          </button>


          <button
            style={activeTab === "image" ? styles.activeTab : styles.tab}
            onClick={() => setActiveTab("image")}
          >
            Summary Generation
          </button>
        </div>

        <div style={styles.tabContent}>
          {activeTab === "members" && <Members members={members} hostName={hostName} handlePermission={handlePermission} currentUser={currentUser} permittedMember={permittedMember} />}
          {activeTab === "chat" && <ChatBox roomId={roomId} currentUser={currentUser} />}
          {activeTab === "image" && <SummaryGeneration roomId={roomId} setSummary={setSummary} summary={summary} />}
          {activeTab === "documents" && (
            <Documents roomId={roomId} />
          )}
        </div>
      </div>
    </div>
  );
}

export default RoomPage;

//////////////// COMPONENTS //////////////////

function MicIcon({ isOn }) {
  return (
    <span style={{ position: "relative", display: "inline-block" }}>
      <span style={{ fontSize: "16px" }}>🎙️</span>
      {!isOn && <span style={styles.micSlash}>/</span>}
    </span>
  );
}

function Members({ members, hostName, handlePermission, currentUser, permittedMember }) {
  if (!members || members.length === 0) {
    return (
      <p style={{ opacity: 0.6, fontSize: "13px", padding: "10px" }}>
        No members in room yet...
      </p>
    );
  }

  return (
    <div>
      {members.map((m) => (
        <div key={m.userId} style={memberStyles.row}>
          <div style={memberStyles.userInfo}>
            <div style={memberStyles.avatar} className={`${m.userId === currentUser?.userId ? 'border-2 border-emerald-500' : ''}`}>
              {m.name ? m.name.charAt(0).toUpperCase() : "?"}
            </div>
            <span style={memberStyles.name}>
              {m.name || "Anonymous"} {m.userId === currentUser?.userId ? " (You)" : ""}
            </span>
          </div>

          <div style={memberStyles.actions}>
            <button style={styles.voiceBtn} title="Mic">
              🎙️
            </button>

            {
              (currentUser?.userId === hostName && m.userId !== hostName) &&
              <button style={styles.drawBtn} title="Draw permission" onClick={() => { handlePermission(m.userId) }}>
                {
                  permittedMember.includes(m.userId) ? '❌' : '✏️'
                }
              </button>
            }
          </div>
        </div>
      ))}
    </div>
  );
}

function ChatBox({ roomId, currentUser }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const messagesEndRef = useRef(null);

  useEffect(() => {
    const client = getStompClient();
    if (!client) return;

    console.log("Setting up chat subscription for room:", roomId);
    
    const subscription = client.subscribe("/topic/chat/" + roomId, (msg) => {
      const message = JSON.parse(msg.body);

      console.log("Received Chat:", message);

      setMessages((prev) => [...prev, message]);
    });

    return () => {
      if (subscription) {
        console.log("Cleaning up stale chat listener...");
        subscription.unsubscribe();
      }
    };
  }, [roomId]);

  const sendMessage = () => {
    if (!input.trim() || !currentUser) return;

    const message = {
      text: input,
      userName: currentUser.name,
      userId: currentUser.userId,
      createdAt: new Date().toISOString()
    };
    console.log("Current User:", currentUser);
    console.log("Message:", message);

    getStompClient().publish({
      destination: "/app/chat",
      body: JSON.stringify({
        roomId,
        message
      }),
    });

    setInput("");
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <div style={chatStyles.wrapper}>
      <div style={chatStyles.messages}>
        {messages.length === 0 ? (
          <p style={{ opacity: 0.5, fontSize: "13px", textAlign: "center", marginTop: "20px" }}>
            No messages yet. Start the conversation!
          </p>
        ) : (
          messages.map((m, idx) => (
            <div key={idx} style={chatStyles.messageCard}>
              <div style={chatStyles.userName}>{m.userName}</div>
              <div style={chatStyles.messageText}>{m.text}</div>
              <div style={chatStyles.time}>
                {m.createdAt ? new Date(m.createdAt).toLocaleTimeString([], {
                  hour: "2-digit",
                  minute: "2-digit",
                }) : new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <div style={chatStyles.inputBox}>
        <input
          style={chatStyles.input}
          value={input}
          placeholder="Type a message..."
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && sendMessage()}
        />
        <button style={chatStyles.sendBtn} onClick={sendMessage}>
          ➤
        </button>
      </div>
    </div>
  );
}
function Documents({ roomId }) {
  const [documents, setDocuments] = useState([]);
  const [file, setFile] = useState(null);

  useEffect(() => {
    fetchDocuments();
  }, []);

  const fetchDocuments = async () => {
    try {
      const { data } = await api.get(`/documents/${roomId}`);
      setDocuments(data);
    } catch (err) {
      console.error(err);
    }
  };

  const uploadFile = async () => {

  console.log("UPLOAD CLICKED");
  console.log("FILE =", file);

  if (!file) {
    alert("No file selected");
    return;
  }

  const formData = new FormData();
    formData.append("file", file);

    try {
      console.log("Sending request...");

      await api.post(
        `/documents/upload/${roomId}`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      alert("PDF Uploaded");

      setFile(null);
      fetchDocuments();

    } catch (err) {
      console.error(err);
      alert("Upload failed");
    }
  };
  return (
    <div style={{ padding: "10px" }}>
      <input
        type="file"
        accept=".pdf"
        onChange={(e) => setFile(e.target.files[0])}
      />

      <button
        onClick={uploadFile}
        style={{
          marginTop: "10px",
          padding: "8px 12px",
          background: "#2563eb",
          border: "none",
          borderRadius: "8px",
          color: "white",
          cursor: "pointer",
        }}
      >
        Upload PDF
      </button>

      <div style={{ marginTop: "20px" }}>
        {documents.map((doc) => (
          <div
            key={doc.id}
            style={{
              border: "1px solid #333",
              padding: "10px",
              marginBottom: "10px",
              borderRadius: "8px",
            }}
          >
            <div>{doc.fileName}</div>

            <div
              style={{
                fontSize: "12px",
                opacity: 0.7,
              }}
            >
              {doc.uploadedBy}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}


function SummaryGeneration({ roomId, setSummary, summary }) {

  const [loading, setLoading] = useState(false);

  const handleOnClick = async () => {
    setLoading(true);
    try {
      const { data } = await api.post(`/summary/${roomId}`);
      setSummary(data.output);
    } catch (err) {
      toast.error(err.message?.data?.msg || "something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ padding: "20px", textAlign: "center", opacity: 0.9 }}>

      <style>{`
        .summary-output pre {
          white-space: pre-wrap;
          background: #0d0d0d;
          color: #e8e8e8;
          padding: 16px;
          border-radius: 8px;
          font-family: "Fira Code", "Consolas", "Menlo", monospace;
          font-size: 14px;
          line-height: 1.55;
          text-align: left;
          border: 1px solid #333;
          box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
          overflow-x: auto;
        }
      `}</style>

      <div className="flex justify-around items-center">
        <button
          className="text-white border border-white hover:bg-white hover:text-black transition-all shadow-md shadow-white rounded"
          style={{ padding: "10px 20px" }}
          onClick={handleOnClick}
          disabled={loading}
        >
          {loading ? "Generating..." : "Generate Summary!"}
        </button>
        {
          summary &&
          <button onClick={() => navigator.clipboard.writeText(summary)} className="text-2xl rounded hover:bg-white relative after:content-['Copy Summary'] after:absolute after:bottom-50 after:w-50 after:h-50 after:bg-amber-300 " style={{ padding: "5px 10px" }}>
            📋
          </button>
        }

      </div>
      {
        summary && (
          <div className="summary-output" style={{ marginTop: "20px" }}>
            <div dangerouslySetInnerHTML={{ __html: summary }} />
          </div>
        )
      }

    </div>
  );
}

//////////////// STYLES //////////////////

const styles = {
  page: {
    minHeight: "100vh",
    background: "#020817",
    color: "#f8fafc",
    display: "flex",
  },

  boardArea: {
    flex: 1,
    margin: "10px",
    border: "1.5px solid rgba(255,255,255,0.25)",
    borderRadius: "14px",
    display: "flex",
    flexDirection: "column",
    overflow: "hidden",
  },

  roomHeader: {
    display: "flex",
    justifyContent: "space-between",
    padding: "10px 14px",
    borderBottom: "1px solid rgba(255,255,255,0.2)",
    flexShrink: 0,
  },

  roomIdBox: { display: "flex", alignItems: "center", gap: "6px" },
  roomText: { fontWeight: "600", fontSize: "14px" },

  copyIcon: {
    border: "1px solid rgba(255,255,255,0.4)",
    background: "rgba(255,255,255,0.05)",
    borderRadius: "6px",
    padding: "3px 6px",
    cursor: "pointer",
    transition: "all 0.2s",
  },

  headerActions: { display: "flex", gap: "8px" },

  voiceBtn: {
    border: "1px solid rgba(255,255,255,0.4)",
    background: "rgba(255,255,255,0.1)",
    borderRadius: "6px",
    padding: "6px 12px",
    cursor: "pointer",
    transition: "all 0.2s",
  },

  voiceBtnOff: {
    border: "1px solid rgba(255,0,0,0.6)",
    background: "rgba(255,0,0,0.15)",
    borderRadius: "6px",
    padding: "6px 12px",
    cursor: "pointer",
    transition: "all 0.2s",
  },

  drawBtn: {
    border: "1px solid rgba(255,255,255,0.4)",
    background: "rgba(255,255,255,0.1)",
    borderRadius: "6px",
    padding: "6px 10px",
    cursor: "pointer",
    fontSize: "14px",
    transition: "all 0.2s",
  },

  micSlash: {
    position: "absolute",
    top: "-2px",
    left: "6px",
    color: "red",
    fontWeight: "800",
    fontSize: "18px",
  },

  leaveBtn: {
    background: "linear-gradient(135deg,#ef4444,#dc2626)",
    border: "none",
    borderRadius: "6px",
    padding: "6px 12px",
    color: "white",
    cursor: "pointer",
    fontWeight: "600",
    transition: "all 0.2s",
    fontSize: "14px",
  },

  rightPanel: {
    width: "340px",
    margin: "10px",
    border: "1.5px solid rgba(255,255,255,0.25)",
    borderRadius: "14px",
    display: "flex",
    flexDirection: "column",
    height: "calc(100vh - 20px)",
    overflow: "hidden",
  },

  tabs: {
    display: "flex",
    gap: "6px",
    padding: "10px",
    flexShrink: 0,
  },

  tab: {
    flex: 1,
    padding: "8px",
    background: "#020817",
    borderRadius: "8px",
    border: "1px solid #0c0d0dff",
    cursor: "pointer",
    fontSize: "12px",
    transition: "all 0.2s",
  },

  activeTab: {
    flex: 1,
    padding: "8px",
    background: "#ebeff1ff",
    color: "#020817",
    borderRadius: "8px",
    fontWeight: "600",
    fontSize: "12px",
    border: "none",
    cursor: "pointer",
  },

  tabContent: {
    flex: 1,
    padding: "12px",
    overflow: "auto",
  },
};

const chatStyles = {
  wrapper: {
    display: "flex",
    flexDirection: "column",
    height: "100%",
    maxHeight: "100%",
  },

  messages: {
    flex: 1,
    overflowY: "auto",
    display: "flex",
    flexDirection: "column",
    gap: "10px",
    paddingRight: "5px",
    marginBottom: "10px",
    minHeight: 0,
  },

  messageCard: {
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.15)",
    borderRadius: "10px",
    padding: "8px 10px 18px 10px",
    position: "relative",
    flexShrink: 0,
  },

  userName: {
    fontWeight: "600",
    fontSize: "13px",
    marginBottom: "4px",
    color: "#60a5fa",
  },

  messageText: {
    marginBottom: "6px",
    fontSize: "14px",
    lineHeight: "1.4",
    wordWrap: "break-word",
  },

  time: {
    fontSize: "11px",
    color: "#9ca3af",
    position: "absolute",
    bottom: "4px",
    right: "8px",
  },

  inputBox: {
    display: "flex",
    gap: "6px",
    borderTop: "1px solid rgba(255,255,255,0.2)",
    paddingTop: "10px",
    flexShrink: 0,
  },

  input: {
    flex: 1,
    padding: "8px",
    borderRadius: "8px",
    border: "1px solid rgba(255,255,255,0.3)",
    background: "rgba(255,255,255,0.05)",
    color: "white",
    fontSize: "14px",
    outline: "none",
  },

  sendBtn: {
    padding: "8px 12px",
    background: "#2563eb",
    color: "white",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    fontSize: "16px",
    transition: "all 0.2s",
  },
};

const memberStyles = {
  row: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "12px 8px",
    borderBottom: "1px solid rgba(255,255,255,0.1)",
    transition: "background 0.2s",
  },

  userInfo: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
  },

  avatar: {
    width: "32px",
    height: "32px",
    borderRadius: "50%",
    background: "linear-gradient(135deg, #3b82f6, #8b5cf6)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700",
    fontSize: "14px",
    color: "white",
  },

  name: {
    fontSize: "14px",
    fontWeight: "500",
  },

  actions: {
    display: "flex",
    gap: "8px",
  },
};