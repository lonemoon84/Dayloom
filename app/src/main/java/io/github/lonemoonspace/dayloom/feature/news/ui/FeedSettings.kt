package io.github.lonemoonspace.dayloom.feature.news.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lonemoonspace.dayloom.R
import io.github.lonemoonspace.dayloom.core.error.AppError
import io.github.lonemoonspace.dayloom.core.i18n.asString
import io.github.lonemoonspace.dayloom.core.ui.currentLocale
import io.github.lonemoonspace.dayloom.core.ui.rememberMinuteTick
import io.github.lonemoonspace.dayloom.core.ui.theme.appSurfaces
import io.github.lonemoonspace.dayloom.core.ui.theme.statusColors
import io.github.lonemoonspace.dayloom.core.ui.userMessage
import io.github.lonemoonspace.dayloom.feature.news.NewsSettings
import io.github.lonemoonspace.dayloom.feature.news.domain.Feed
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedError
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedParser
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedPolicy
import io.github.lonemoonspace.dayloom.feature.news.domain.FeedState
import io.github.lonemoonspace.dayloom.feature.news.domain.NewsSourceKind
import io.github.lonemoonspace.dayloom.feature.news.domain.NewsSyncPolicy
import io.github.lonemoonspace.dayloom.feature.news.domain.StarterFeeds
import io.github.lonemoonspace.dayloom.feature.news.domain.StarterLanguage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What adding a typed address found. / 添加输入的地址的结果。 */
internal sealed interface AddFeedResult {
    data class Added(val title: String) : AddFeedResult
    data object Exists : AddFeedResult
    data object Full : AddFeedResult
    data object Invalid : AddFeedResult
    data object NotFound : AddFeedResult
    data class Failed(val error: AppError) : AddFeedResult
}

/**
 * Feeds or Miniflux. Switching keeps both setups, so switching back needs no typing; the stored articles follow the source
 * on its next sync.
 * 订阅源或 Miniflux。切换时两边的设置都保留，切回来不用重新填写；本地文章在下一次同步时跟着来源走。
 */
@Composable
internal fun SourcePicker(kind: NewsSourceKind, onPick: (NewsSourceKind) -> Unit) {
    Text(stringResource(R.string.news_source_title), style = MaterialTheme.typography.labelLarge)
    val options = listOf(NewsSourceKind.RSS to R.string.news_source_rss, NewsSourceKind.MINIFLUX to R.string.news_source_miniflux)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (option, label) ->
            SegmentedButton(
                selected = kind == option,
                onClick = { onPick(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label), maxLines = 1) }
        }
    }
}

/**
 * The followed feeds with how each last went, and three ways to add more: an address, the suggestions (shown by themselves
 * the first time), or an OPML file from another reader.
 * 订阅的源及其最近一次的情况，以及三种添加方式：输入地址、推荐源（第一次会自动弹出）、从其他阅读器导出的 OPML 文件。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeedsForm(
    saved: NewsSettings,
    states: List<FeedState>,
    addFeed: suspend (String) -> AddFeedResult,
    update: ((NewsSettings) -> NewsSettings) -> Unit,
) {
    var adding by rememberSaveable { mutableStateOf(false) }
    var suggesting by rememberSaveable { mutableStateOf(false) }
    var imported by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(saved.starterOffered, saved.feeds.isEmpty()) {
        if (!saved.starterOffered && saved.feeds.isEmpty()) {
            suggesting = true
            update { it.copy(starterOffered = true) }
        }
    }
    val openOpml = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { readCapped(it) }
                } catch (_: IOException) {
                    null
                } catch (_: SecurityException) {
                    null
                }
            }
            val found = bytes?.let { FeedParser.opml(it) }.orEmpty()
            val before = saved.feeds.size
            val after = FeedPolicy.add(saved.feeds, found).size
            update { it.copy(feeds = FeedPolicy.add(it.feeds, found)) }
            imported = after - before
        }
    }

    Hint(stringResource(R.string.news_source_rss_note))
    Text(
        pluralStringResource(R.plurals.news_feeds_count, saved.feeds.size, saved.feeds.size),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 8.dp),
    )
    val byUrl = states.associateBy { it.url }
    val now = rememberMinuteTick()
    saved.feeds.forEach { feed ->
        FeedRow(feed, byUrl[feed.url], now) { update { s -> s.copy(feeds = s.feeds.filter { it.url != feed.url }) } }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = { adding = true }, enabled = saved.feeds.size < FeedPolicy.MAX_FEEDS) { Text(stringResource(R.string.news_feed_add)) }
        FilledTonalButton(onClick = { suggesting = true }) { Text(stringResource(R.string.news_feed_starter)) }
        TextButton(onClick = { openOpml.launch(OPML_TYPES) }) { Text(stringResource(R.string.news_feed_import)) }
    }
    imported?.let { count ->
        Text(
            if (count > 0) pluralStringResource(R.plurals.news_opml_imported, count, count) else stringResource(R.string.news_opml_none),
            style = MaterialTheme.typography.bodySmall,
            color = if (count > 0) MaterialTheme.statusColors.green else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Hint(stringResource(R.string.news_feed_note))

    if (adding) AddFeedDialog(addFeed) { adding = false }
    if (suggesting) {
        StarterDialog(followed = saved.feeds.mapTo(HashSet()) { it.url }, onDismiss = { suggesting = false }) { picked ->
            update { it.copy(feeds = FeedPolicy.add(it.feeds, picked)) }
            suggesting = false
        }
    }
}

@Composable
private fun FeedRow(feed: Feed, state: FeedState?, now: java.time.ZonedDateTime, onRemove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Box(
            Modifier.size(28.dp).background(MaterialTheme.appSurfaces.tile, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                feed.title.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "·",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(feed.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val error = state?.error
            if (error != null) {
                Text(
                    stringResource(R.string.news_feed_unreadable, feedErrorText(error, state.code)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.statusColors.red,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                val updated = state?.okAt?.takeIf { it > 0 }?.let { stringResource(R.string.news_feed_updated, ago(it, now)) }
                    ?: stringResource(R.string.news_feed_never)
                Hint(FeedPolicy.hostOf(feed.url) + " · " + updated)
            }
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.news_feed_menu, feed.title))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.news_feed_remove)) }, onClick = {
                    menu = false
                    onRemove()
                })
            }
        }
    }
}

@Composable
private fun feedErrorText(error: FeedError, code: Int): String = when (error) {
    FeedError.NETWORK -> stringResource(R.string.news_feed_error_network)
    FeedError.HTTP -> stringResource(R.string.news_feed_error_http, code)
    FeedError.NOT_A_FEED -> stringResource(R.string.news_feed_error_not_feed)
}

/**
 * Takes a feed or web page address; the module checks it before adding, so a typo is caught here rather than as a broken
 * feed later.
 * 输入订阅源或网页地址；模块先检查再添加，打错的地址在这里就能发现，而不是之后变成一个坏掉的源。
 */
