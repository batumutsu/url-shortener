"use client"

import { useState } from "react"
import { z } from "zod"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Button } from "@/components/ui/button"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { createShortUrl } from "@/lib/api"

const formSchema = z.object({
  longUrl: z.string().url({ message: "Please enter a valid URL" }).min(1, { message: "URL is required" }),
})

export function CreateUrlForm() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [shortUrl, setShortUrl] = useState<string | null>(null)

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      longUrl: "",
    },
  })

  async function onSubmit(values: z.infer<typeof formSchema>) {
    setIsLoading(true)
    setError(null)
    setSuccess(null)
    setShortUrl(null)

    try {
      const response = await createShortUrl(values.longUrl)
      setSuccess("URL shortened successfully!")
      setShortUrl(response.shortUrl)
      form.reset()
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create shortened URL. Please try again.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleCopy = () => {
    if (shortUrl) {
      navigator.clipboard.writeText(shortUrl)
      setSuccess("Copied to clipboard!")
    }
  }

  return (
    <div>
      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {success && (
        <Alert className="mb-4">
          <AlertDescription>{success}</AlertDescription>
        </Alert>
      )}

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <FormField
            control={form.control}
            name="longUrl"
            render={({ field }) => (
              <FormItem>
                <FormLabel>URL to shorten</FormLabel>
                <FormControl>
                  <Input placeholder="https://example.com/very/long/url/that/needs/shortening" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button type="submit" disabled={isLoading}>
            {isLoading ? "Creating..." : "Create Short URL"}
          </Button>
        </form>
      </Form>

      {shortUrl && (
        <div className="mt-6 p-4 border rounded-md">
          <h3 className="font-medium mb-2">Your shortened URL:</h3>
          <div className="flex items-center gap-2">
            <Input value={shortUrl} readOnly />
            <Button onClick={handleCopy}>Copy</Button>
          </div>
        </div>
      )}
    </div>
  )
}

