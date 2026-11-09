package io.github.lonemoonspace.dayloom.feature.news.data

import io.github.lonemoonspace.dayloom.core.error.AppError
import io.github.lonemoonspace.dayloom.core.network.executeOrAppError
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedFetcher
import io.github.lonemoonspace.dayloom.feature.news.domain.FetchResult
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer

/**
 * Plain HTTP GET of a feed with If-None-Match / If-Modified-Since, so an unchanged feed costs a 304 and no parsing. The
 * body is capped at [MAX_BYTES]: a feed is a few hundred kilobytes, and a page that never ends must not fill the memory.
 * 带 If-None-Match / If-Modified-Since 的普通 HTTP GET，订阅源没变时只花一个 304，不用解析。正文上限 [MAX_BYTES]：订阅源一般几百 KB，
 * 一个没完没了的页面不能把内存占满。
 */
class FeedApi(private val http: OkHttpClient, private val io: CoroutineContext = Dispatchers.IO) : FeedFetcher {

    override suspend fun fetch(url: String, etag: String, lastModified: String): FetchResult = withContext(io) {
        val target = url.toHttpUrlOrNull() ?: throw AppError.BadData(SERVICE, "invalid feed address")
        val request = Request.Builder().url(target).header("Accept", ACCEPT).apply {
            if (etag.isNotBlank()) header("If-None-Match", etag)
            if (lastModified.isNotBlank()) header("If-Modified-Since", lastModified)
        }.build()
        http.newCall(request).executeOrAppError().use { response ->
            if (response.code == HTTP_NOT_MODIFIED) return@withContext FetchResult.NotModified
            if (!response.isSuccessful) throw AppError.Http(SERVICE, response.code)
            val buffer = Buffer()
            try {
                val source = response.body.source()
                while (buffer.size <= MAX_BYTES) {
                    if (source.read(buffer, CHUNK) == -1L) break
                }
            } catch (e: IOException) {
                throw AppError.Network(e)
            }
            if (buffer.size > MAX_BYTES) return@withContext FetchResult.TooLarge
            FetchResult.Fetched(
                bytes = buffer.readByteArray(),
                etag = response.header("ETag").orEmpty(),
                lastModified = response.header("Last-Modified").orEmpty(),
                finalUrl = response.request.url.toString(),
            )
        }
    }

    companion object {
        const val SERVICE = "RSS"
        const val MAX_BYTES = 4L * 1024 * 1024
        private const val CHUNK = 64L * 1024
        private const val HTTP_NOT_MODIFIED = 304
        private const val ACCEPT = "application/rss+xml, application/atom+xml, application/rdf+xml, application/xml;q=0.9, text/xml;q=0.9, */*;q=0.5"
    }
}
