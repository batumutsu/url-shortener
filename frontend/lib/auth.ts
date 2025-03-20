import type { AuthRequest, AuthResponse, RegistrationRequest } from "./types";
import { API_URL } from "./config";

export async function registerUser(
  data: RegistrationRequest
): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/auth/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(
      errorData.message || "Registration failed. Please try again."
    );
  }

  const authResponse = await response.json();

  // Dispatch custom event to notify about auth change
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("authChange"));
  }

  return authResponse;
}

export async function loginUser(data: AuthRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(
      errorData.message || "Login failed. Please check your credentials."
    );
  }

  const authResponse = await response.json();

  // Dispatch custom event to notify about auth change
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("authChange"));
  }

  return authResponse;
}

export async function refreshToken(token: string): Promise<AuthResponse> {
  const response = await fetch(
    `${API_URL}/auth/refresh?refreshToken=${token}`,
    {
      method: "POST",
    }
  );

  if (!response.ok) {
    // If refresh fails, clear the auth data
    if (typeof window !== "undefined") {
      localStorage.removeItem("auth");
      window.dispatchEvent(new Event("authChange"));
    }
    throw new Error("Failed to refresh token");
  }

  const authResponse = await response.json();

  // Dispatch custom event to notify about auth change
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("authChange"));
  }

  return authResponse;
}

export async function logoutUser(token: string): Promise<void> {
  const response = await fetch(`${API_URL}/auth/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to logout");
  }
}

export async function checkAuth(): Promise<boolean> {
  try {
    if (typeof window === "undefined") return false;

    const authData = localStorage.getItem("auth");
    if (!authData) return false;

    const { accessToken, refreshToken: refreshTokenValue } = JSON.parse(
      authData
    ) as AuthResponse;

    // Try to use the current token
    try {
      const response = await fetch(`${API_URL}/urls`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.ok) return true;

      // If token is expired, try to refresh
      if (response.status === 401) {
        try {
          const newAuthData = await refreshToken(refreshTokenValue);
          localStorage.setItem("auth", JSON.stringify(newAuthData));
          window.dispatchEvent(new Event("authChange"));
          return true;
        } catch (refreshError) {
          // If refresh fails, clear auth data
          localStorage.removeItem("auth");
          window.dispatchEvent(new Event("authChange"));
          return false;
        }
      }

      return false;
    } catch (error) {
      return false;
    }
  } catch (error) {
    return false;
  }
}

export function getAuthToken(): string | null {
  try {
    const authData = localStorage.getItem("auth");
    if (!authData) return null;

    const { accessToken } = JSON.parse(authData) as AuthResponse;
    return accessToken;
  } catch (error) {
    return null;
  }
}

export function getUsername(): string | null {
  try {
    const authData = localStorage.getItem("auth");
    if (!authData) return null;

    const { username } = JSON.parse(authData) as AuthResponse;
    return username;
  } catch (error) {
    return null;
  }
}
