import { LogOut, Music, User, Library, Plus } from 'lucide-react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { authStore } from '../../stores/authStore';

interface MainLayoutProps {
    children: React.ReactNode;
}

export function MainLayout({ children }: MainLayoutProps) {
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = authStore();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isActive = (path: string) => location.pathname === path;

    return (
        <div className="h-screen flex bg-black">
            <aside className="w-64 bg-black flex flex-col sidebar-animate">
                <div className="p-6">
                    <Link to="/" className="flex items-center space-x-2 group">
                        <Music className="h-8 w-8 text-[#1DB954]" />
                        <span className="text-xl font-bold text-white">
              Seplag Music
            </span>
                    </Link>
                </div>

                <nav className="flex-1 px-3 space-y-2">
                    <Link
                        to="/artists"
                        className={`flex items-center space-x-4 px-4 py-3 rounded-md transition-all ${
                            isActive('/artists')
                                ? 'bg-[#282828] text-white'
                                : 'text-[#b3b3b3] hover:text-white'
                        }`}
                    >
                        <Library className="h-6 w-6" />
                        <span className="font-semibold">Sua Biblioteca</span>
                    </Link>

                    <button
                        onClick={() => navigate('/artists/new')}
                        className="w-full flex items-center space-x-4 px-4 py-3 rounded-md text-[#b3b3b3] hover:text-white transition-all"
                    >
                        <Plus className="h-6 w-6" />
                        <span className="font-semibold">Criar Artista</span>
                    </button>
                </nav>

                <div className="p-4 border-t border-[#282828]">
                    <div className="flex items-center justify-between px-4 py-3 rounded-md bg-[#181818] hover:bg-[#282828] transition-all">
                        <div className="flex items-center space-x-3">
                            <div className="w-8 h-8 rounded-full bg-[#535353] flex items-center justify-center">
                                <User className="h-4 w-4 text-white" />
                            </div>
                            <span className="text-sm font-semibold text-white truncate max-w-[120px]">
                {user?.username}
              </span>
                        </div>
                        <button
                            onClick={handleLogout}
                            className="text-[#b3b3b3] hover:text-white transition-colors"
                            title="Sair"
                        >
                            <LogOut className="h-5 w-5" />
                        </button>
                    </div>
                </div>
            </aside>

            <main className="flex-1 flex flex-col overflow-hidden">
                <header className="h-16 bg-gradient-to-b from-[#121212] to-transparent flex items-center justify-between px-8 backdrop-blur-sm">
                    <div className="flex items-center space-x-4">
                        <button
                            onClick={() => navigate(-1)}
                            className="w-8 h-8 rounded-full bg-black/40 flex items-center justify-center text-white hover:bg-black/60 transition-all"
                        >
                            ←
                        </button>
                        <button
                            onClick={() => navigate(1)}
                            className="w-8 h-8 rounded-full bg-black/40 flex items-center justify-center text-white hover:bg-black/60 transition-all"
                        >
                            →
                        </button>
                    </div>
                </header>

                <div className="flex-1 overflow-y-auto bg-gradient-to-b from-[#121212] to-black px-8 pb-8">
                    <div className="max-w-[1955px] mx-auto animate-fade-in">
                        {children}
                    </div>
                </div>
            </main>
        </div>
    );
}