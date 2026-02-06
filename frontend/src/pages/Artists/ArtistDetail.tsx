import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Edit,
    Trash2,
    Plus,
    Music2,
    Users,
    Calendar,
    Play,
    MoreVertical,
    Search,
    ArrowUpAZ,
    ArrowDownAZ,
} from 'lucide-react';
import { useMemo, useState, useEffect } from 'react';
import toast from 'react-hot-toast';

import { artistService } from '../../services/artist.service';
import { albumService } from '../../services/album.service';
import { Button } from '../../components/ui/Button';
import { LoadingPage, LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { getErrorMessage } from '../../lib/api';
import type { AlbumResponse } from '../../types/api.types';

type SortDir = 'asc' | 'desc';

export default function ArtistDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [search, setSearch] = useState('');
    const [sortDir, setSortDir] = useState<SortDir>('asc');

    const { data: artist, isLoading: loadingArtist, error } = useQuery({
        queryKey: ['artist', id],
        queryFn: () => artistService.getById(Number(id)),
        enabled: !!id,
    });

    const { data: allAlbums = [], isLoading: loadingAlbums } = useQuery({
        queryKey: ['albums'],
        queryFn: albumService.getAll,
        enabled: !!artist,
        refetchOnMount: 'always',
        refetchOnWindowFocus: true,
        staleTime: 0,
    });

    const artistAlbums = useMemo(() => {
        const base = allAlbums.filter((album) =>
            album.artists.some((a) => a.id === Number(id))
        );

        const q = search.trim().toLowerCase();
        const filtered = q ? base.filter((a) => a.title.toLowerCase().includes(q)) : base;

        const sorted = [...filtered].sort((a, b) => {
            const at = a.title.toLowerCase();
            const bt = b.title.toLowerCase();
            return sortDir === 'asc' ? at.localeCompare(bt) : bt.localeCompare(at);
        });

        return sorted;
    }, [allAlbums, id, search, sortDir]);

    useEffect(() => {
        if (error) toast.error(getErrorMessage(error));
    }, [error]);

    const handleDelete = async () => {
        if (!artist) return;

        const confirmed = window.confirm(
            `Tem certeza que deseja deletar o artista "${artist.name}"?`
        );
        if (!confirmed) return;

        try {
            await artistService.delete(artist.id);
            toast.success('Artista deletado com sucesso!');
            navigate('/artists');
        } catch (err) {
            toast.error(getErrorMessage(err));
        }
    };

    if (loadingArtist) return <LoadingPage />;

    if (error || !artist) {
        return (
            <div className="container py-8">
                <EmptyState
                    title="Artista não encontrado"
                    description="O artista que você procura não existe ou foi removido."
                    actionLabel="Voltar para Artistas"
                    onAction={() => navigate('/artists')}
                />
            </div>
        );
    }

    return (
        <div className="space-y-8 pt-6">
            <div className="flex items-start gap-8">
                <div className="flex-shrink-0">
                    <div className="flex h-56 w-56 items-center justify-center rounded-full bg-gradient-to-br from-[#1DB954]/20 to-[#282828] shadow-2xl">
                        {artist.isBand ? (
                            <Users className="h-24 w-24 text-[#1DB954]" />
                        ) : (
                            <Music2 className="h-24 w-24 text-[#1DB954]" />
                        )}
                    </div>
                </div>

                <div className="flex-1 space-y-4">
                    <div>
                        <p className="text-sm font-semibold uppercase text-[#b3b3b3]">
                            {artist.isBand ? 'Banda' : 'Artista'}
                        </p>
                        <h1 className="mb-4 mt-2 text-6xl font-bold text-white">{artist.name}</h1>
                        <p className="text-[#b3b3b3]">
                            {artistAlbums.length} {artistAlbums.length === 1 ? 'álbum' : 'álbuns'}
                        </p>
                    </div>

                    <div className="flex items-center gap-4 pt-4">
                        <Button
                            onClick={() => navigate(`/artists/${artist.id}/edit`)}
                            variant="secondary"
                            className="h-10"
                        >
                            <Edit className="mr-2 h-4 w-4" />
                            Editar
                        </Button>

                        <Button onClick={handleDelete} variant="destructive" className="h-10">
                            <Trash2 className="mr-2 h-4 w-4" />
                            Deletar
                        </Button>

                        <button
                            onClick={() => navigate(-1)}
                            className="text-[#b3b3b3] transition-colors hover:text-white"
                            aria-label="Voltar"
                        >
                            <ArrowLeft className="h-6 w-6" />
                        </button>
                    </div>
                </div>
            </div>

            <div className="space-y-4">
                <div className="flex items-center justify-between">
                    <h2 className="text-2xl font-bold text-white">Discografia</h2>

                    <Button
                        onClick={() => navigate('/albums/new', { state: { artistId: artist.id } })}
                        variant="primary"
                        size="sm"
                    >
                        <Plus className="mr-2 h-4 w-4" />
                        Adicionar Álbum
                    </Button>
                </div>

                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="relative w-full sm:max-w-md">
                        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-white/60" />
                        <input
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder="Buscar álbum por título..."
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

                {loadingAlbums ? (
                    <div className="flex justify-center py-12">
                        <LoadingSpinner size="lg" />
                    </div>
                ) : artistAlbums.length === 0 ? (
                    <EmptyState
                        title={allAlbums.length === 0 ? 'Nenhum álbum cadastrado' : 'Nenhum resultado'}
                        description={
                            allAlbums.length === 0
                                ? `${artist.name} ainda não possui álbuns cadastrados.`
                                : 'Tente buscar por outro título.'
                        }
                        actionLabel={allAlbums.length === 0 ? 'Adicionar Álbum' : 'Limpar busca'}
                        onAction={() => {
                            if (allAlbums.length === 0) navigate('/albums/new', { state: { artistId: artist.id } });
                            else setSearch('');
                        }}
                        icon={<Music2 className="h-16 w-16" />}
                    />
                ) : (
                    <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
                        {artistAlbums.map((album, index) => (
                            <AlbumCard key={album.id} album={album} index={index} />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

interface AlbumCardProps {
    album: AlbumResponse;
    index: number;
}

function AlbumCard({ album, index }: AlbumCardProps) {
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [showMenu, setShowMenu] = useState(false);
    const [imageError, setImageError] = useState(false);

    const coverUrl = album.covers && album.covers.length > 0 ? album.covers[0].imageUrl : null;

    const deleteMutation = useMutation({
        mutationFn: () => albumService.delete(album.id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['albums'] });
            toast.success('Álbum deletado com sucesso!');
        },
        onError: (err) => {
            toast.error(getErrorMessage(err));
        },
    });

    const handleDelete = async () => {
        const confirmed = window.confirm(`Tem certeza que deseja deletar o álbum "${album.title}"?`);
        if (confirmed) deleteMutation.mutate();
    };

    return (
        <div
            className="group relative rounded-lg bg-[#181818] p-4 spotify-card animate-fade-in"
            style={{ animationDelay: `${index * 50}ms` }}
        >
            <div className="absolute right-4 top-4 z-10">
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        setShowMenu(!showMenu);
                    }}
                    className="flex h-8 w-8 items-center justify-center rounded-full bg-black/60 text-white opacity-0 transition-all hover:bg-black/80 group-hover:opacity-100"
                    type="button"
                >
                    <MoreVertical className="h-4 w-4" />
                </button>

                {showMenu && (
                    <div className="absolute right-0 top-10 min-w-[150px] rounded-md bg-[#282828] py-1 shadow-xl">
                        <button
                            type="button"
                            onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/albums/${album.id}/edit`);
                                setShowMenu(false);
                            }}
                            className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-white transition-colors hover:bg-[#3e3e3e]"
                        >
                            <Edit className="h-4 w-4" />
                            Editar
                        </button>

                        <button
                            type="button"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleDelete();
                                setShowMenu(false);
                            }}
                            className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-[#e22134] transition-colors hover:bg-[#3e3e3e]"
                        >
                            <Trash2 className="h-4 w-4" />
                            Deletar
                        </button>
                    </div>
                )}
            </div>

            <div className="relative mb-4 aspect-square cursor-pointer">
                {coverUrl && !imageError ? (
                    <img
                        src={coverUrl}
                        alt={album.title}
                        className="h-full w-full rounded-md object-cover shadow-2xl"
                        onError={() => setImageError(true)}
                        crossOrigin="anonymous"
                    />
                ) : (
                    <div className="flex h-full w-full items-center justify-center rounded-md bg-gradient-to-br from-[#1DB954]/20 to-[#282828] shadow-2xl">
                        <Music2 className="h-16 w-16 text-[#1DB954]" />
                    </div>
                )}

                <button
                    type="button"
                    className="play-button absolute bottom-2 right-2 flex h-12 w-12 items-center justify-center rounded-full bg-[#1DB954] shadow-xl transition-all hover:scale-110 hover:bg-[#1ed760]"
                >
                    <Play className="ml-0.5 h-5 w-5 fill-black text-black" />
                </button>
            </div>

            <div className="space-y-1">
                <h3 className="truncate font-bold text-white group-hover:underline">{album.title}</h3>

                <div className="flex items-center gap-2 text-sm text-[#b3b3b3]">
                    <Calendar className="h-3 w-3" />
                    <span>{album.releaseYear}</span>
                </div>

                {album.artists.length > 0 && (
                    <p className="truncate text-sm text-[#b3b3b3]">
                        {album.artists.map((a) => a.name).join(', ')}
                    </p>
                )}

                {album.covers && album.covers.length > 0 && (
                    <p className="text-xs text-[#535353]">
                        {album.covers.length} {album.covers.length === 1 ? 'capa' : 'capas'}
                    </p>
                )}
            </div>
        </div>
    );
}
