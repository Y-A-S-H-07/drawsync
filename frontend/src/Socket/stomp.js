import { Client } from "@stomp/stompjs";

let stompClient = null;

export const connectSocket = (onConnected) => {
  const token = localStorage.getItem("token"); 

  if (stompClient && stompClient.connected) {
    console.log(" Reusing existing active STOMP connection.");
    if (onConnected) onConnected();
    return;
  }

  console.log(" Creating fresh STOMP connection instance...");
  stompClient = new Client({
    brokerURL: "https://drawsync-us5j.onrender.com/ws",
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: token ? `Bearer ${token}` : "",
    },
    debug: (str) => console.log("STOMP Debug:", str), 
  });

  stompClient.onConnect = () => {
    console.log(" Successfully connected via STOMP");
    if (onConnected) onConnected();
  };

  stompClient.onWebSocketClose = () => {
    console.warn(" WebSocket connection closed gracefully.");
  };

  stompClient.onStompError = (frame) => {
    console.error(" STOMP Broker Error: " + frame.headers["message"]);
  };

  stompClient.activate();
};

export const getStompClient = () => stompClient;