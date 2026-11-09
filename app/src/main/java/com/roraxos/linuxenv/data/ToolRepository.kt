package com.roraxos.linuxenv.data

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads the tool catalog from assets/tools.json. Replace that file's
 * contents with your own tools — this repository doesn't validate or
 * filter what the commands do.
 */
class ToolRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadTools(): List<Tool> {
        val text = context.assets.open("tools.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<ToolCatalog>(text).tools
    }

    fun loadToolsByCategory(): Map<String, List<Tool>> =
        loadTools().groupBy { it.category }
}
