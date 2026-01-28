import { cn } from '../../lib/utils';

interface LoadingSpinnerProps {
    className?: string;
    size?: 'sm' | 'md' | 'lg';
}

export function LoadingSpinner({ className, size = 'md' }: LoadingSpinnerProps) {
    const sizeClasses = {
        sm: 'h-4 w-4 border-2',
        md: 'h-8 w-8 border-3',
        lg: 'h-12 w-12 border-4',
    };

    return (
        <div className="flex items-center justify-center">
            <div
                className={cn(
                    'animate-spin rounded-full border-primary border-t-transparent',
                    sizeClasses[size],
                    className
                )}
            />
        </div>
    );
}

export function LoadingPage() {
    return (
        <div className="flex min-h-screen items-center justify-center">
            <div className="text-center">
                <LoadingSpinner size="lg" />
                <p className="mt-4 text-muted-foreground">Carregando...</p>
            </div>
        </div>
    );
}