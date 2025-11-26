import { useAuth } from "@/context/AuthContext";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Home, Dog, Calendar, Settings, LogOut, PawPrint } from "lucide-react";
import { cn } from "@/lib/utils"; // Import pour gérer les classes conditionnelles

export default function DashboardLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const navItems = [
        { id: "/dashboard", label: "Dashboard", icon: Home },
        { id: "/pets", label: "Mes animaux", icon: Dog },
        { id: "/calendar", label: "Calendrier", icon: Calendar },
        { id: "/settings", label: "Paramètres", icon: Settings },
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-rose-50 via-pink-50 to-amber-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 flex">
            {/* SIDEBAR */}
            <div className="w-72 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl border-r border-rose-100 dark:border-slate-800 p-6 flex flex-col fixed h-full z-20">
                <div className="flex items-center gap-3 mb-10">
                    <div className="w-12 h-12 bg-gradient-to-br from-rose-400 to-pink-400 rounded-2xl flex items-center justify-center shadow-lg shadow-rose-200 dark:shadow-none">
                        <PawPrint size={24} className="text-white" />
                    </div>
                    <span className="text-xl font-bold bg-gradient-to-r from-rose-500 to-pink-500 bg-clip-text text-transparent">
            PetCare
          </span>
                </div>

                <nav className="flex-1 space-y-2">
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.id;
                        return (
                            <button
                                key={item.id}
                                onClick={() => navigate(item.id)}
                                className={cn(
                                    "w-full flex items-center gap-3 px-4 py-3 rounded-2xl transition-all duration-300",
                                    isActive
                                        ? "bg-gradient-to-r from-rose-400 to-pink-400 text-white shadow-lg shadow-rose-200 dark:shadow-none"
                                        : "text-gray-600 dark:text-gray-400 hover:bg-rose-50 dark:hover:bg-slate-800 hover:text-rose-500 dark:hover:text-rose-400"
                                )}
                            >
                                <Icon size={20} />
                                <span className="font-medium">{item.label}</span>
                            </button>
                        );
                    })}
                </nav>

                <div className="pt-6 border-t border-rose-100 dark:border-slate-800">
                    <div className="flex items-center gap-3 p-3 rounded-2xl hover:bg-rose-50 dark:hover:bg-slate-800 transition-colors cursor-pointer group">
                        <div className="w-10 h-10 bg-gradient-to-br from-amber-300 to-rose-300 dark:from-slate-700 dark:to-slate-600 rounded-xl flex items-center justify-center text-white font-semibold">
                            {user?.firstName?.charAt(0) || "U"}
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="font-medium text-gray-800 dark:text-gray-200 truncate group-hover:text-rose-500 dark:group-hover:text-rose-400 transition-colors">{user?.firstName}</p>
                            <p className="text-sm text-gray-500 dark:text-gray-500 truncate">{user?.email}</p>
                        </div>
                        <button onClick={logout} className="p-2 text-gray-400 hover:text-rose-500 transition-colors">
                            <LogOut size={18} />
                        </button>
                    </div>
                </div>
            </div>

            <div className="flex-1 ml-72 p-8 overflow-auto text-gray-800 dark:text-gray-200">
                <Outlet />
            </div>
        </div>
    );
}