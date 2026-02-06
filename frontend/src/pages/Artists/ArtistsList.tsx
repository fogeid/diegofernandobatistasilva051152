import { useMemo, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Plus, Search, ArrowUpAZ, ArrowDownAZ } from 'lucide-react';
import toast from 'react-hot-toast';

import { artistService } from '../../services/artist.service';
import { albumService } from '../../services/album.service';

import { Button } from '../../components/ui/Button';
import { LoadingPage } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { ArtistCard as ArtistCardItem } from '../../components/artists/ArtistCard';
import { getErrorMessage } from '../../lib/api';

type SortDir = 'asc' | 'desc';

export default function ArtistsList() {
    const navigate = useNavigate();

    const [search, setSearch] = useState('');
    const [sortDir, setSortDir] = useState<SortDir>('asc');

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

    const filteredAndSorted = useMemo(() => {
        const q = search.trim().toLowerCase();

        const filtered = q
            ? artistsWithAlbumCount.filter((a) => a.name.toLowerCase().includes(q))
            : artistsWithAlbumCount;

        const sorted = [...filtered].sort((a, b) => {
            const an = a.name.toLowerCase();
            const bn = b.name.toLowerCase();
            return sortDir === 'asc' ? an.localeCompare(bn) : bn.localeCompare(an);
        });

        return sorted;
    }, [artistsWithAlbumCount, search, sortDir]);

    useEffect(() => {
        if (error) toast.error(getErrorMessage(error));
    }, [error]);

    if (loadingArtists) return <LoadingPage />;

    return (
        <div className="space-y-6 pt-6">
            <div className="flex items-end justify-between">
                <div>
                    <h1 className="mb-2 text-4xl font-bold text-white">Seus Artistas</h1>
                    <p className="text-sm text-[#b3b3b3]">
                        {filteredAndSorted.length}{' '}
                        {filteredAndSorted.length === 1 ? 'artista' : 'artistas'}
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

            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="relative w-full sm:max-w-md">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-white/60" />
                    <input
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Buscar artista por nome..."
                        className="w-full rounded-xl border border-white/10 bg-white/5 py-2 pl-10 pr-3 text-white placeholder:text-white/40 outline-none focus:border-white/20"
                    />
                </div>

                <Button
                    type="button"
                    variant="outline"
                    onClick={() => setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))}
                    className="border-white/10 bg-white/5 text-white hover:bg-white/10"
                >
                    {sortDir === 'asc' ? (
                        <>
                            <ArrowUpAZ className="mr-2 h-4 w-4" />
                            A–Z
                        </>
                    ) : (
                        <>
                            <ArrowDownAZ className="mr-2 h-4 w-4" />
                            Z–A
                        </>
                    )}
                </Button>
            </div>

            {filteredAndSorted.length === 0 ? (
                <EmptyState
                    title={artistsWithAlbumCount.length === 0 ? 'Nenhum artista encontrado' : 'Nenhum resultado'}
                    description={
                        artistsWithAlbumCount.length === 0
                            ? 'Comece cadastrando seu primeiro artista'
                            : 'Tente buscar por outro nome.'
                    }
                    actionLabel={artistsWithAlbumCount.length === 0 ? 'Criar Artista' : 'Limpar busca'}
                    onAction={() => {
                        if (artistsWithAlbumCount.length === 0) navigate('/artists/new');
                        else setSearch('');
                    }}
                />
            ) : (
                <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-6">
                    {filteredAndSorted.map((artist, index) => (
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
