import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import toast from 'react-hot-toast';

export interface NotificationMessage {
    type: string;
    message: string;
    timestamp: string;
    data?: Record<string, any>;
}

class WebSocketService {
    private client: Client | null = null;
    private connected = false;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    private reconnectDelay = 3000;

    constructor() {
        this.client = null;
    }

    connect() {
        if (this.connected) {
            console.log('WebSocket já está conectado');
            return;
        }

        const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

        this.client = new Client({
            webSocketFactory: () => new SockJS(wsUrl) as any,

            connectHeaders: {},

            debug: (str) => {
                if (import.meta.env.DEV) {
                    console.log('STOMP:', str);
                }
            },

            reconnectDelay: this.reconnectDelay,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,

            onConnect: () => {
                console.log('✅ WebSocket conectado');
                this.connected = true;
                this.reconnectAttempts = 0;
                this.subscribeToTopics();
            },

            onDisconnect: () => {
                console.log('❌ WebSocket desconectado');
                this.connected = false;
            },

            onStompError: (frame) => {
                console.error('❌ Erro STOMP:', frame);
                this.handleReconnect();
            },
        });

        this.client.activate();
    }

    private handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Tentando reconectar... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('❌ Número máximo de tentativas de reconexão atingido');
            toast.error('Conexão com servidor perdida. Recarregue a página.');
        }
    }

    private subscribeToTopics() {
        if (!this.client) return;

        this.client.subscribe('/topic/albums', (message: IMessage) => {
            this.handleNotification(message);
        });

        this.client.subscribe('/topic/artists', (message: IMessage) => {
            this.handleNotification(message);
        });

        this.client.subscribe('/topic/covers', (message: IMessage) => {
            this.handleNotification(message);
        });

        this.client.subscribe('/topic/regionais', (message: IMessage) => {
            this.handleNotification(message);
        });

        console.log('✅ Inscrito em todos os tópicos');
    }

    private handleNotification(message: IMessage) {
        try {
            const notification: NotificationMessage = JSON.parse(message.body);

            console.log('📬 Notificação recebida:', notification);

            this.showToast(notification);

            const event = new CustomEvent('websocket-notification', {
                detail: notification,
            });
            window.dispatchEvent(event);
        } catch (error) {
            console.error('Erro ao processar notificação:', error);
        }
    }

    private showToast(notification: NotificationMessage) {
        const { type, message } = notification;

        switch (type) {
            case 'ALBUM_CREATED':
                toast.success(`🎵 ${message}`, { duration: 5000 });
                break;

            case 'ALBUM_UPDATED':
                toast.success(`✏️ ${message}`, { duration: 4000 });
                break;

            case 'ALBUM_DELETED':
                toast.error(`🗑️ ${message}`, { duration: 4000 });
                break;

            case 'ARTIST_CREATED':
                toast.success(`🎤 ${message}`, { duration: 5000 });
                break;

            case 'COVER_UPLOADED':
                toast.success(`🖼️ ${message}`, { duration: 4000 });
                break;

            case 'REGIONAIS_SYNCED':
                toast.success(`🗺️ ${message}`, { duration: 4000 });
                break;

            default:
                toast(`📬 ${message}`, { duration: 4000 });
        }
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.connected = false;
            console.log('WebSocket desconectado manualmente');
        }
    }

    isConnected(): boolean {
        return this.connected;
    }
}

export const webSocketService = new WebSocketService();