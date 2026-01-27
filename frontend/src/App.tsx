import { BrowserRouter } from 'react-router-dom';
import { useEffect } from 'react';
import { AppRoutes } from './routes/AppRoutes';
import { webSocketService } from './services/websocket.service';
import { authStore } from './stores/authStore';

function App() {
    const isAuthenticated = authStore((state) => state.isAuthenticated);

    useEffect(() => {
        if (isAuthenticated) {
            webSocketService.connect();
        }

        return () => {
            webSocketService.disconnect();
        };
    }, [isAuthenticated]);

    return (
        <BrowserRouter>
            <AppRoutes />
        </BrowserRouter>
    );
}

export default App;