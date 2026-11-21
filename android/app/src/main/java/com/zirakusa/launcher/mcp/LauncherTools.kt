package com.zirakusa.launcher.mcp

import com.zirakusa.launcher.data.AppEntry
import com.zirakusa.launcher.data.Apps
import com.zirakusa.launcher.data.Dock
import com.zirakusa.launcher.data.FamilyCard
import org.json.JSONArray
import org.json.JSONObject

/** What the tools read and change; the app gives the phone's apps and its settings, tests a fake. */
interface LauncherData {
    fun apps(): List<AppEntry>
    fun dock(): List<String>
    fun setDock(keys: List<String>)
    fun hidden(): Set<String>
    /** What each family app says now (its card in «Hoy»). */
    fun cards(): List<FamilyCard>
    /** The apps opened most (counted on the phone only). */
    fun frequent(): List<AppEntry>
}

/** An expected failure the AI can read (structuredContent.error.code), never a crash. */
class ToolFailure(val code: String, message: String) : Exception(message)

/**
 * Inicio's MCP tools: the apps on the phone (and which of them are family apps with their own MCP module) and the
 * dock; with a read-and-write token also add to and remove from the dock. Apps are never opened or removed from here.
 */
object LauncherTools {
    class Tool(val name: String, val description: String, val write: Boolean, val schema: JSONObject)

    private fun schema(required: List<String>, vararg props: Pair<String, JSONObject>) = JSONObject()
        .put("type", "object").put("properties", JSONObject().apply { props.forEach { (k, v) -> put(k, v) } })
        .put("required", JSONArray(required)).put("additionalProperties", false)
    private fun prop(type: String, description: String) = JSONObject().put("type", type).put("description", description)
    private val pkg = "package" to prop("string", "The app's package, e.g. com.zirakusa.reloj (from launcher_apps).")

    val tools = listOf(
        Tool("launcher_apps", "Apps that can be opened on the phone, A to Z: name, package, whether it is a family app (and its MCP " +
            "module, reachable through Gateway Hub), in the dock or hidden. Optionally filtered by «query» (accents ignored).", false,
            schema(emptyList(), "query" to prop("string", "Words of the app's name or package."),
                "family_only" to prop("boolean", "Only the family apps (they speak MCP)."))),
        Tool("launcher_today", "The user's day as Inicio's «Hoy» shows it: one card per family app that has something to say (e.g. Reloj: " +
            "the next alarm; Calendario: today's events), then the apps opened most. Each card names its app and MCP module.", false, schema(emptyList())),
        Tool("launcher_dock", "The apps in the dock at the bottom of the home screen, in order (up to ${Dock.MAX}).", false, schema(emptyList())),
        Tool("launcher_dock_add", "Adds an app to the end of the dock (nothing when it is already there).", true, schema(listOf("package"), pkg)),
        Tool("launcher_dock_remove", "Removes an app from the dock (the app stays installed).", true, schema(listOf("package"), pkg)),
    )

    fun available(access: Access) = tools.filter { !it.write || access == Access.WRITE }

    fun call(name: String, args: JSONObject, access: Access, data: LauncherData): JSONObject {
        val tool = tools.firstOrNull { it.name == name } ?: throw ToolFailure("UNKNOWN_TOOL", "No tool called «$name».")
        if (tool.write && access != Access.WRITE) throw ToolFailure("READ_ONLY", "This token can only read. Create a read-and-write token in Inicio.")
        val props = tool.schema.getJSONObject("properties")
        args.keys().forEach { if (!props.has(it)) throw ToolFailure("BAD_ARGUMENT", "Unknown argument «$it».") }
        val required = tool.schema.getJSONArray("required")
        (0 until required.length()).map { required.getString(it) }.forEach { if (!args.has(it)) throw ToolFailure("BAD_ARGUMENT", "Missing «$it».") }
        return when (name) {
            "launcher_apps" -> apps(args, data)
            "launcher_dock" -> dock(data)
            "launcher_today" -> JSONObject().put("cards", JSONArray(data.cards().map { c ->
                JSONObject().put("app", c.app.label).put("package", c.app.pkg).put("mcp_module", c.app.module ?: JSONObject.NULL)
                    .put("title", c.title).put("lines", JSONArray(c.lines)).put("empty", c.empty)
            })).put("frequent", JSONArray(data.frequent().map { JSONObject().put("name", it.label).put("package", it.pkg) }))
            else -> {
                val p = (args.opt("package") as? String)?.trim()?.takeIf { Regex("^[A-Za-z0-9_.]{1,200}$").matches(it) }
                    ?: throw ToolFailure("BAD_ARGUMENT", "package must be an app package like com.example.app.")
                val app = Apps.byPackage(data.apps(), p) ?: throw ToolFailure("NOT_FOUND", "No app «$p» that can be opened.")
                val now = data.dock()
                val next = if (name == "launcher_dock_add") Dock.add(now, app.key) else Dock.remove(now, app.key)
                if (name == "launcher_dock_add" && app.key !in now && next == now) throw ToolFailure("DOCK_FULL", "The dock already has ${Dock.MAX} apps.")
                if (next != now) data.setDock(next)
                dock(data)
            }
        }
    }

    private fun apps(args: JSONObject, data: LauncherData): JSONObject {
        val query = if (!args.has("query")) "" else (args.opt("query") as? String)?.takeIf { it.length <= 100 }
            ?: throw ToolFailure("BAD_ARGUMENT", "query must be text of up to 100 characters.")
        val familyOnly = if (!args.has("family_only")) false else args.opt("family_only") as? Boolean
            ?: throw ToolFailure("BAD_ARGUMENT", "family_only must be true or false.")
        val all = data.apps().let { if (familyOnly) Apps.family(it) else it }
        val found = if (query.isBlank()) Apps.sorted(all) else Apps.search(all, query)
        val dock = data.dock().toSet(); val hidden = data.hidden()
        return JSONObject().put("total", found.size).put("apps", JSONArray(found.take(300).map { describe(it, it.key in dock, it.key in hidden) }))
    }

    private fun dock(data: LauncherData): JSONObject {
        val apps = data.apps().associateBy { it.key }
        return JSONObject().put("max", Dock.MAX).put("dock", JSONArray(data.dock().mapNotNull { apps[it] }.map { describe(it, true, false) }))
    }

    private fun describe(a: AppEntry, inDock: Boolean, hidden: Boolean) = JSONObject().put("name", a.label).put("package", a.pkg)
        .put("family", a.family).put("mcp_module", a.module ?: JSONObject.NULL).put("in_dock", inDock).put("hidden", hidden)
}
