package com.roraxos.linuxenv.data

import kotlinx.serialization.Serializable

/**
 * A single dashboard tool. Tools are pure data — this app has no built-in
 * knowledge of what any tool's command does. Define your own tools.json
 * with commands you're authorized to run in your environment.
 */
@Serializable
data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconName: String,
    val command: String,
    val requiredPackages: List<String> = emptyList(),
    val requiresRoot: Boolean = false
)

@Serializable
data class ToolCatalog(
    val tools: List<Tool>
)
