import api from '../lib/api';
import type {
    AlbumRequest,
    AlbumResponse,
    AlbumCoverUploadResponse,
    Page,
    PageRequest,
    SearchParams,
} from '../types/api.types';

const BASE_PATH = '/albums';

export const albumService = {
    async getAll(): Promise<AlbumResponse[]> {
        const response = await api.get<AlbumResponse[]>(BASE_PATH);
        return response.data;
    },

    async getById(id: number): Promise<AlbumResponse> {
        const response = await api.get<AlbumResponse>(`${BASE_PATH}/${id}`);
        return response.data;
    },

    async searchByTitle(params: SearchParams): Promise<AlbumResponse[]> {
        const response = await api.get<AlbumResponse[]>(`${BASE_PATH}/search`, {
            params: {
                title: params.query,
            },
        });
        return response.data;
    },

    async getByYear(year: number): Promise<AlbumResponse[]> {
        const response = await api.get<AlbumResponse[]>(`${BASE_PATH}/year/${year}`);
        return response.data;
    },

    async getBandAlbums(pageRequest: PageRequest): Promise<Page<AlbumResponse>> {
        const response = await api.get<Page<AlbumResponse>>(`${BASE_PATH}/bands`, {
            params: {
                page: pageRequest.page,
                size: pageRequest.size,
            },
        });
        return response.data;
    },

    async getSoloAlbums(pageRequest: PageRequest): Promise<Page<AlbumResponse>> {
        const response = await api.get<Page<AlbumResponse>>(`${BASE_PATH}/solo`, {
            params: {
                page: pageRequest.page,
                size: pageRequest.size,
            },
        });
        return response.data;
    },

    async create(data: AlbumRequest): Promise<AlbumResponse> {
        const response = await api.post<AlbumResponse>(BASE_PATH, data);
        return response.data;
    },

    async update(id: number, data: AlbumRequest): Promise<AlbumResponse> {
        const response = await api.put<AlbumResponse>(`${BASE_PATH}/${id}`, data);
        return response.data;
    },

    async delete(id: number): Promise<void> {
        await api.delete(`${BASE_PATH}/${id}`);
    },

    async uploadCover(albumId: number, file: File): Promise<AlbumCoverUploadResponse> {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post<AlbumCoverUploadResponse>(
            `${BASE_PATH}/${albumId}/covers`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            }
        );
        return response.data;
    },

    async getCovers(albumId: number): Promise<AlbumCoverUploadResponse[]> {
        const response = await api.get<AlbumCoverUploadResponse[]>(`${BASE_PATH}/${albumId}/covers`);
        return response.data;
    },

    async deleteCover(albumId: number, coverId: number): Promise<void> {
        await api.delete(`${BASE_PATH}/${albumId}/covers/${coverId}`);
    },
};