package com.zirakusa.launcher

import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.mcp.Access
import com.zirakusa.launcher.mcp.HubLink
import com.zirakusa.launcher.mcp.Kind
import com.zirakusa.launcher.mcp.LauncherData
import com.zirakusa.launcher.mcp.McpHandler
import com.zirakusa.launcher.mcp.Token
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Inicio's MCP: tools list, good calls, bad arguments, not found, full dock, read-only, wrong and paused tokens, hub link. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class McpTest {
    private fun app(label: String, pkg: String, module: String? = null) = AppEntry(pkg, "$pkg.Main", label, module)
    private val data = object : LauncherData {
        val list = listOf(app("Reloj", "com.zirakusa.reloj", "clock_"), app("Cámara", "com.android.camera"), app("Notas", "com.zirakusa.notas", "notes_"),
            app("Chrome", "com.android.chrome"), app("Mapas", "com.maps"), app("Música", "com.music"), app("Fotos", "com.photos"))
        var current = listOf("com.android.camera/com.android.camera.Main")
        override fun apps() = list
        override fun dock() = current
        override fun setDock(keys: List<String>) { current = keys }
        override fun hidden() = setOf("com.maps/com.maps.Main")
    }
    private val writer = Token("w", "Claude", "ini_" + "w".repeat(40), Access.WRITE, Kind.HUB, "Casa")
    private val reader = Token("r", "Lector", "ini_" + "r".repeat(40), Access.READ)
    private val paused = Token("p", "Viejo", "ini_" + "p".repeat(40), Access.WRITE, paused = true)
    private val handler = McpHandler({ listOf(writer, reader, paused) }, data, "0.1.0")

    private fun rpc(token: Token, method: String, params: JSONObject = JSONObject()) = runBlocking {
        JSONObject(handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer ${token.secret}"), emptyMap(),
            JSONObject().put("jsonrpc", "2.0").put("id", 1).put("method", method).put("params", params).toString()).body).getJSONObject("result")
    }
    private fun tool(name: String, args: JSONObject, token: Token = writer) = rpc(token, "tools/call", JSONObject().put("name", name).put("arguments", args))
    private fun ok(name: String, args: JSONObject = JSONObject()) =
        tool(name, args).also { assertFalse(it.toString(), it.optBoolean("isError")) }.getJSONObject("structuredContent")
    private fun code(name: String, args: JSONObject = JSONObject(), token: Token = writer) = tool(name, args, token).also { assertTrue(it.optBoolean("isError")) }
        .getJSONObject("structuredContent").getJSONObject("error").getString("code")
    private fun names(token: Token) = rpc(token, "tools/list").getJSONArray("tools").let { t -> (0 until t.length()).map { t.getJSONObject(it).getString("name") } }
    private fun dockPackages(o: JSONObject) = o.getJSONArray("dock").let { a -> (0 until a.length()).map { a.getJSONObject(it).getString("package") } }

    @Test fun listsTheTools() {
        assertEquals(listOf("launcher_apps", "launcher_dock", "launcher_dock_add", "launcher_dock_remove"), names(writer))
        assertEquals(listOf("launcher_apps", "launcher_dock"), names(reader))
        assertEquals("inicio", rpc(writer, "initialize").getJSONObject("serverInfo").getString("name"))
    }

    @Test fun readsAppsAndDock() {
        val all = ok("launcher_apps")
        assertEquals(7, all.getInt("total")); assertEquals("Cámara", all.getJSONArray("apps").getJSONObject(0).getString("name"))
        assertTrue(all.getJSONArray("apps").getJSONObject(0).getBoolean("in_dock"))
        val family = ok("launcher_apps", JSONObject().put("family_only", true)).getJSONArray("apps")
        assertEquals(listOf("Notas", "Reloj"), (0 until family.length()).map { family.getJSONObject(it).getString("name") })
        assertEquals("clock_", family.getJSONObject(1).getString("mcp_module"))
        assertEquals("com.music", ok("launcher_apps", JSONObject().put("query", "musica")).getJSONArray("apps").getJSONObject(0).getString("package"))
        assertTrue(ok("launcher_apps", JSONObject().put("query", "mapas")).getJSONArray("apps").getJSONObject(0).getBoolean("hidden"))
        assertEquals(listOf("com.android.camera"), dockPackages(ok("launcher_dock")))
    }

    @Test fun changesTheDock() {
        assertEquals(listOf("com.android.camera", "com.zirakusa.reloj"), dockPackages(ok("launcher_dock_add", JSONObject().put("package", "com.zirakusa.reloj"))))
        assertEquals(2, dockPackages(ok("launcher_dock_add", JSONObject().put("package", "com.zirakusa.reloj"))).size)   // already there
        assertEquals(listOf("com.zirakusa.reloj"), dockPackages(ok("launcher_dock_remove", JSONObject().put("package", "com.android.camera"))))
        listOf("com.android.chrome", "com.music", "com.photos", "com.zirakusa.notas").forEach { ok("launcher_dock_add", JSONObject().put("package", it)) }
        assertEquals("DOCK_FULL", code("launcher_dock_add", JSONObject().put("package", "com.android.camera")))
    }

    @Test fun badArgumentsNotFoundAndReadOnly() {
        assertEquals("BAD_ARGUMENT", code("launcher_apps", JSONObject().put("sql", "x")))
        assertEquals("BAD_ARGUMENT", code("launcher_apps", JSONObject().put("family_only", "yes")))
        assertEquals("BAD_ARGUMENT", code("launcher_dock_add", JSONObject()))
        assertEquals("BAD_ARGUMENT", code("launcher_dock_add", JSONObject().put("package", "../etc; rm")))
        assertEquals("NOT_FOUND", code("launcher_dock_add", JSONObject().put("package", "com.nope")))
        assertEquals("NOT_FOUND", code("launcher_dock_remove", JSONObject().put("package", "com.nope")))
        assertEquals("UNKNOWN_TOOL", code("launcher_open"))
        assertEquals("READ_ONLY", code("launcher_dock_add", JSONObject().put("package", "com.music"), reader))
        assertEquals(1, data.current.size)
    }

    @Test fun wrongAndPausedTokensAndHubLink() = runBlocking {
        val body = """{"jsonrpc":"2.0","id":1,"method":"tools/list"}"""
        assertEquals(401, handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer ${paused.secret}"), emptyMap(), body).status)
        assertEquals(401, handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer ini_wrong"), emptyMap(), body).status)
        assertEquals(200, handler.handle("POST", "/mcp", emptyMap(), mapOf("token" to reader.secret), body).status)
        assertTrue(Token.secret().startsWith("ini_"))
        assertEquals("NOT_SAME_DEVELOPER", HubLink.answer(false).getString("error"))
        assertEquals("REDIRECT_OFF", HubLink.answer(true).getString("error"))
    }
}
