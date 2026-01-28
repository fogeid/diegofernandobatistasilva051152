import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Plus, Music2, Users, Play } from 'lucide-react';
import toast from 'react-hot-toast';
import { artistService } from '../../services/artist.service';
import { albumService } from '../../services/album.service';
import type { ArtistResponse } from '../../types/api.types';
import { Button } from '../../components/ui/Button';
import { LoadingPage } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { getErrorMessage } from '../../lib/api';

export default function ArtistsList() {
    const navigate = useNavigate();

    const { data: artists, isLoading: loadingArtists, error } = useQuery({
        queryKey: ['artists'],
        queryFn: artistService.getAll,
    });

    const { data: albums = [] } = useQuery({
        queryKey: ['albums'],
        queryFn: albumService.getAll,
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

    if (error) {
        toast.error(getErrorMessage(error));
    }

    if (loadingArtists) {
        return <LoadingPage />;
    }

    return (
        <div className="pt-6 space-y-6">
            {/* Header */}
            <div className="flex items-end justify-between">
                <div>
                    <h1 className="text-4xl font-bold text-white mb-2">
                        Seus Artistas
                    </h1>
                    <p className="text-[#b3b3b3] text-sm">
                        {artistsWithAlbumCount.length} {artistsWithAlbumCount.length === 1 ? 'artista' : 'artistas'}
                    </p>
                </div>
                <Button
                    onClick={() => navigate('/artists/new')}
                    className="bg-[#1DB954] hover:bg-[#1ed760] text-white hover:scale-105 shadow-lg"
                >
                    <Plus className="mr-2 h-5 w-5" />
                    Criar Artista
                </Button>
            </div>

            {/* Artists Grid */}
            {artistsWithAlbumCount.length === 0 ? (
                <EmptyState
                    title="Nenhum artista encontrado"
                    description="Comece cadastrando seu primeiro artista"
                    actionLabel="Criar Artista"
                    onAction={() => navigate('/artists/new')}
                />
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6 gap-4">
                    {artistsWithAlbumCount.map((artist, index) => (
                        <ArtistCard
                            key={artist.id}
                            artist={artist}
                            index={index}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

interface ArtistCardProps {
    artist: ArtistResponse & { albumCount: number };
    index: number;
}

function ArtistCard({ artist, index }: ArtistCardProps) {
    const navigate = useNavigate();

    return (
        <div
            className="group relative p-4 bg-[#181818] rounded-lg cursor-pointer spotify-card animate-fade-in"
            onClick={() => navigate(`/artists/${artist.id}`)}
            style={{ animationDelay: `${index * 50}ms` }}
        >
            {/* Artist Image/Icon */}
            <div className="relative mb-4 aspect-square">
                <div className="w-full h-full bg-gradient-to-br from-[#1DB954]/20 to-[#282828] rounded-full flex items-center justify-center shadow-2xl">
                    {artist.isBand ? (
                        <Users className="h-16 w-16 text-[#1DB954]" />
                    ) : (
                        <Music2 className="h-16 w-16 text-[#1DB954]" />
                    )}
                </div>

                {/* Play Button */}
                <button className="play-button absolute right-2 bottom-2 w-12 h-12 bg-[#1DB954] rounded-full flex items-center justify-center shadow-xl hover:scale-110 hover:bg-[#1ed760] transition-all">
                    <Play className="h-5 w-5 text-black fill-black ml-0.5" />
                </button>
            </div>

            {/* Artist Info */}
            <div className="space-y-1">
                <h3 className="text-white font-bold truncate group-hover:underline">
                    {artist.name}
                </h3>
                <p className="text-sm text-[#b3b3b3] truncate">
                    {artist.isBand ? 'Banda' : 'Artista'} • {artist.albumCount} {artist.albumCount === 1 ? 'álbum' : 'álbuns'}
                </p>
            </div>
        </div>
    );
}