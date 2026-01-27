import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

export function formatDate(date: string): string {
    return new Date(date).toLocaleDateString('pt-BR');
}

export function formatDateTime(date: string): string {
    return new Date(date).toLocaleString('pt-BR');
}

export function truncate(str: string, length: number): string {
    return str.length > length ? str.substring(0, length) + '...' : str;
}