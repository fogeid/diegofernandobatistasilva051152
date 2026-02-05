import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';

export interface User {
    username: string;
    exp: number;
}

type JwtPayload = {
    exp?: number;
    username?: string;
    preferred_username?: string;
    user_name?: string;
    name?: string;
    sub?: string;
    email?: string;
    [key: string]: any;
};

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

function mapJwtToUser(payload: JwtPayload): User | null {
    const exp = payload?.exp;
    if (!exp) return null;

    const username =
        payload.preferred_username ||
        payload.username ||
        payload.user_name ||
        payload.email ||
        payload.name ||
        payload.sub;

    if (!username) {
        return { username: 'Usuário', exp };
    }

    return { username, exp };
}

export const authStore = create<AuthState>()(
    persist(
        (set, get) => ({
            token: null,
            refreshToken: null,
            user: null,
            isAuthenticated: false,

            login: (token: string, refreshToken: string) => {
                if (!token || typeof token !== 'string') {
                    console.error('Token inválido recebido:', token);
                    get().logout();
                    return;
                }

                try {
                    const decoded = jwtDecode<JwtPayload>(token);
                    const user = mapJwtToUser(decoded);

                    if (!user) {
                        console.error('JWT sem exp (inválido):', decoded);
                        get().logout();
                        return;
                    }

                    set({
                        token,
                        refreshToken,
                        user,
                        isAuthenticated: true,
                    });

                    console.log('Login bem-sucedido:', user.username);
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
                console.log('Logout realizado');
            },

            setToken: (token: string) => {
                if (!token || typeof token !== 'string') {
                    console.error('Token inválido:', token);
                    get().logout();
                    return;
                }

                try {
                    const decoded = jwtDecode<JwtPayload>(token);
                    const user = mapJwtToUser(decoded);

                    if (!user) {
                        console.error('JWT sem exp (inválido):', decoded);
                        get().logout();
                        return;
                    }

                    set({
                        token,
                        user,
                        isAuthenticated: true,
                    });
                } catch (error) {
                    console.error('Erro ao decodificar token:', error);
                    get().logout();
                }
            },

            isTokenExpired: () => {
                const { user } = get();

                if (!user?.exp) return true;

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
