"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { ModeToggle } from "@/components/mode-toggle";
import { getUsername, logoutUser } from "@/lib/auth";

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    // Prevent navigation from causing unintended logouts
    const handleBeforeUnload = () => {
      // This is just to ensure the auth state persists during navigation
      const auth = localStorage.getItem("auth");
      if (auth) {
        sessionStorage.setItem("preserveAuth", "true");
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, []);

  useEffect(() => {
    const checkAuth = () => {
      const auth = localStorage.getItem("auth");
      setIsLoggedIn(!!auth);
      setUsername(!!auth ? getUsername() : null);
    };

    // Check auth on initial load
    checkAuth();

    // Create a custom event for auth changes
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "auth") {
        checkAuth();
      }
    };

    // Listen for storage events from other tabs/windows
    window.addEventListener("storage", handleStorageChange);

    // Create a custom event for this tab
    const authChangeEvent = new Event("authChange");
    window.addEventListener("authChange", checkAuth);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
      window.removeEventListener("authChange", checkAuth);
    };
  }, []);

  const handleLogout = async () => {
    try {
      const auth = localStorage.getItem("auth");
      if (auth) {
        const { accessToken } = JSON.parse(auth);
        await logoutUser(accessToken);
      }
      localStorage.removeItem("auth");
      window.dispatchEvent(new Event("authChange"));
      router.push("/");
    } catch (error) {
      console.error("Logout failed:", error);
      localStorage.removeItem("auth");
      window.dispatchEvent(new Event("authChange"));
      router.push("/");
    }
  };

  // Don't show navbar on login and register pages
  if (pathname === "/login" || pathname === "/register") {
    return null;
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <div className="mr-4 flex">
          <Link href="/dashboard" className="flex items-center space-x-2">
            <span className="font-bold text-xl">URL Shortener</span>
          </Link>
        </div>
        <div className="flex flex-1 items-center justify-end space-x-2">
          <nav className="flex items-center space-x-2">
            {isLoggedIn ? (
              <>
                {username && (
                  <span className="text-sm text-muted-foreground mr-2">
                    Hello, {username}
                  </span>
                )}
                <Link href="/dashboard">
                  <Button variant="ghost">Dashboard</Button>
                </Link>
                <Button variant="ghost" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Link href="/login">
                  <Button variant="ghost">Login</Button>
                </Link>
                <Link href="/register">
                  <Button variant="ghost">Register</Button>
                </Link>
              </>
            )}
            <ModeToggle />
          </nav>
        </div>
      </div>
    </header>
  );
}
