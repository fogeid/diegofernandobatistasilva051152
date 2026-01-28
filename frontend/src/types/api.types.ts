export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    tokenType: string;
    accessToken: string;  // ← API retorna accessToken
    refreshToken: string;
    expiresIn: number;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}

export interface Artist {
    id: number;
    name: string;
    isBand: boolean;
    albumCount?: number;
    createdAt: string;
    updatedAt: string;
}

export interface ArtistRequest {
    name: string;
    isBand: boolean;
}

export interface ArtistResponse {
    id: number;
    name: string;
    isBand: boolean;
    albumCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface Album {
    id: number;
    title: string;
    releaseYear: number;
    artists: Artist[];
    covers: AlbumCover[];
    createdAt: string;
    updatedAt: string;
}

export interface AlbumRequest {
    title: string;
    releaseYear: number;
    artistIds: number[];
}

export interface AlbumResponse {
    id: number;
    title: string;
    releaseYear: number;
    artists: ArtistResponse[];
    covers: AlbumCover[];
    createdAt: string;
    updatedAt: string;
}

export interface AlbumCover {
    id: number;
    fileName: string;
    imageUrl: string;
    fileSize: number;
    contentType: string;
    createdAt: string;
}

export interface AlbumCoverUploadResponse {
    id: number;
    fileName: string;
    fileUrl: string;
    fileSize: number;
    contentType: string;
    uploadedAt: string;
}

export interface Regional {
    id: number;
    nome: string;
    ativo: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface RegionalRequest {
    id?: number;
    nome: string;
    ativo?: boolean;
}

export interface RegionalResponse {
    id: number;
    nome: string;
    ativo: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface SyncResult {
    message: string;
    novos: number;
    atualizados: number;
    inativados: number;
    total: number;
}

export interface Page<T> {
    content: T[];
    pageable: {
        pageNumber: number;
        pageSize: number;
    };
    totalElements: number;
    totalPages: number;
    last: boolean;
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}

export interface PageRequest {
    page: number;
    size: number;
    sort?: string;
}

export interface HealthResponse {
    status: 'UP' | 'DOWN';
    components?: {
        [key: string]: {
            status: 'UP' | 'DOWN';
            details?: any;
        };
    };
}

export interface SearchParams {
    query?: string;
    page?: number;
    size?: number;
    sort?: 'asc' | 'desc';
}