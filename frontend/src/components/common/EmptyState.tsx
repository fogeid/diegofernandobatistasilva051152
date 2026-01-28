import { FileQuestion } from 'lucide-react';
import { Button } from '../ui/Button';

interface EmptyStateProps {
    title: string;
    description?: string;
    actionLabel?: string;
    onAction?: () => void;
    icon?: React.ReactNode;
}

export function EmptyState({
                               title,
                               description,
                               actionLabel,
                               onAction,
                               icon,
                           }: EmptyStateProps) {
    return (
        <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="mb-4 text-muted-foreground">
                {icon || <FileQuestion className="h-16 w-16" />}
            </div>
            <h3 className="mb-2 text-lg font-semibold">{title}</h3>
            {description && (
                <p className="mb-4 max-w-md text-sm text-muted-foreground">
                    {description}
                </p>
            )}
            {actionLabel && onAction && (
                <Button onClick={onAction}>{actionLabel}</Button>
            )}
        </div>
    );
}