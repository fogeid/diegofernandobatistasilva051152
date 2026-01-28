import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { artistService } from '../../services/artist.service';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Label } from '../../components/ui/Label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { LoadingPage } from '../../components/common/LoadingSpinner';
import { getErrorMessage } from '../../lib/api';

const artistSchema = z.object({
    name: z.string().min(2, 'Nome deve ter no mínimo 2 caracteres'),
    isBand: z.boolean(),
});

type ArtistFormData = z.infer<typeof artistSchema>;

export default function ArtistForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const isEditing = !!id;

    const { data: artist, isLoading } = useQuery({
        queryKey: ['artist', id],
        queryFn: () => artistService.getById(Number(id)),
        enabled: isEditing,
    });

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        formState: { errors },
    } = useForm<ArtistFormData>({
        resolver: zodResolver(artistSchema),
        defaultValues: {
            name: '',
            isBand: false,
        },
    });

    useEffect(() => {
        if (artist) {
            setValue('name', artist.name);
            setValue('isBand', artist.isBand);
        }
    }, [artist, setValue]);

    const createMutation = useMutation({
        mutationFn: artistService.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['artists'] });
            toast.success('Artista criado com sucesso!');
            navigate('/artists');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: ArtistFormData) =>
            artistService.update(Number(id), data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['artists'] });
            queryClient.invalidateQueries({ queryKey: ['artist', id] });
            toast.success('Artista atualizado com sucesso!');
            navigate(`/artists/${id}`);
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const onSubmit = (data: ArtistFormData) => {
        if (isEditing) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    const isSubmitting = createMutation.isPending || updateMutation.isPending;
    const isBand = watch('isBand');

    if (isLoading) {
        return <LoadingPage />;
    }

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex items-center space-x-4">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => navigate(isEditing ? `/artists/${id}` : '/artists')}
                >
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold">
                        {isEditing ? 'Editar Artista' : 'Novo Artista'}
                    </h1>
                    <p className="text-muted-foreground">
                        {isEditing
                            ? 'Atualize as informações do artista'
                            : 'Preencha os dados para criar um novo artista'}
                    </p>
                </div>
            </div>

            {/* Formulário */}
            <Card>
                <CardHeader>
                    <CardTitle>Informações do Artista</CardTitle>
                    <CardDescription>
                        Os campos marcados com * são obrigatórios
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        {/* Nome */}
                        <div className="space-y-2">
                            <Label htmlFor="name">Nome *</Label>
                            <Input
                                id="name"
                                placeholder="Ex: Queen, Freddie Mercury"
                                {...register('name')}
                                disabled={isSubmitting}
                            />
                            {errors.name && (
                                <p className="text-sm text-destructive">{errors.name.message}</p>
                            )}
                        </div>

                        {/* Tipo */}
                        <div className="space-y-2">
                            <Label>Tipo *</Label>
                            <div className="flex items-center space-x-4">
                                <label className="flex items-center space-x-2 cursor-pointer">
                                    <input
                                        type="radio"
                                        value="false"
                                        checked={!isBand}
                                        onChange={() => setValue('isBand', false)}
                                        disabled={isSubmitting}
                                        className="h-4 w-4 text-primary focus:ring-2 focus:ring-primary"
                                    />
                                    <span>Artista Solo</span>
                                </label>
                                <label className="flex items-center space-x-2 cursor-pointer">
                                    <input
                                        type="radio"
                                        value="true"
                                        checked={isBand}
                                        onChange={() => setValue('isBand', true)}
                                        disabled={isSubmitting}
                                        className="h-4 w-4 text-primary focus:ring-2 focus:ring-primary"
                                    />
                                    <span>Banda</span>
                                </label>
                            </div>
                        </div>

                        {/* Botões */}
                        <div className="flex items-center space-x-4">
                            <Button type="submit" disabled={isSubmitting}>
                                <Save className="mr-2 h-4 w-4" />
                                {isSubmitting
                                    ? 'Salvando...'
                                    : isEditing
                                        ? 'Atualizar'
                                        : 'Criar Artista'}
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => navigate(isEditing ? `/artists/${id}` : '/artists')}
                                disabled={isSubmitting}
                            >
                                Cancelar
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}