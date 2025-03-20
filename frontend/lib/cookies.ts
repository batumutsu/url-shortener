import Cookies from "js-cookie";
import type { AuthResponse } from "./types";

// Cookie options
const cookieOptions = {
  path: "/",
  secure: process.env.NODE_ENV === "production",
  sameSite: "strict" as const,
  // HttpOnly can only be set by the server, not by client-side JavaScript
};

// Cookie names
export const AUTH_COOKIE_NAME = "auth_token";
export const REFRESH_COOKIE_NAME = "refresh_token";

// Set auth cookies
export function setAuthCookies(authData: AuthResponse): void {
  Cookies.set(AUTH_COOKIE_NAME, authData.accessToken, {
    ...cookieOptions,
    expires: new Date(Date.now() + 1000 * 60 * 60), // 1 hour
  });

  Cookies.set(REFRESH_COOKIE_NAME, authData.refreshToken, {
    ...cookieOptions,
    expires: new Date(Date.now() + 1000 * 60 * 60 * 2), // 2 hours
  });

  // Store username in a regular cookie that can be read by client
  Cookies.set("username", authData.username, {
    ...cookieOptions,
    expires: new Date(Date.now() + 1000 * 60 * 60 * 2), // 2 hours
  });
}

// Remove auth cookies
export function removeAuthCookies(): void {
  Cookies.remove(AUTH_COOKIE_NAME, { path: "/" });
  Cookies.remove(REFRESH_COOKIE_NAME, { path: "/" });
  Cookies.remove("username", { path: "/" });
}

// Get auth token
export function getAuthToken(): string | null {
  return Cookies.get(AUTH_COOKIE_NAME) || null;
}

// Get refresh token
export function getRefreshToken(): string | null {
  return Cookies.get(REFRESH_COOKIE_NAME) || null;
}

// Check if user is authenticated
export function isAuthenticated(): boolean {
  return !!getAuthToken();
}

// Get username
export function getUsername(): string | null {
  return Cookies.get("username") || null;
}
