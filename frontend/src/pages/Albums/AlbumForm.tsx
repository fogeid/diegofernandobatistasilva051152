import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Save, Upload, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { albumService } from '../../services/album.service';
import { artistService } from '../../services/artist.service';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Label } from '../../components/ui/Label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { LoadingPage, LoadingSpinner } from '../../components/common/LoadingSpinner';
import { getErrorMessage } from '../../lib/api';

const albumSchema = z.object({
    title: z.string().min(2, 'Título deve ter no mínimo 2 caracteres'),
    releaseYear: z
        .number()
        .min(1900, 'Ano inválido')
        .max(new Date().getFullYear() + 1, 'Ano não pode ser futuro'),
    artistIds: z.array(z.number()).min(1, 'Selecione pelo menos um artista'),
});

type AlbumFormData = z.infer<typeof albumSchema>;

export default function AlbumForm() {
    const navigate = useNavigate();
    const location = useLocation();
    const queryClient = useQueryClient();
    const { id } = useParams<{ id: string }>();

    const albumId = useMemo(() => (id ? Number(id) : null), [id]);
    const isEditMode = !!albumId;

    const preSelectedArtistId = location.state?.artistId;

    const [selectedArtists, setSelectedArtists] = useState<number[]>(
        preSelectedArtistId ? [preSelectedArtistId] : []
    );
    const [coverFiles, setCoverFiles] = useState<File[]>([]);
    const [coverPreviews, setCoverPreviews] = useState<string[]>([]);

    const { data: artists = [], isLoading: loadingArtists } = useQuery({
        queryKey: ['artists'],
        queryFn: artistService.getAll,
    });

    const { data: album, isLoading: loadingAlbum } = useQuery({
        queryKey: ['album', albumId],
        queryFn: () => albumService.getById(albumId as number),
        enabled: isEditMode && !!albumId,
    });

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<AlbumFormData>({
        resolver: zodResolver(albumSchema),
        defaultValues: {
            title: '',
            releaseYear: new Date().getFullYear(),
            artistIds: preSelectedArtistId ? [preSelectedArtistId] : [],
        },
    });

    useEffect(() => {
        if (!album) return;

        const ids = (album.artists || []).map((a) => a.id);

        setSelectedArtists(ids);

        reset({
            title: album.title,
            releaseYear: album.releaseYear,
            artistIds: ids,
        });
    }, [album, reset]);

    const saveMutation = useMutation({
        mutationFn: async (payload: AlbumFormData) => {
            const data = {
                ...payload,
                artistIds: selectedArtists,
            };

            if (isEditMode) {
                return albumService.update(albumId as number, data);
            }

            return albumService.create(data);
        },
        onSuccess: async (savedAlbum) => {
            if (coverFiles.length > 0) {
                try {
                    for (const file of coverFiles) {
                        await albumService.uploadCover(savedAlbum.id, file);
                    }
                    toast.success(isEditMode ? 'Álbum atualizado e capas enviadas!' : 'Álbum criado com capas enviadas!');
                } catch (error) {
                    toast.error((isEditMode ? 'Álbum atualizado' : 'Álbum criado') + ' mas erro ao enviar capas: ' + getErrorMessage(error));
                }
            } else {
                toast.success(isEditMode ? 'Álbum atualizado com sucesso!' : 'Álbum criado com sucesso!');
            }

            queryClient.invalidateQueries({ queryKey: ['albums'] });
            if (albumId) queryClient.invalidateQueries({ queryKey: ['album', albumId] });

            navigate('/artists');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const handleArtistToggle = (artistId: number) => {
        setSelectedArtists((prev) =>
            prev.includes(artistId) ? prev.filter((x) => x !== artistId) : [...prev, artistId]
        );
    };

    const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files || []);

        const validFiles = files.filter((file) => {
            if (!file.type.startsWith('image/')) {
                toast.error(`${file.name} não é uma imagem válida`);
                return false;
            }
            return true;
        });

        validFiles.forEach((file) => {
            const reader = new FileReader();
            reader.onloadend = () => setCoverPreviews((prev) => [...prev, reader.result as string]);
            reader.readAsDataURL(file);
        });

        setCoverFiles((prev) => [...prev, ...validFiles]);
    };

    const removeCover = (index: number) => {
        setCoverFiles((prev) => prev.filter((_, i) => i !== index));
        setCoverPreviews((prev) => prev.filter((_, i) => i !== index));
    };

    const onSubmit = (data: AlbumFormData) => {
        saveMutation.mutate({
            ...data,
            artistIds: selectedArtists,
        });
    };

    const isSubmitting = saveMutation.isPending;

    if (loadingArtists || (isEditMode && loadingAlbum)) {
        return <LoadingPage />;
    }

    if (isEditMode && !album) {
        return (
            <div className="flex justify-center py-12">
                <LoadingSpinner size="lg" />
            </div>
        );
    }

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center space-x-4">
                <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold">{isEditMode ? 'Editar Álbum' : 'Novo Álbum'}</h1>
                    <p className="text-muted-foreground">
                        {isEditMode ? 'Atualize os dados do álbum' : 'Preencha os dados para criar um novo álbum'}
                    </p>
                </div>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Informações do Álbum</CardTitle>
                        <CardDescription>Os campos marcados com * são obrigatórios</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="title">Título *</Label>
                            <Input id="title" placeholder="Ex: A Night at the Opera" {...register('title')} disabled={isSubmitting} />
                            {errors.title && <p className="text-sm text-destructive">{errors.title.message}</p>}
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="releaseYear">Ano de Lançamento *</Label>
                            <Input
                                id="releaseYear"
                                type="number"
                                placeholder="Ex: 1975"
                                {...register('releaseYear', { valueAsNumber: true })}
                                disabled={isSubmitting}
                            />
                            {errors.releaseYear && <p className="text-sm text-destructive">{errors.releaseYear.message}</p>}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Artistas *</CardTitle>
                        <CardDescription>Selecione um ou mais artistas para este álbum</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="grid gap-2 md:grid-cols-2">
                            {artists.map((artist) => (
                                <label
                                    key={artist.id}
                                    className="flex items-center space-x-2 cursor-pointer p-3 rounded-lg border hover:bg-accent"
                                >
                                    <input
                                        type="checkbox"
                                        checked={selectedArtists.includes(artist.id)}
                                        onChange={() => handleArtistToggle(artist.id)}
                                        disabled={isSubmitting}
                                        className="h-4 w-4 rounded text-primary focus:ring-2 focus:ring-primary"
                                    />
                                    <span>{artist.name}</span>
                                    <span className="text-xs text-muted-foreground">({artist.isBand ? 'Banda' : 'Solo'})</span>
                                </label>
                            ))}
                        </div>

                        {selectedArtists.length === 0 && (
                            <p className="text-sm text-destructive mt-2">Selecione pelo menos um artista</p>
                        )}
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Capas do Álbum</CardTitle>
                        <CardDescription>
                            {isEditMode
                                ? 'Envie novas capas (opcional). As capas antigas continuam, a menos que você as remova em outra tela.'
                                : 'Faça upload de uma ou mais capas (opcional)'}
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <label
                                htmlFor="cover"
                                className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-lg cursor-pointer hover:bg-accent"
                            >
                                <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                    <Upload className="w-8 h-8 mb-2 text-muted-foreground" />
                                    <p className="text-sm text-muted-foreground">Clique para selecionar imagens</p>
                                </div>
                                <input
                                    id="cover"
                                    type="file"
                                    accept="image/*"
                                    multiple
                                    onChange={handleCoverChange}
                                    disabled={isSubmitting}
                                    className="hidden"
                                />
                            </label>
                        </div>

                        {coverPreviews.length > 0 && (
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                {coverPreviews.map((preview, index) => (
                                    <div key={index} className="relative group">
                                        <img src={preview} alt={`Preview ${index + 1}`} className="w-full h-32 object-cover rounded-lg" />
                                        <button
                                            type="button"
                                            onClick={() => removeCover(index)}
                                            className="absolute top-2 right-2 p-1 bg-destructive text-destructive-foreground rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                                        >
                                            <X className="h-4 w-4" />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>

                <div className="flex items-center space-x-4">
                    <Button type="submit" disabled={isSubmitting || selectedArtists.length === 0}>
                        <Save className="mr-2 h-4 w-4" />
                        {isSubmitting ? 'Salvando...' : isEditMode ? 'Salvar Alterações' : 'Criar Álbum'}
                    </Button>

                    <Button type="button" variant="outline" onClick={() => navigate(-1)} disabled={isSubmitting}>
                        Cancelar
                    </Button>
                </div>
            </form>
        </div>
    );
}
