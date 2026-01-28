import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Edit, Trash2, Plus, Music2, Users, Calendar, Play, MoreVertical } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { artistService } from '../../services/artist.service';
import { albumService } from '../../services/album.service';
import { Button } from '../../components/ui/Button';
import { LoadingPage, LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { getErrorMessage } from '../../lib/api';
import type { AlbumResponse } from '../../types/api.types';

export default function ArtistDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const { data: artist, isLoading: loadingArtist, error } = useQuery({
        queryKey: ['artist', id],
        queryFn: () => artistService.getById(Number(id)),
        enabled: !!id,
    });

    const { data: allAlbums = [], isLoading: loadingAlbums } = useQuery({
        queryKey: ['albums'],
        queryFn: albumService.getAll,
        enabled: !!artist,
    });

    const artistAlbums = allAlbums.filter((album) =>
        album.artists.some((a) => a.id === Number(id))
    );

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
        } catch (error) {
            toast.error(getErrorMessage(error));
        }
    };

    if (loadingArtist) {
        return <LoadingPage />;
    }

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
        <div className="pt-6 space-y-8">
            {/* Header */}
            <div className="flex items-start gap-8">
                {/* Artist Image */}
                <div className="flex-shrink-0">
                    <div className="w-56 h-56 bg-gradient-to-br from-[#1DB954]/20 to-[#282828] rounded-full flex items-center justify-center shadow-2xl">
                        {artist.isBand ? (
                            <Users className="h-24 w-24 text-[#1DB954]" />
                        ) : (
                            <Music2 className="h-24 w-24 text-[#1DB954]" />
                        )}
                    </div>
                </div>

                {/* Artist Info */}
                <div className="flex-1 space-y-4">
                    <div>
                        <p className="text-sm text-[#b3b3b3] uppercase font-semibold">
                            {artist.isBand ? 'Banda' : 'Artista'}
                        </p>
                        <h1 className="text-6xl font-bold text-white mt-2 mb-4">
                            {artist.name}
                        </h1>
                        <p className="text-[#b3b3b3]">
                            {artistAlbums.length} {artistAlbums.length === 1 ? 'álbum' : 'álbuns'}
                        </p>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-4 pt-4">
                        <Button
                            onClick={() => navigate(`/artists/${artist.id}/edit`)}
                            variant="secondary"
                            className="h-10"
                        >
                            <Edit className="mr-2 h-4 w-4" />
                            Editar
                        </Button>
                        <Button
                            onClick={handleDelete}
                            variant="destructive"
                            className="h-10"
                        >
                            <Trash2 className="mr-2 h-4 w-4" />
                            Deletar
                        </Button>
                        <button
                            onClick={() => navigate(-1)}
                            className="text-[#b3b3b3] hover:text-white transition-colors"
                        >
                            <ArrowLeft className="h-6 w-6" />
                        </button>
                    </div>
                </div>
            </div>

            {/* Albums Section */}
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

                {loadingAlbums ? (
                    <div className="flex justify-center py-12">
                        <LoadingSpinner size="lg" />
                    </div>
                ) : artistAlbums.length === 0 ? (
                    <EmptyState
                        title="Nenhum álbum cadastrado"
                        description={`${artist.name} ainda não possui álbuns cadastrados.`}
                        actionLabel="Adicionar Álbum"
                        onAction={() => navigate('/albums/new', { state: { artistId: artist.id } })}
                        icon={<Music2 className="h-16 w-16" />}
                    />
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
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
    const [showMenu, setShowMenu] = useState(false);
    const [imageError, setImageError] = useState(false);

    const coverUrl =
        album.covers && album.covers.length > 0
            ? album.covers[0].imageUrl
            : null;

    const deleteMutation = useMutation({
        mutationFn: () => albumService.delete(album.id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['albums'] });
            toast.success('Álbum deletado com sucesso!');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const handleDelete = async () => {
        const confirmed = window.confirm(
            `Tem certeza que deseja deletar o álbum "${album.title}"?`
        );

        if (confirmed) {
            deleteMutation.mutate();
        }
    };

    const handleImageError = () => {
        console.log('Erro ao carregar imagem:', coverUrl);
        setImageError(true);
    };

    const handleImageLoad = () => {
        console.log('Imagem carregada com sucesso:', coverUrl);
    };

    // Debug: mostrar URL no console
    console.log('Album:', album.title, 'Cover URL:', coverUrl);

    return (
        <div
            className="group relative p-4 bg-[#181818] rounded-lg spotify-card animate-fade-in"
            style={{ animationDelay: `${index * 50}ms` }}
        >
            {/* Menu Actions */}
            <div className="absolute top-4 right-4 z-10">
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        setShowMenu(!showMenu);
                    }}
                    className="w-8 h-8 rounded-full bg-black/60 flex items-center justify-center text-white hover:bg-black/80 transition-all opacity-0 group-hover:opacity-100"
                >
                    <MoreVertical className="h-4 w-4" />
                </button>

                {showMenu && (
                    <div className="absolute top-10 right-0 bg-[#282828] rounded-md shadow-xl min-w-[150px] py-1">
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                // TODO: Implementar edição de álbum
                                toast('Edição de álbum em breve!');
                                setShowMenu(false);
                            }}
                            className="w-full px-4 py-2 text-left text-sm text-white hover:bg-[#3e3e3e] transition-colors flex items-center gap-2"
                        >
                            <Edit className="h-4 w-4" />
                            Editar
                        </button>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                handleDelete();
                                setShowMenu(false);
                            }}
                            className="w-full px-4 py-2 text-left text-sm text-[#e22134] hover:bg-[#3e3e3e] transition-colors flex items-center gap-2"
                        >
                            <Trash2 className="h-4 w-4" />
                            Deletar
                        </button>
                    </div>
                )}
            </div>

            {/* Album Cover */}
            <div className="relative mb-4 aspect-square cursor-pointer">
                {coverUrl && !imageError ? (
                    <img
                        src={coverUrl}
                        alt={album.title}
                        className="w-full h-full object-cover rounded-md shadow-2xl"
                        onError={handleImageError}
                        onLoad={handleImageLoad}
                        crossOrigin="anonymous"
                    />
                ) : (
                    <div className="w-full h-full bg-gradient-to-br from-[#1DB954]/20 to-[#282828] rounded-md flex items-center justify-center shadow-2xl">
                        <Music2 className="h-16 w-16 text-[#1DB954]" />
                    </div>
                )}

                {/* Play Button */}
                <button className="play-button absolute right-2 bottom-2 w-12 h-12 bg-[#1DB954] rounded-full flex items-center justify-center shadow-xl hover:scale-110 hover:bg-[#1ed760] transition-all">
                    <Play className="h-5 w-5 text-black fill-black ml-0.5" />
                </button>
            </div>

            {/* Album Info */}
            <div className="space-y-1">
                <h3 className="text-white font-bold truncate group-hover:underline">
                    {album.title}
                </h3>
                <div className="flex items-center gap-2 text-sm text-[#b3b3b3]">
                    <Calendar className="h-3 w-3" />
                    <span>{album.releaseYear}</span>
                </div>

                {/* Artists */}
                {album.artists.length > 0 && (
                    <p className="text-sm text-[#b3b3b3] truncate">
                        {album.artists.map(a => a.name).join(', ')}
                    </p>
                )}

                {/* Covers count */}
                {album.covers && album.covers.length > 0 && (
                    <p className="text-xs text-[#535353]">
                        {album.covers.length} {album.covers.length === 1 ? 'capa' : 'capas'}
                    </p>
                )}
            </div>
        </div>
    );
}