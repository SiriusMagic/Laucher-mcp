package com.zirakusa.launcher

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.SharedValues
import com.zirakusa.launcher.mcp.McpMode
import com.zirakusa.launcher.ui.AppActions
import com.zirakusa.launcher.ui.HomeState
import com.zirakusa.launcher.ui.LauncherApp
import com.zirakusa.launcher.ui.Page
import com.zirakusa.launcher.ui.theme.HomeColors
import com.zirakusa.launcher.ui.theme.HomeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The home app: reads the phone's apps (again when one is installed or removed), opens them and does what the screens ask. */
class MainActivity : ComponentActivity() {
    private lateinit var state: HomeState
    private val role = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { checkDefault() }
    private val packages = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.data?.schemeSpecificPart?.let(PhoneApps::forget)
            reload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = HomeState(SharedValues(this))
        McpMode.onChanged = { state.reload() }
        McpMode.load(this)
        reload()
        ContextCompat.registerReceiver(this, packages, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED); addAction(Intent.ACTION_PACKAGE_CHANGED); addDataScheme("package")
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        val actions = AppActions(icon = { app, px -> PhoneApps.icon(this, app, px) }, cards = { PhoneApps.summaries(this, it) }, open = ::open,
            info = { start(PhoneApps.info(it.pkg)) }, uninstall = { start(PhoneApps.uninstall(it.pkg)) },
            clock = { start(PhoneApps.clock(this)) }, calendar = { start(PhoneApps.calendar(this)) },
            wallpaper = { start(Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), getString(R.string.wallpaper_change))) },
            systemSettings = { start(Intent(Settings.ACTION_SETTINGS)) }, makeDefault = ::makeDefault, toast = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }, copy = ::copy, goToHub = ::goToHub)
        setContent {
            HomeTheme(state.themeMode, state.accent) {
                val light = HomeColors.isLight && !(state.wallpaper && state.page == Page.HOME)
                SideEffect { WindowCompat.getInsetsController(window, window.decorView).run { isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light } }
                BackHandler(state.page != Page.HOME) {
                    when {
                        state.page == Page.HIDDEN || state.page == Page.MCP -> state.page = Page.SETTINGS
                        state.page == Page.CARDS -> state.page = state.cardsBack
                        state.page == Page.SETTINGS && state.section != com.zirakusa.launcher.ui.screens.Section.MENU -> state.section = com.zirakusa.launcher.ui.screens.Section.MENU
                        state.page == Page.WELCOME -> state.finishWelcome()
                        else -> state.home()
                    }
                }
                LauncherApp(state, actions)
            }
        }
    }

    /** The home button while Inicio is open goes back to the home screen. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME) && state.page != Page.WELCOME) state.home()
    }

    override fun onResume() {
        super.onResume()
        state.h24 = DateFormat.is24HourFormat(this)
        state.nextAlarm = PhoneApps.nextAlarm(this)
        state.tick()
        checkDefault()
        reload()
    }

    override fun onDestroy() { runCatching { unregisterReceiver(packages) }; super.onDestroy() }

    private fun reload() = lifecycleScope.launch {
        val list = withContext(Dispatchers.IO) { PhoneApps.list(this@MainActivity) }
        state.load(list) { PhoneApps.dockCandidates(this@MainActivity) }
    }

    private fun open(app: AppEntry) {
        try { startActivity(PhoneApps.launch(app)); state.launched(app) } catch (_: Exception) { Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show(); reload() }
        if (state.page == Page.DRAWER || state.page == Page.FEED) window.decorView.postDelayed({ state.home() }, 400)
    }

    private fun start(intent: Intent) = try { startActivity(intent) } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
    }

    private fun checkDefault() {
        state.isDefault = packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
            ?.activityInfo?.packageName == packageName
    }

    /** «Pantalla de inicio predeterminada»: Android's own question (10+), or its home settings. */
    private fun makeDefault() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME) && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                role.launch(roles.createRequestRoleIntent(RoleManager.ROLE_HOME)); return
            }
        }
        start(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    private fun copy(link: String) {
        getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("Inicio MCP", link))
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    /** «Ir al Hub»: copies the hub's link (in case it must be pasted) and opens the Gateway Hub app. */
    private fun goToHub() {
        getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("Inicio › Gateway Hub", McpMode.hubLink()))
        val open = packageManager.getLaunchIntentForPackage("com.zirakusa.gatewayhub")
        if (open != null) startActivity(open) else Toast.makeText(this, R.string.mcp_paste_in_hub, Toast.LENGTH_LONG).show()
    }
}
