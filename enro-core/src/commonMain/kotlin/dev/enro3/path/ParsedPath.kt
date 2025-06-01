package dev.enro3.path

import net.thauvin.erik.urlencoder.UrlEncoderUtil

public data class ParsedPath internal constructor(
    val pathParts: List<String>,
    val queryParts: Map<String, String>,
) {
    public companion object {
        public fun fromString(path: String): ParsedPath {
            val pathParts = mutableListOf<String>()
            val queryParts = mutableMapOf<String, String>()

            val parts = path.split("?")
            val pathPattern = parts[0]
                .removePrefix("/")
                .removeSuffix("/")
            val queryPattern = parts.getOrNull(1)

            // Parse path pattern
            pathPattern.split("/").forEach { segment ->
                pathParts.add(UrlEncoderUtil.decode(segment))
            }

            // Parse query pattern
            queryPattern?.split("&")?.forEach { param ->
                val keyValue = param.split("=")
                if (keyValue.size == 2) {
                    queryParts[UrlEncoderUtil.decode(keyValue[0])] = UrlEncoderUtil.decode(keyValue[1])
                }
            }

            return ParsedPath(pathParts, queryParts)
        }
    }
}