import { useNavigate } from 'react-router-dom';
import { Home, ArrowLeft } from 'lucide-react';
import { Button } from '../components/ui/Button';

export default function NotFound() {
    const navigate = useNavigate();

    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 via-white to-purple-50 p-4">
            <div className="text-center">
                <h1 className="text-9xl font-bold text-primary">404</h1>
                <h2 className="mt-4 text-3xl font-semibold text-gray-900">
                    Página não encontrada
                </h2>
                <p className="mt-2 text-gray-600 max-w-md mx-auto">
                    A página que você está procurando não existe ou foi removida.
                </p>

                <div className="mt-8 flex items-center justify-center space-x-4">
                    <Button onClick={() => navigate(-1)} variant="outline">
                        <ArrowLeft className="mr-2 h-4 w-4" />
                        Voltar
                    </Button>
                    <Button onClick={() => navigate('/')}>
                        <Home className="mr-2 h-4 w-4" />
                        Página Inicial
                    </Button>
                </div>
            </div>
        </div>
    );
}