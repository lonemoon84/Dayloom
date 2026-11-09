package io.github.lonemoonspace.dayloom.feature.news.data

import io.github.lonemoonspace.dayloom.core.error.AppError
import io.github.lonemoonspace.dayloom.feature.news.domain.ArticleOwner
import io.github.lonemoonspace.dayloom.feature.news.domain.ArticleStore
import io.github.lonemoonspace.dayloom.feature.news.domain.Feed
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedError
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedFetcher
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedParser
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedPolicy
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedState
import io.github.lonemoonspace.dayloom.feature.news.domain.FetchResult
import io.github.lonemoonspace.dayloom.feature.news.domain.ParsedFeed
import io.github.lonemoonspace.dayloom.feature.news.domain.SyncSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/**
 * Reads the followed feeds straight from the phone, a few at a time, and merges them into the stored articles. One broken
 * feed only marks itself; the sync fails only when every feed failed for lack of network. Network calls run outside
 * [lock], like [NewsSync], so a tap is never held up.
 * 在手机上直接读取订阅的源（同时几个），并合并进本地文章。某个源坏了只标记它自己；只有所有源都因为没网失败时，这次同步才算失败。
 * 与 [NewsSync] 一样，网络调用在 [lock] 之外进行，点按不会被卡住。
 */
class FeedSync(
    private val store: ArticleStore,
    private val fetcher: FeedFetcher,
    private val owner: ArticleOwner,
    private val lock: Mutex,
) {
    private class Outcome(val state: FeedState, val parsed: ParsedFeed? = null, val failure: AppError? = null)

    suspend fun sync(feeds: List<Feed>, previous: List<FeedState>, nowMillis: Long): SyncSummary {
        // Validators are only worth sending when the articles they vouch for are still here; after a switch of source or a
        // feed added again, a 304 would leave the list empty.
        // 只有校验值对应的文章还在本机时才值得发送；切换来源或重新添加某个源之后，一个 304 会让列表空着。
        val held = lock.withLock {
            if (owner.get() == FeedPolicy.OWNER) store.all().mapTo(HashSet()) { FeedPolicy.feedTagOf(it.id) } else emptySet()
        }
        val before = previous.associateBy { it.url }
        val gate = Semaphore(FeedPolicy.PARALLEL_FETCHES)
        val outcomes = coroutineScope {
            feeds.map { feed ->
                async { gate.withPermit { read(feed, before[feed.url]?.takeIf { FeedPolicy.feedTag(feed.url) in held }, nowMillis) } }
            }.awaitAll()
        }
        if (outcomes.isNotEmpty() && outcomes.all { it.state.error == FeedError.NETWORK }) {
            throw outcomes.firstNotNullOf { it.failure }
        }
        lock.withLock {
            if (owner.get() != FeedPolicy.OWNER) {
                // Another source's articles have different ids and no meaning here. / 另一个来源的文章 id 不同，在这里没有意义。
                store.clear()
                owner.set(FeedPolicy.OWNER)
            }
            val fetched = outcomes.mapNotNull { o -> o.parsed?.let { o.state.url to it } }.toMap()
            store.apply(FeedPolicy.merge(store.all(), fetched, feeds, nowMillis))
        }
        return lock.withLock { store.summary() }.copy(feeds = outcomes.map { it.state })
    }

    private suspend fun read(feed: Feed, previous: FeedState?, nowMillis: Long): Outcome {
        val kept = FeedState(url = feed.url, etag = previous?.etag.orEmpty(), lastModified = previous?.lastModified.orEmpty(), okAt = previous?.okAt ?: 0)
        return try {
            when (val result = fetcher.fetch(feed.url, kept.etag, kept.lastModified)) {
                FetchResult.NotModified -> Outcome(kept.copy(okAt = nowMillis))
                is FetchResult.Fetched -> {
                    val parsed = FeedParser.parse(result.bytes) ?: return Outcome(kept.copy(error = FeedError.NOT_A_FEED))
                    Outcome(FeedState(feed.url, result.etag, result.lastModified, okAt = nowMillis), parsed)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError.Http) {
            Outcome(kept.copy(error = FeedError.HTTP, code = e.code), failure = e)
        } catch (e: AppError.BadData) {
            Outcome(kept.copy(error = FeedError.NOT_A_FEED), failure = e)
        } catch (e: Exception) {
            Outcome(kept.copy(error = FeedError.NETWORK), failure = e as? AppError ?: AppError.Network(e))
        }
    }
}
