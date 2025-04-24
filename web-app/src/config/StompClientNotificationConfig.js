import TokenService from "@/service/TokenService.js";
import { Client } from "@stomp/stompjs";

export const initializeStompClient = () => {
    const token = TokenService.getSessionAccessToken();
    const stompClient = new Client({
      brokerURL: "http://localhost:8080/api/ws",
      // debug: (str) => {
      //   console.log(str);
      // },
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: (frame) => {
      },
      onWebSocketError: (error) => {
        console.error("Error with websocket", error);
      },
      onStompError: (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
      },
    });
    return stompClient
}