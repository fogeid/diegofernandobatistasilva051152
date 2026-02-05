import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { PrivateRoute } from './PrivateRoute';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const Login = lazy(() => import('../pages/Login'));
const ArtistsList = lazy(() => import('../pages/Artists/ArtistsList'));
const ArtistDetail = lazy(() => import('../pages/Artists/ArtistDetail'));
const ArtistForm = lazy(() => import('../pages/Artists/ArtistForm'));
const AlbumForm = lazy(() => import('../pages/Albums/AlbumForm'));
const NotFound = lazy(() => import('../pages/NotFound'));

export function AppRoutes() {
    console.log('AppRoutes carregou - versão 1');
    return (
        <Suspense fallback={<LoadingSpinner />}>
            <Routes>
                <Route path="/login" element={<Login />} />

                <Route element={<PrivateRoute />}>
                    <Route path="/" element={<Navigate to="/artists" replace />} />
                    <Route path="/artists" element={<ArtistsList />} />
                    <Route path="/artists/:id" element={<ArtistDetail />} />
                    <Route path="/artists/new" element={<ArtistForm />} />
                    <Route path="/artists/:id/edit" element={<ArtistForm />} />

                    <Route path="/albums/new" element={<AlbumForm />} />
                    <Route path="/albums/:id/edit" element={<AlbumForm />} />
                    <Route path="/albums/*" element={<div style={{ padding: 20, color: 'white' }}>BATEU /albums/*</div>} />
                </Route>

                <Route path="*" element={<NotFound />} />
            </Routes>
        </Suspense>
    );
}
