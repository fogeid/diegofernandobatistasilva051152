import api from '../lib/api';
import type { LoginRequest, LoginResponse, RefreshTokenRequest } from '../types/api.types';

const BASE_PATH = '/auth';

export const authService = {
    async login(credentials: LoginRequest): Promise<LoginResponse> {
        const response = await api.post<LoginResponse>(`${BASE_PATH}/login`, credentials);
        return response.data;
    },

    async refreshToken(data: RefreshTokenRequest): Promise<LoginResponse> {
        const response = await api.post<LoginResponse>(`${BASE_PATH}/refresh`, data);
        return response.data;
    },

    async healthCheck(): Promise<{ status: string }> {
        const response = await api.get('/actuator/health');
        return response.data;
    },
};