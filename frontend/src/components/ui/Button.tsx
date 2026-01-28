import { forwardRef } from 'react';
import type { ButtonHTMLAttributes } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '../../lib/utils';

const buttonVariants = cva(
    'inline-flex items-center justify-center rounded-full text-sm font-bold transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#1DB954] disabled:opacity-50 disabled:pointer-events-none active:scale-95',
    {
        variants: {
            variant: {
                default: 'bg-white text-black hover:scale-105',
                primary: 'bg-[#1DB954] text-black hover:bg-[#1ed760] hover:scale-105',
                secondary: 'bg-[#282828] text-white hover:bg-[#3e3e3e]',
                outline: 'border border-[#727272] text-white hover:border-white',
                ghost: 'text-[#b3b3b3] hover:text-white hover:bg-[#282828]',
                destructive: 'bg-[#e22134] text-white hover:bg-[#ff4d5a]',
            },
            size: {
                default: 'h-10 px-6',
                sm: 'h-8 px-4 text-xs',
                lg: 'h-12 px-8 text-base',
                icon: 'h-10 w-10',
            },
        },
        defaultVariants: {
            variant: 'default',
            size: 'default',
        },
    }
);

export interface ButtonProps
    extends ButtonHTMLAttributes<HTMLButtonElement>,
        VariantProps<typeof buttonVariants> {
    asChild?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant, size, ...props }, ref) => {
        return (
            <button
                className={cn(buttonVariants({ variant, size, className }))}
                ref={ref}
                {...props}
            />
        );
    }
);

Button.displayName = 'Button';

export { Button, buttonVariants };