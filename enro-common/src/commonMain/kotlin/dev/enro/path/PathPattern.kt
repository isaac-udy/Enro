package dev.enro.path

import net.thauvin.erik.urlencoder.UrlEncoderUtil

@PublishedApi
internal data class PathPattern(
    val pathElements: List<PathElement>,
    val queryElements: List<QueryElement>,
) {
    fun matches(path: ParsedPath): Boolean {
        if (pathElements.size != path.pathParts.size) {
            return false
        }

        for (i in pathElements.indices) {
            val element = pathElements[i]
            val part = path.pathParts[i]

            when (element) {
                is PathElement.Segment -> if (element.value != part) return false
                is PathElement.PathParam -> continue
            }
        }

        for (queryElement in queryElements) {
            when (queryElement) {
                is QueryElement.QueryParam -> if (!path.queryParts.containsKey(queryElement.queryName)) return false
                is QueryElement.OptionalQueryParam -> continue
            }
        }

        return true
    }

    fun toPathData(parsedPath: ParsedPath): PathData {
        val data = mutableMapOf<String, String>()
        for (i in pathElements.indices) {
            val element = pathElements[i]
            val parsedPart = parsedPath.pathParts[i]

            when (element) {
                is PathElement.Segment -> continue
                is PathElement.PathParam -> data[element.name] = parsedPart
            }
        }

        for (queryElement in queryElements) {
            when (queryElement) {
                is QueryElement.QueryParam -> {
                    val queryValue = parsedPath.queryParts[queryElement.queryName]
                    requireNotNull(queryValue)
                    data[queryElement.paramName] = queryValue
                }
                is QueryElement.OptionalQueryParam -> {
                    val queryValue = parsedPath.queryParts[queryElement.queryName]
                        ?: continue
                    data[queryElement.paramName] = queryValue
                }
            }
        }

        return PathData(data)
    }

    fun toPath(data: PathData): String {
        val pathBuilder = StringBuilder()
        val queryBuilder = StringBuilder()

        for (i in pathElements.indices) {
            val element = pathElements[i]
            when (element) {
                is PathElement.Segment -> pathBuilder.append("/").append(UrlEncoderUtil.encode(element.value))
                is PathElement.PathParam -> {
                    val value = data.data[element.name]
                    requireNotNull(value) { "Missing value for path parameter: ${element.name}" }
                    pathBuilder.append("/").append(UrlEncoderUtil.encode(value))
                }
            }
        }

        if (queryElements.isNotEmpty()) {
            queryBuilder.append("?")
            val queryValues = mutableListOf<String>()
            for (i in queryElements.indices) {
                val element = queryElements[i]
                when (element) {
                    is QueryElement.QueryParam -> {
                        val value = data.data[element.paramName]
                        requireNotNull(value) { "Missing value for query parameter: ${element.paramName}" }
                        queryValues.add("${element.queryName}=${UrlEncoderUtil.encode(value)}")
                    }
                    is QueryElement.OptionalQueryParam -> {
                        val value = data.data[element.paramName]
                        if (value != null) {
                            queryValues.add("${element.queryName}=${UrlEncoderUtil.encode(value)}")
                        }
                    }
                }
            }
            queryBuilder.append(
                queryValues.joinToString("&")
            )
        }

        return pathBuilder.toString() + queryBuilder.toString()
    }

    sealed class PathElement {
        data class Segment(val value: String) : PathElement()
        data class PathParam(val name: String) : PathElement()
    }

    sealed class QueryElement {
        abstract val queryName: String
        abstract val paramName: String

        data class QueryParam(
            override val queryName: String,
            override val paramName: String
        ) : QueryElement()

        data class OptionalQueryParam(
            override val queryName: String,
            override val paramName: String
        ) : QueryElement()
    }

    companion object {
        @PublishedApi
        internal fun fromString(pattern: String): PathPattern {
            val pathElements = mutableListOf<PathElement>()
            val queryElements = mutableListOf<QueryElement>()

            val parts = pattern.split("?", limit = 2)
            val pathPattern = parts[0]
                .removePrefix("/")
                .removeSuffix("/")
            val queryPattern = parts.getOrNull(1)

            // Parse path pattern
            pathPattern.split("/").forEach { segment ->
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    pathElements.add(
                        PathElement.PathParam(
                            segment.substring(
                                1,
                                segment.length - 1
                            )
                        )
                    )
                } else {
                    pathElements.add(PathElement.Segment(segment))
                }
            }

            // Parse query pattern
            queryPattern?.split("&")?.forEach { param ->
                val keyValue = param.split("=")
                require(keyValue.size == 2) {
                    "Invalid query parameter format: $param"
                }
                val key = keyValue[0]
                val value = keyValue[1]
                when {
                    value.startsWith("{") && value.endsWith("?}") -> {
                        queryElements.add(
                            QueryElement.OptionalQueryParam(
                                queryName = key,
                                paramName = value.substring(1, value.length - 2)
                            )
                        )
                    }
                    value.startsWith("{") && value.endsWith("}") -> {
                        queryElements.add(
                            QueryElement.QueryParam(
                                queryName = key,
                                paramName = value.substring(1, value.length - 1)
                            )
                        )
                    }
                    else -> {
                        error("Invalid query parameter format: $param")
                    }
                }
            }

            return PathPattern(pathElements, queryElements)
        }
    }
}