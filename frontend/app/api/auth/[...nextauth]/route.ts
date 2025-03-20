import { type NextRequest, NextResponse } from "next/server";
import { API_URL } from "@/lib/config";
import type { AuthResponse } from "@/lib/types";

// This is a server-side API route that handles setting HttpOnly cookies
// which are more secure than client-side cookies

export async function POST(request: NextRequest) {
  try {
    const { action, data } = await request.json();

    if (action === "login" || action === "register") {
      // Forward the request to the backend
      const endpoint = action === "login" ? "login" : "register";
      const response = await fetch(`${API_URL}/auth/${endpoint}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        return NextResponse.json(
          { error: errorData.message || `${action} failed` },
          { status: response.status }
        );
      }

      const authData: AuthResponse = await response.json();

      // Create the response
      const res = NextResponse.json({
        success: true,
        username: authData.username,
      });

      // Set HttpOnly cookies that can't be accessed by JavaScript
      res.cookies.set("auth_token", authData.accessToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 60 * 60, // 1 hour
        path: "/",
      });

      res.cookies.set("refresh_token", authData.refreshToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      // Set a non-HttpOnly cookie for the username so client can access it
      res.cookies.set("username", authData.username, {
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      return res;
    }

    if (action === "logout") {
      const token = request.cookies.get("auth_token")?.value;

      if (token) {
        try {
          await fetch(`${API_URL}/auth/logout`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
        } catch (error) {
          console.error("Error during logout:", error);
        }
      }

      const res = NextResponse.json({ success: true });

      // Clear all cookies
      res.cookies.delete("auth_token");
      res.cookies.delete("refresh_token");
      res.cookies.delete("username");

      return res;
    }

    if (action === "refresh") {
      const refreshToken = request.cookies.get("refresh_token")?.value;

      if (!refreshToken) {
        return NextResponse.json(
          { error: "No refresh token" },
          { status: 401 }
        );
      }

      const response = await fetch(
        `${API_URL}/auth/refresh?refreshToken=${refreshToken}`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        const res = NextResponse.json(
          { error: "Failed to refresh token" },
          { status: 401 }
        );
        res.cookies.delete("auth_token");
        res.cookies.delete("refresh_token");
        res.cookies.delete("username");
        return res;
      }

      const authData: AuthResponse = await response.json();

      const res = NextResponse.json({
        success: true,
        username: authData.username,
      });

      res.cookies.set("auth_token", authData.accessToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 60 * 60, // 1 hour
        path: "/",
      });

      res.cookies.set("refresh_token", authData.refreshToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      // Set a non-HttpOnly cookie for the username so client can access it
      res.cookies.set("username", authData.username, {
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      return res;
    }

    return NextResponse.json({ error: "Invalid action" }, { status: 400 });
  } catch (error) {
    console.error("Auth API route error:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

// Update the GET handler to check for 429 responses
export async function GET(request: NextRequest) {
  // Check if user is authenticated
  const token = request.cookies.get("auth_token")?.value;

  if (!token) {
    return NextResponse.json({ authenticated: false });
  }

  try {
    // Validate token with a simple request to the backend
    const response = await fetch(`${API_URL}/urls`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.ok) {
      return NextResponse.json({
        authenticated: true,
        username: request.cookies.get("username")?.value,
      });
    }

    // Handle rate limiting
    if (response.status === 429) {
      const res = NextResponse.json({
        authenticated: false,
        error:
          "Too many requests. You have been logged out for security reasons.",
      });

      // Clear all cookies
      res.cookies.delete("auth_token");
      res.cookies.delete("refresh_token");
      res.cookies.delete("username");

      return res;
    }

    // If token is expired, try to refresh
    if (response.status === 401) {
      const refreshToken = request.cookies.get("refresh_token")?.value;

      if (!refreshToken) {
        return NextResponse.json({ authenticated: false });
      }

      const refreshResponse = await fetch(
        `${API_URL}/auth/refresh?refreshToken=${refreshToken}`,
        {
          method: "POST",
        }
      );

      // Also check for rate limiting in refresh token request
      if (refreshResponse.status === 429) {
        const res = NextResponse.json({
          authenticated: false,
          error:
            "Too many requests. You have been logged out for security reasons.",
        });

        // Clear all cookies
        res.cookies.delete("auth_token");
        res.cookies.delete("refresh_token");
        res.cookies.delete("username");

        return res;
      }

      if (!refreshResponse.ok) {
        const res = NextResponse.json({ authenticated: false });
        res.cookies.delete("auth_token");
        res.cookies.delete("refresh_token");
        res.cookies.delete("username");
        return res;
      }

      const authData: AuthResponse = await refreshResponse.json();

      const res = NextResponse.json({
        authenticated: true,
        username: authData.username,
      });

      res.cookies.set("auth_token", authData.accessToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 60 * 60, // 1 hour
        path: "/",
      });

      res.cookies.set("refresh_token", authData.refreshToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      res.cookies.set("username", authData.username, {
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
        path: "/",
      });

      return res;
    }

    return NextResponse.json({ authenticated: false });
  } catch (error) {
    console.error("Auth check error:", error);
    return NextResponse.json({ authenticated: false });
  }
}
