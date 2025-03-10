"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { UrlList } from "@/components/url-list"
import { CreateUrlForm } from "@/components/create-url-form"
import { checkAuth } from "@/lib/auth"

export default function DashboardPage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkAuthentication = async () => {
      const isAuthenticated = await checkAuth()
      if (!isAuthenticated) {
        router.push("/login")
      } else {
        setIsLoading(false)
      }
    }

    checkAuthentication()
  }, [router])

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">Loading...</div>
      </div>
    )
  }

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6">Dashboard</h1>

      <Tabs defaultValue="urls" className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="urls">My URLs</TabsTrigger>
          <TabsTrigger value="create">Create New URL</TabsTrigger>
        </TabsList>
        <TabsContent value="urls">
          <Card>
            <CardHeader>
              <CardTitle>My Shortened URLs</CardTitle>
            </CardHeader>
            <CardContent>
              <UrlList />
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="create">
          <Card>
            <CardHeader>
              <CardTitle>Create New Shortened URL</CardTitle>
            </CardHeader>
            <CardContent>
              <CreateUrlForm />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