@Composable
private fun AddFeedDialog(addFeed: suspend (String) -> AddFeedResult, onDismiss: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AddFeedResult?>(null) }
    val scope = rememberCoroutineScope()
    val problem = when {
        input.isBlank() -> null
        NewsSyncPolicy.isPlainHttp(input) -> R.string.news_feed_url_http
        FeedPolicy.normalizeFeedUrl(input) == null -> R.string.news_feed_url_invalid
        else -> null
    }
    val submit = {
        if (!working && problem == null && input.isNotBlank()) {
            working = true
            result = null
            scope.launch {
                val r = addFeed(input)
                working = false
                result = r
                if (r is AddFeedResult.Added) onDismiss()
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.news_feed_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; result = null },
                    singleLine = true,
                    label = { Text(stringResource(R.string.news_feed_url)) },
                    isError = problem != null,
                    supportingText = problem?.let { { Text(stringResource(it)) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (working) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Hint(stringResource(R.string.news_feed_checking))
                    }
                }
                addFeedMessage(result)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }, enabled = !working && problem == null && input.isNotBlank()) {
                Text(stringResource(R.string.news_feed_add_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

@Composable
private fun addFeedMessage(result: AddFeedResult?): String? = when (result) {
    null, is AddFeedResult.Added -> null
    AddFeedResult.Exists -> stringResource(R.string.news_feed_exists)
    AddFeedResult.Full -> stringResource(R.string.news_feed_full, FeedPolicy.MAX_FEEDS)
    AddFeedResult.Invalid -> stringResource(R.string.news_feed_url_invalid)
    AddFeedResult.NotFound -> stringResource(R.string.news_feed_not_found)
    is AddFeedResult.Failed -> result.error.userMessage().asString()
}

/**
 * The suggested feeds by language, the app's own first. Nothing is ticked to begin with; feeds already followed show as
 * ticked and cannot be unticked here.
 * 按语言分组的推荐源，App 当前语言的那组在最前。一开始都不勾选；已经订阅的显示为已勾选，在这里不能取消。
 */
@Composable
private fun StarterDialog(followed: Set<String>, onDismiss: () -> Unit, onAdd: (List<Feed>) -> Unit) {
    val picked = remember { mutableStateListOf<String>() }
    val groups = StarterFeeds.grouped(currentLocale().language)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.news_starter_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Hint(stringResource(R.string.news_starter_note))
                groups.forEach { (language, feeds) ->
                    Text(
                        stringResource(languageName(language)),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    )
                    feeds.forEach { starter ->
                        val already = starter.url in followed
                        val checked = already || starter.url in picked
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !already) { if (checked) picked.remove(starter.url) else picked.add(starter.url) },
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null, enabled = !already, modifier = Modifier.padding(12.dp))
                            Column {
                                Text(starter.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Hint(FeedPolicy.hostOf(starter.url))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(StarterFeeds.all.filter { it.url in picked }.map { Feed(it.url, it.title) }) },
                enabled = picked.isNotEmpty(),
            ) { Text(pluralStringResource(R.plurals.news_starter_add, picked.size, picked.size)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

private fun languageName(language: StarterLanguage) = when (language) {
    StarterLanguage.CHINESE -> R.string.news_starter_chinese
    StarterLanguage.NORWEGIAN -> R.string.news_starter_norwegian
    StarterLanguage.ENGLISH -> R.string.news_starter_english
}

/** The stream's bytes, or null when it is larger than [OPML_MAX_BYTES]. / 流的全部字节；超过 [OPML_MAX_BYTES] 时为 null。 */
private fun readCapped(input: InputStream): ByteArray? {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    while (true) {
        val n = input.read(buffer)
        if (n < 0) return out.toByteArray()
        if (out.size() + n > OPML_MAX_BYTES) return null
        out.write(buffer, 0, n)
    }
}

/** OPML files come as any of these, often as plain XML or even untyped. / OPML 文件的类型五花八门，常常只是普通 XML，甚至没有类型。 */
private val OPML_TYPES = arrayOf("text/x-opml", "application/xml", "text/xml", "application/octet-stream", "*/*")

/** An OPML file is a few kilobytes; anything far bigger is not one. / OPML 文件一般几 KB，大得多的就不是。 */
private const val OPML_MAX_BYTES = 1024 * 1024
