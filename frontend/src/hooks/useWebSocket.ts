import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
// Map the API base URL to the websocket endpoint (e.g. http://localhost:8080/ws)
const WS_URL = API_BASE_URL.replace('/api', '/ws');

export const useWebSocket = () => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('STOMP connected');
        setConnected(true);
      },
      onDisconnect: () => {
        console.log('STOMP disconnected');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const subscribe = (topic: string, callback: (message: any) => void) => {
    if (!clientRef.current || !connected) return null;
    
    return clientRef.current.subscribe(topic, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (e) {
        console.error('Failed to parse STOMP message', e);
      }
    });
  };

  return { connected, subscribe };
};
