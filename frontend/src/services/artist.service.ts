import api from '../lib/api';
import type {
    Artist,
    ArtistRequest,
    ArtistResponse,
    Page,
    PageRequest,
    SearchParams,
} from '../types/api.types';

const BASE_PATH = '/artists';

export const artistService = {
    async getAll(): Promise<ArtistResponse[]> {
        const response = await api.get<ArtistResponse[]>(BASE_PATH);
        return response.data;
    },

    async getById(id: number): Promise<ArtistResponse> {
        const response = await api.get<ArtistResponse>(`${BASE_PATH}/${id}`);
        return response.data;
    },

    async searchByName(params: SearchParams): Promise<ArtistResponse[]> {
        const response = await api.get<ArtistResponse[]>(`${BASE_PATH}/search`, {
            params: {
                name: params.query,
            },
        });
        return response.data;
    },

    async getBands(): Promise<ArtistResponse[]> {
        const response = await api.get<ArtistResponse[]>(`${BASE_PATH}/bands`);
        return response.data;
    },

    async getSoloArtists(): Promise<ArtistResponse[]> {
        const response = await api.get<ArtistResponse[]>(`${BASE_PATH}/solo`);
        return response.data;
    },

    async create(data: ArtistRequest): Promise<ArtistResponse> {
        const response = await api.post<ArtistResponse>(BASE_PATH, data);
        return response.data;
    },

    async update(id: number, data: ArtistRequest): Promise<ArtistResponse> {
        const response = await api.put<ArtistResponse>(`${BASE_PATH}/${id}`, data);
        return response.data;
    },

    async delete(id: number): Promise<void> {
        await api.delete(`${BASE_PATH}/${id}`);
    },
};