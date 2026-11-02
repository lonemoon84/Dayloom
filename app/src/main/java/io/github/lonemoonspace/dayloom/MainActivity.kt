package io.github.lonemoonspace.dayloom

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.lonemoonspace.dayloom.app.nav.AppRoot
import io.github.lonemoonspace.dayloom.core.ui.theme.DayloomTheme

class MainActivity : ComponentActivity() {

    // Deep links from onCreate and onNewIntent take the same path into Compose.
    // onCreate 与 onNewIntent 带来的深链走同一条路径交给 Compose。
    private var pendingIntent by mutableStateOf<Intent?>(null)

    // Set once the settings, and so the theme preset, have been read. / 设置（以及其中的主题预设）读到之后置位。
    @Volatile private var themeKnown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate: it swaps the splash theme for the real one. / 必须在 super.onCreate 之前：它把启动页主题换回正式主题。
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // The splash stays until the theme preset is known, so the first frame is not drawn in wallpaper colours and then
        // repainted. Capped, so a settings file that keeps failing still reaches StartupWait's message.
        // 启动页一直显示到主题预设读出来，第一帧就不会先用壁纸配色画、再整页换色。设了上限，设置文件一直读不出来时仍能显示
        // StartupWait 的提示。
        val start = SystemClock.uptimeMillis()
        splash.setKeepOnScreenCondition { !themeKnown && SystemClock.uptimeMillis() - start < SPLASH_MAX_MS }
        // After recreation the back stack is restored; replaying the launch intent would push the destination twice.
        // 重建后返回栈已恢复；再重放启动 Intent 会把目的地重复入栈。
        if (savedInstanceState == null) pendingIntent = intent
        enableEdgeToEdge()
        val graph = DayloomApp.from(this).graph
        setContent {
            val settings by graph.appSettings.flow.collectAsStateWithLifecycle(initialValue = null)
            SideEffect { if (settings != null) themeKnown = true }
            DayloomTheme(preset = settings?.themePreset.orEmpty()) {
                AppRoot(graph = graph, pendingIntent = pendingIntent)
            }
        }
    }

    // singleTop (see the manifest) brings notification taps here instead of stacking a new activity.
    // singleTop（见清单）让通知点击走到这里，而不是再叠一个 Activity。
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntent = intent
    }
}

/** Longest the splash waits for the settings; a normal read takes a few milliseconds. / 启动页等设置的最长时间；正常读取只要几毫秒。 */
private const val SPLASH_MAX_MS = 1_000L
