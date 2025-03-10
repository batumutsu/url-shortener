"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { ArrowLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { AnalyticsChart } from "@/components/analytics-chart"
import { AnalyticsPieChart } from "@/components/analytics-pie-chart"
import { getUrlAnalytics } from "@/lib/api"
import type { AnalyticsResponse } from "@/lib/types"
import { checkAuth } from "@/lib/auth"

export default function AnalyticsPage({
  params,
}: {
  params: { shortCode: string }
}) {
  const router = useRouter()
  const [analytics, setAnalytics] = useState<AnalyticsResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isAuthenticated = await checkAuth()
        if (!isAuthenticated) {
          router.push("/login")
          return
        }

        const data = await getUrlAnalytics(params.shortCode)
        setAnalytics(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load analytics data. Please try again.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchData()
  }, [params.shortCode, router])

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">Loading analytics data...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container mx-auto py-10">
        <Button variant="outline" onClick={() => router.back()} className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" /> Back
        </Button>
        <div className="text-center">
          <p className="text-red-500">{error}</p>
        </div>
      </div>
    )
  }

  if (!analytics) {
    return (
      <div className="container mx-auto py-10">
        <Button variant="outline" onClick={() => router.back()} className="mb-4">
          <ArrowLeft className="mr-2 h-4 w-4" /> Back
        </Button>
        <div className="text-center">
          <p>No analytics data available for this URL.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-10">
      <Button variant="outline" onClick={() => router.back()} className="mb-4">
        <ArrowLeft className="mr-2 h-4 w-4" /> Back
      </Button>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Clicks</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{analytics.totalClicks}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Short URL</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-medium truncate">{analytics.shortUrl}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Original URL</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-medium truncate">{analytics.longUrl}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Short Code</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{analytics.shortCode}</div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="daily" className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="daily">Daily Clicks</TabsTrigger>
          <TabsTrigger value="referrers">Referrers</TabsTrigger>
          <TabsTrigger value="browsers">Browsers</TabsTrigger>
        </TabsList>
        <TabsContent value="daily">
          <Card>
            <CardHeader>
              <CardTitle>Daily Clicks</CardTitle>
              <CardDescription>Number of clicks per day for this URL</CardDescription>
            </CardHeader>
            <CardContent className="h-[400px]">
              <AnalyticsChart data={analytics.clicksByDay} />
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="referrers">
          <Card>
            <CardHeader>
              <CardTitle>Referrers</CardTitle>
              <CardDescription>Where your traffic is coming from</CardDescription>
            </CardHeader>
            <CardContent className="h-[400px]">
              <AnalyticsPieChart data={analytics.referrerCounts} />
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="browsers">
          <Card>
            <CardHeader>
              <CardTitle>Browsers</CardTitle>
              <CardDescription>Browsers used to access your URL</CardDescription>
            </CardHeader>
            <CardContent className="h-[400px]">
              <AnalyticsPieChart data={analytics.browserCounts} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

