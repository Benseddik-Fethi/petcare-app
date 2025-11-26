import * as React from "react"
import { cn } from "@/lib/utils"
import type {LucideIcon} from "lucide-react";

// On étend les props HTML standards pour y ajouter notre prop 'icon' optionnelle
export interface InputProps
    extends React.InputHTMLAttributes<HTMLInputElement> {
    icon?: LucideIcon;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
    ({ className, type, icon: Icon, ...props }, ref) => {
        if (Icon) {
            return (
                <div className="relative w-full">
                    <div className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                        <Icon className="h-4 w-4" />
                    </div>
                    <input
                        type={type}
                        className={cn(
                            "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
                            "pl-10",
                            className
                        )}
                        ref={ref}
                        {...props}
                    />
                </div>
            )
        }

        return (
            <input
                type={type}
                className={cn(
                    "flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
                    className
                )}
                ref={ref}
                {...props}
            />
        )
    }
)
Input.displayName = "Input"

export { Input }