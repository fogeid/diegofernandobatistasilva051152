import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';

export interface User {
    username: string;
    exp: number;
}

interface AuthState {
    token: string | null;
    refreshToken: string | null;
    user: User | null;
    isAuthenticated: boolean;

    login: (token: string, refreshToken: string) => void;
    logout: () => void;
    setToken: (token: string) => void;
    isTokenExpired: () => boolean;
}

export const authStore = create<AuthState>()(
    persist(
        (set, get) => ({
            token: null,
            refreshToken: null,
            user: null,
            isAuthenticated: false,

            login: (token: string, refreshToken: string) => {
                try {
                    const decoded = jwtDecode<User>(token);

                    set({
                        token,
                        refreshToken,
                        user: decoded,
                        isAuthenticated: true,
                    });
                } catch (error) {
                    console.error('Erro ao decodificar token:', error);
                    get().logout();
                }
            },

            logout: () => {
                set({
                    token: null,
                    refreshToken: null,
                    user: null,
                    isAuthenticated: false,
                });
            },

            setToken: (token: string) => {
                try {
                    const decoded = jwtDecode<User>(token);

                    set({
                        token,
                        user: decoded,
                        isAuthenticated: true,
                    });
                } catch (error) {
                    console.error('Erro ao decodificar token:', error);
                    get().logout();
                }
            },

            isTokenExpired: () => {
                const { user } = get();

                if (!user || !user.exp) {
                    return true;
                }

                const expirationTime = user.exp * 1000;
                const currentTime = Date.now();
                const timeUntilExpiration = expirationTime - currentTime;

                return timeUntilExpiration < 60000;
            },
        }),
        {
            name: 'seplag-auth-storage',
            partialize: (state) => ({
                token: state.token,
                refreshToken: state.refreshToken,
                user: state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);