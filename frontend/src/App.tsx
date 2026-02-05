import { BrowserRouter } from 'react-router-dom';
import React, { lazy, Suspense, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoadingPage } from './components/common/LoadingSpinner';
import { webSocketService } from './services/websocket.service';
import { authStore } from './stores/authStore';
import { MainLayout } from './components/layout/MainLayout';

const Login = lazy(() => import('./pages/Login'));
const ArtistsList = lazy(() => import('./pages/Artists/ArtistsList'));
const ArtistDetail = lazy(() => import('./pages/Artists/ArtistDetail'));
const ArtistForm = lazy(() => import('./pages/Artists/ArtistForm'));
const AlbumForm = lazy(() => import('./pages/Albums/AlbumForm'));
const NotFound = lazy(() => import('./pages/NotFound'));

function PrivateRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = authStore((state) => state.isAuthenticated);

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <MainLayout>{children}</MainLayout>;
}

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
            <Suspense fallback={<LoadingPage />}>
                <Routes>
                    {/* Rota pública */}
                    <Route path="/login" element={<Login />} />

                    {/* Rotas privadas */}
                    <Route
                        path="/"
                        element={
                            <PrivateRoute>
                                <Navigate to="/artists" replace />
                            </PrivateRoute>
                        }
                    />

                    {/* Artistas */}
                    <Route
                        path="/artists"
                        element={
                            <PrivateRoute>
                                <ArtistsList />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/artists/new"
                        element={
                            <PrivateRoute>
                                <ArtistForm />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/artists/:id"
                        element={
                            <PrivateRoute>
                                <ArtistDetail />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/artists/:id/edit"
                        element={
                            <PrivateRoute>
                                <ArtistForm />
                            </PrivateRoute>
                        }
                    />

                    {/* Álbuns */}
                    <Route
                        path="/albums/new"
                        element={
                            <PrivateRoute>
                                <AlbumForm />
                            </PrivateRoute>
                        }
                    />
                    <Route
                        path="/albums/:id/edit"
                        element={
                            <PrivateRoute>
                                <AlbumForm />
                            </PrivateRoute>
                        }
                    />

                    {/* 404 - Not Found */}
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </Suspense>
        </BrowserRouter>
    );
}

export default App;
