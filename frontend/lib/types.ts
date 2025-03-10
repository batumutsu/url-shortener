export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  username: string
}

export interface RegistrationRequest {
  username: string
  email: string
  password: string
}

export interface AuthRequest {
  username: string
  password: string
}

export interface UrlResponse {
  id: string
  shortCode: string
  shortUrl: string
  longUrl: string
  createdAt: string
  clicks: number
}

export interface AnalyticsResponse {
  urlId: string
  shortCode: string
  shortUrl: string
  longUrl: string
  totalClicks: number
  clicksByDay: Record<string, number>
  referrerCounts: Record<string, number>
  browserCounts: Record<string, number>
}

