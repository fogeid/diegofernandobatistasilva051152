import { useEffect, useState } from 'react';
import { webSocketService } from '../services/websocket.service';
import type { NotificationMessage } from '../services/websocket.service';

export function useWebSocket() {
    const [isConnected, setIsConnected] = useState(false);
    const [lastMessage, setLastMessage] = useState<NotificationMessage | null>(null);

    useEffect(() => {
        webSocketService.connect();
        setIsConnected(webSocketService.isConnected());

        const handleNotification = (event: Event) => {
            const customEvent = event as CustomEvent<NotificationMessage>;
            setLastMessage(customEvent.detail);
        };

        window.addEventListener('websocket-notification', handleNotification);

        return () => {
            window.removeEventListener('websocket-notification', handleNotification);
        };
    }, []);

    return {
        isConnected,
        lastMessage,
    };
}