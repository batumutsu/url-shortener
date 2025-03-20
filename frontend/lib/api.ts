import type { AnalyticsResponse, UrlResponse } from "./types";
import { API_URL } from "./config";
import { getAuthToken } from "./auth";

export async function getUserUrls(): Promise<UrlResponse[]> {
  const token = getAuthToken();
  if (!token) {
    throw new Error("Authentication required");
  }

  const response = await fetch(`${API_URL}/urls`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("Authentication expired. Please login again.");
    }
    throw new Error("Failed to fetch URLs");
  }

  return response.json();
}

export async function createShortUrl(longUrl: string): Promise<UrlResponse> {
  const token = getAuthToken();
  if (!token) {
    throw new Error("Authentication required");
  }

  const response = await fetch(`${API_URL}/urls/shorten`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ longUrl }),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("Authentication expired. Please login again.");
    }
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to create shortened URL");
  }

  return response.json();
}

export async function getUrlDetails(shortCode: string): Promise<UrlResponse> {
  const token = getAuthToken();
  if (!token) {
    throw new Error("Authentication required");
  }

  const response = await fetch(`${API_URL}/urls/${shortCode}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("URL not found");
    }
    if (response.status === 401) {
      throw new Error("Authentication expired. Please login again.");
    }
    throw new Error("Failed to fetch URL details");
  }

  return response.json();
}

export async function getUrlAnalytics(
  shortCode: string
): Promise<AnalyticsResponse> {
  const token = getAuthToken();
  if (!token) {
    throw new Error("Authentication required");
  }

  const response = await fetch(`${API_URL}/urls/analytics/${shortCode}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("URL not found");
    }
    if (response.status === 401) {
      throw new Error("Authentication expired. Please login again.");
    }
    throw new Error("Failed to fetch analytics data");
  }

  return response.json();
}

export async function deleteUrl(shortCode: string): Promise<void> {
  const token = getAuthToken();
  if (!token) {
    throw new Error("Authentication required");
  }

  const response = await fetch(`${API_URL}/urls/${shortCode}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("URL not found");
    }
    if (response.status === 401) {
      throw new Error("Authentication expired. Please login again.");
    }
    if (response.status === 403) {
      throw new Error("You don't have permission to delete this URL");
    }
    throw new Error("Failed to delete URL");
  }
}
