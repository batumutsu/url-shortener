"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Copy, ExternalLink, BarChart2, Trash2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { getUserUrls, deleteUrl } from "@/lib/api"
import type { UrlResponse } from "@/lib/types"
import { formatDate } from "@/lib/utils"

export function UrlList() {
  const router = useRouter()
  const [urls, setUrls] = useState<UrlResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchUrls = async () => {
      try {
        const data = await getUserUrls()
        setUrls(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load URLs. Please try again.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchUrls()
  }, [])

  const handleCopy = (url: string) => {
    navigator.clipboard.writeText(url)
  }

  const handleDelete = async (shortCode: string) => {
    try {
      await deleteUrl(shortCode)
      setUrls(urls.filter((url) => url.shortCode !== shortCode))
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete URL. Please try again.")
    }
  }

  if (isLoading) {
    return <div className="text-center py-4">Loading your URLs...</div>
  }

  if (error) {
    return <div className="text-center py-4 text-red-500">{error}</div>
  }

  if (urls.length === 0) {
    return <div className="text-center py-4">You haven&apos;t created any shortened URLs yet.</div>
  }

  return (
    <div className="overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Short URL</TableHead>
            <TableHead>Original URL</TableHead>
            <TableHead>Created</TableHead>
            <TableHead>Clicks</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {urls.map((url) => (
            <TableRow key={url.id}>
              <TableCell className="font-medium">
                <div className="flex items-center space-x-2">
                  <span className="truncate max-w-[150px]">{url.shortUrl}</span>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleCopy(url.shortUrl)}
                    title="Copy to clipboard"
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon" asChild title="Open in new tab">
                    <a href={url.shortUrl} target="_blank" rel="noopener noreferrer">
                      <ExternalLink className="h-4 w-4" />
                    </a>
                  </Button>
                </div>
              </TableCell>
              <TableCell className="truncate max-w-[200px]">{url.longUrl}</TableCell>
              <TableCell>{formatDate(url.createdAt)}</TableCell>
              <TableCell>{url.clicks}</TableCell>
              <TableCell>
                <div className="flex space-x-1">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => router.push(`/dashboard/analytics/${url.shortCode}`)}
                    title="View analytics"
                  >
                    <BarChart2 className="h-4 w-4" />
                  </Button>
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="ghost" size="icon" title="Delete URL">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Are you sure?</AlertDialogTitle>
                        <AlertDialogDescription>
                          This will permanently delete this shortened URL. This action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={() => handleDelete(url.shortCode)}>Delete</AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

