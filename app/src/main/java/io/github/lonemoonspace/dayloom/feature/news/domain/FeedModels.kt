package io.github.lonemoonspace.dayloom.feature.news.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Where the news tab gets its articles: feeds read straight from the phone, or the user's own Miniflux server.
 * 新闻标签页的文章来源：手机直接读取的订阅源，或用户自己的 Miniflux 服务器。
 */
@Serializable
enum class NewsSourceKind {
    @SerialName("rss")
    RSS,

    @SerialName("miniflux")
    MINIFLUX,
}

/** A feed the user follows; [title] is what the feed called itself when it was added. / 用户订阅的一个源；[title] 是添加时订阅源给自己起的名字。 */
@Serializable
data class Feed(val url: String = "", val title: String = "")

/** Why a feed could not be read on the last sync. / 上一次同步读不到某个订阅源的原因。 */
@Serializable
enum class FeedError {
    @SerialName("network")
    NETWORK,

    /** The server answered with an error status; see [FeedState.code]. / 服务器返回了错误状态码，见 [FeedState.code]。 */
    @SerialName("http")
    HTTP,

    /** The address answered, but not with RSS, Atom or RDF. / 地址有应答，但不是 RSS、Atom 或 RDF。 */
    @SerialName("not_feed")
    NOT_A_FEED,

    /** The body went past [io.github.lonemoonspace.dayloom.feature.news.data.FeedApi.MAX_BYTES]. / 正文超过了上限。 */
    @SerialName("too_large")
    TOO_LARGE,
}

/**
 * What the last sync learned about one feed: the validators for a conditional request, when it last succeeded, and the
 * error of the last attempt, if any. Kept in the snapshot, not in settings, because it is state, not a choice.
 * 上一次同步得知的某个订阅源的情况：条件请求要用的校验值、上次成功的时刻，以及最近一次失败的原因（如有）。放在快照里而不是设置里，
 * 因为它是状态，不是用户的选择。
 */
@Serializable
data class FeedState(
    val url: String = "",
    val etag: String = "",
    val lastModified: String = "",
    /** Last successful read, epoch millis; 0 = never. / 上次成功读取的时刻（epoch 毫秒）；0 表示从未成功。 */
    val okAt: Long = 0,
    val error: FeedError? = null,
    /** The HTTP status for [FeedError.HTTP]. / [FeedError.HTTP] 时的 HTTP 状态码。 */
    val code: Int = 0,
)

/** One parsed feed. / 解析后的一个订阅源。 */
data class ParsedFeed(val title: String, val items: List<FeedItem>)

/**
 * One item of a feed. [key] identifies it within the feed (guid, id, else link); [publishedAt] is 0 when the feed gave no
 * readable date.
 * 订阅源里的一条。[key] 在这个源里唯一标识它（guid、id，没有就用链接）；订阅源没有可读的日期时 [publishedAt] 为 0。
 */
data class FeedItem(
    val key: String,
    val title: String,
    val link: String,
    val author: String,
    val html: String,
    val publishedAt: Long,
)

/**
 * A suggested feed. Only the address and name are kept here; nothing is subscribed until the user ticks it.
 * 一个推荐源。这里只记地址和名字；用户勾选之前什么都不订阅。
 */
data class StarterFeed(val url: String, val title: String, val language: StarterLanguage)

enum class StarterLanguage { CHINESE, NORWEGIAN, ENGLISH }

/**
 * Feeds offered on first use, grouped by language. Each publishes its RSS for readers; the app shows only what the feed
 * itself carries and links back to the article.
 * 首次使用时推荐的订阅源，按语言分组。它们都公开提供 RSS 给阅读器使用；App 只显示订阅源本身给出的内容，并链接回原文。
 */
object StarterFeeds {
    val all: List<StarterFeed> = listOf(
        StarterFeed("https://feeds.bbci.co.uk/zhongwen/simp/rss.xml", "BBC News 中文", StarterLanguage.CHINESE),
        StarterFeed("https://rss.dw.com/rdf/rss-chi-all", "DW 中文", StarterLanguage.CHINESE),
        StarterFeed("https://www.rfi.fr/cn/rss", "RFI 中文", StarterLanguage.CHINESE),
        StarterFeed("https://www.nrk.no/toppsaker.rss", "NRK Nyheter", StarterLanguage.NORWEGIAN),
        StarterFeed("https://www.nrk.no/sport/toppsaker.rss", "NRK Sport", StarterLanguage.NORWEGIAN),
        StarterFeed("https://www.theguardian.com/world/rss", "The Guardian · World", StarterLanguage.ENGLISH),
        StarterFeed("https://feeds.bbci.co.uk/news/world/rss.xml", "BBC News · World", StarterLanguage.ENGLISH),
        StarterFeed("https://feeds.npr.org/1001/rss.xml", "NPR News", StarterLanguage.ENGLISH),
    )

    /**
     * The groups, the one matching the app [language] (an ISO 639 code) first, then the rest in a fixed order.
     * 各个分组：与 App [language]（ISO 639 代码）相同的那组排最前，其余按固定顺序。
     */
    fun grouped(language: String): List<Pair<StarterLanguage, List<StarterFeed>>> {
        val first = when (language.lowercase()) {
            "zh" -> StarterLanguage.CHINESE
            "nb", "nn", "no" -> StarterLanguage.NORWEGIAN
            else -> StarterLanguage.ENGLISH
        }
        val order = listOf(first) + StarterLanguage.entries.filter { it != first }
        return order.map { lang -> lang to all.filter { it.language == lang } }
    }
}

/** What one conditional request for a feed returned. / 对订阅源的一次条件请求的结果。 */
sealed interface FetchResult {
    /** 304: the validators still hold. / 304：校验值仍然有效。 */
    data object NotModified : FetchResult

    /** The body was larger than a feed should ever be; it was not read to the end. / 正文比订阅源应有的大小大得多，没有读完。 */
    data object TooLarge : FetchResult

    /**
     * The body as bytes, so the parser can honour the encoding the document declares; [finalUrl] is where redirects ended,
     * which relative feed links on a page resolve against.
     * 以字节返回正文，解析器才能按文档声明的编码读取；[finalUrl] 是重定向的终点，网页上的相对订阅链接以它为基准解析。
     */
    class Fetched(val bytes: ByteArray, val etag: String = "", val lastModified: String = "", val finalUrl: String = "") : FetchResult
}

/** Reads one feed address; the app uses HTTP, tests a map. / 读取一个订阅地址；App 用 HTTP，测试用一个 map。 */
fun interface FeedFetcher {
    suspend fun fetch(url: String, etag: String, lastModified: String): FetchResult
}
