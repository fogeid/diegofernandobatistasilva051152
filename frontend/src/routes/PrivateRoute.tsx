import { Navigate, Outlet } from 'react-router-dom';
import { authStore } from '../stores/authStore';
import { MainLayout } from '../components/layout/MainLayout';

export function PrivateRoute() {
    const isAuthenticated = authStore((state) => state.isAuthenticated);

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return (
        <MainLayout>
            <Outlet />
        </MainLayout>
    );
}