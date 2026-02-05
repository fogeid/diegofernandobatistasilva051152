import { useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';

import { artistService } from '../../services/artist.service';
import { albumService } from '../../services/album.service';

import { Button } from '../../components/ui/Button';
import { LoadingPage } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { ArtistCard as ArtistCardItem } from '../../components/artists/ArtistCard';
import { getErrorMessage } from '../../lib/api';

export default function ArtistsList() {
    const navigate = useNavigate();

    const {
        data: artists,
        isLoading: loadingArtists,
        error,
    } = useQuery({
        queryKey: ['artists'],
        queryFn: artistService.getAll,
        refetchOnMount: 'always',
        refetchOnWindowFocus: true,
        staleTime: 0,
    });

    const { data: albums = [] } = useQuery({
        queryKey: ['albums'],
        queryFn: albumService.getAll,
        refetchOnMount: 'always',
        refetchOnWindowFocus: true,
        staleTime: 0,
    });

    const artistsWithAlbumCount = useMemo(() => {
        if (!artists) return [];
        return artists.map((artist) => {
            const albumCount = albums.filter((album) =>
                album.artists.some((a) => a.id === artist.id)
            ).length;

            return { ...artist, albumCount };
        });
    }, [artists, albums]);

    useEffect(() => {
        if (error) toast.error(getErrorMessage(error));
    }, [error]);

    if (loadingArtists) {
        return <LoadingPage />;
    }

    return (
        <div className="space-y-6 pt-6">
            <div className="flex items-end justify-between">
                <div>
                    <h1 className="mb-2 text-4xl font-bold text-white">Seus Artistas</h1>
                    <p className="text-sm text-[#b3b3b3]">
                        {artistsWithAlbumCount.length}{' '}
                        {artistsWithAlbumCount.length === 1 ? 'artista' : 'artistas'}
                    </p>
                </div>

                <Button
                    onClick={() => navigate('/artists/new')}
                    className="bg-[#1DB954] text-white shadow-lg hover:scale-105 hover:bg-[#1ed760]"
                >
                    <Plus className="mr-2 h-5 w-5" />
                    Criar Artista
                </Button>
            </div>

            {artistsWithAlbumCount.length === 0 ? (
                <EmptyState
                    title="Nenhum artista encontrado"
                    description="Comece cadastrando seu primeiro artista"
                    actionLabel="Criar Artista"
                    onAction={() => navigate('/artists/new')}
                />
            ) : (
                <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6">
                    {artistsWithAlbumCount.map((artist, index) => (
                        <ArtistCardItem
                            key={artist.id}
                            artist={artist}
                            albumsCount={artist.albumCount ?? 0}
                            onOpen={(id) => navigate(`/artists/${id}`)}
                            animationDelayMs={index * 50}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
