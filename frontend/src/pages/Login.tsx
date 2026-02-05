import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { authService } from '../services/auth.service';
import { authStore } from '../stores/authStore';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import { getErrorMessage } from '../lib/api';

const loginSchema = z.object({
    username: z.string().min(3, 'Username deve ter no mínimo 3 caracteres'),
    password: z.string().min(3, 'Senha deve ter no mínimo 3 caracteres'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function Login() {
    const navigate = useNavigate();
    const { login } = authStore();
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormData) => {
        setIsLoading(true);

        try {
            const response = await authService.login(data);

            if (!response || !response.accessToken) {
                throw new Error('Resposta inválida da API');
            }

            login(response.accessToken, response.refreshToken);
            toast.success('Login realizado com sucesso!');
            navigate('/artists');
        } catch (error) {
            toast.error(getErrorMessage(error));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-[#121212] to-black flex flex-col">
            <div className="flex-1 flex items-center justify-center px-4">
                <div className="w-full max-w-md space-y-8">
                    <div className="text-center">
                        <span className="text-3xl font-bold text-white">Seplag Music</span>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="username" className="text-white text-sm font-semibold">
                                Username
                            </Label>
                            <Input
                                id="username"
                                placeholder="Username"
                                autoComplete="username"
                                {...register('username')}
                                disabled={isLoading}
                                className="h-12 bg-[#121212] border-[#727272] text-white placeholder:text-[#727272] focus:border-white rounded-md"
                            />
                            {errors.username && (
                                <p className="text-sm text-[#e22134]">{errors.username.message}</p>
                            )}
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="password" className="text-white text-sm font-semibold">
                                Senha
                            </Label>
                            <Input
                                id="password"
                                type="password"
                                placeholder="Senha"
                                autoComplete="current-password"
                                {...register('password')}
                                disabled={isLoading}
                                className="h-12 bg-[#121212] border-[#727272] text-white placeholder:text-[#727272] focus:border-white rounded-md"
                            />
                            {errors.password && (
                                <p className="text-sm text-[#e22134]">{errors.password.message}</p>
                            )}
                        </div>

                        <Button
                            type="submit"
                            variant="primary"
                            className="w-full h-12 text-base mt-8"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Entrando...' : 'Entrar'}
                        </Button>
                    </form>

                    <div className="mt-8 p-4 bg-[#181818] rounded-lg border border-[#282828]">
                        <p className="text-[#b3b3b3] text-sm text-center mb-2">
                            Credenciais de teste:
                        </p>
                        <p className="text-white text-sm text-center font-mono">
                            <strong>Username:</strong> admin <br />
                            <strong>Senha:</strong> admin123
                        </p>
                    </div>
                </div>
            </div>

            <footer className="p-8 text-center text-[#727272] text-sm">
                <p>© 2026 Seplag Music • Processo Seletivo Seplag 2026</p>
            </footer>
        </div>
    );
}