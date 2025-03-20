import type { AuthRequest, AuthResponse, RegistrationRequest } from "./types";
import { API_URL } from "./config";
import {
  setAuthCookies,
  removeAuthCookies,
  getAuthToken,
  getRefreshToken,
} from "./cookies";

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

  // Set cookies instead of localStorage
  setAuthCookies(authResponse);

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

  // Set cookies instead of localStorage
  setAuthCookies(authResponse);

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
    // If refresh fails, clear the auth cookies
    removeAuthCookies();

    if (typeof window !== "undefined") {
      window.dispatchEvent(new Event("authChange"));
    }
    throw new Error("Failed to refresh token");
  }

  const authResponse = await response.json();

  // Set cookies with new tokens
  setAuthCookies(authResponse);

  // Dispatch custom event to notify about auth change
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("authChange"));
  }

  return authResponse;
}

export async function logoutUser(token: string): Promise<void> {
  try {
    const response = await fetch(`${API_URL}/auth/logout`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      console.error("Logout API call failed");
    }
  } catch (error) {
    console.error("Error during logout:", error);
  } finally {
    // Always remove cookies regardless of API response
    removeAuthCookies();

    if (typeof window !== "undefined") {
      window.dispatchEvent(new Event("authChange"));
    }
  }
}

export async function checkAuth(): Promise<boolean> {
  try {
    if (typeof window === "undefined") return false;

    // Check if auth token exists in cookies
    const accessToken = getAuthToken();
    if (!accessToken) return false;

    // Try to use the current token
    try {
      const response = await fetch(`${API_URL}/urls`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (response.ok) return true;

      // Handle rate limiting
      if (response.status === 429) {
        // If rate limited, log the user out
        await logoutUser(accessToken);
        if (typeof window !== "undefined") {
          window.dispatchEvent(new Event("authChange"));
        }
        return false;
      }

      // If token is expired, try to refresh
      if (response.status === 401) {
        const refreshTokenValue = getRefreshToken();
        if (!refreshTokenValue) return false;

        try {
          const refreshResponse = await fetch(
            `${API_URL}/auth/refresh?refreshToken=${refreshTokenValue}`,
            {
              method: "POST",
            }
          );

          // Also check for rate limiting in refresh token request
          if (refreshResponse.status === 429) {
            removeAuthCookies();
            if (typeof window !== "undefined") {
              window.dispatchEvent(new Event("authChange"));
            }
            return false;
          }

          if (!refreshResponse.ok) {
            removeAuthCookies();
            if (typeof window !== "undefined") {
              window.dispatchEvent(new Event("authChange"));
            }
            return false;
          }

          const newAuthData = await refreshResponse.json();
          setAuthCookies(newAuthData);
          return true;
        } catch (refreshError) {
          // If refresh fails, clear auth cookies
          removeAuthCookies();
          if (typeof window !== "undefined") {
            window.dispatchEvent(new Event("authChange"));
          }
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

// Replace getAuthToken with the one from cookie.ts
export { getAuthToken, isAuthenticated } from "./cookies";
