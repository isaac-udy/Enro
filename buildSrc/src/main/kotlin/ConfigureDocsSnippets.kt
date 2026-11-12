import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File

/**
 * Keeps the Kotlin samples in the docs site (`docs/ghpages`) in step with compiled source.
 *
 * A sample lives in a snippets module as real Kotlin, between region markers:
 *
 * ```
 * // region docs:animations/decorator
 * ...
 * // endregion
 * ```
 *
 * A Markdown page binds a fenced block to a region with an HTML comment on the line above
 * the fence:
 *
 * ```
 * <!-- snippet: animations/decorator -->
 * ```
 *
 * Registers two tasks:
 * - `checkDocsSnippets` fails when a bound block differs from its region.
 * - `updateDocsSnippets` rewrites every bound block from its region.
 *
 * A source line that ends with `// docs:hide` is compiled but left out of the docs. A block
 * that cannot be compiled is marked `<!-- snippet: skip (reason) -->`. Kotlin blocks with no
 * marker are not checked; set `failOnUnmarked` once every page is migrated.
 */
class ConfigureDocsSnippets : Plugin<Project> {
    override fun apply(project: Project) {
        val root = project.rootProject
        fun DocsSnippetsTask.configure(update: Boolean) {
            group = "verification"
            snippetSources.from(
                root.fileTree("docs/snippets/src") { include("**/*.kt") },
                root.fileTree("docs/snippets-html/src") { include("**/*.kt") },
                // Docs pages can also show regions of the recipes, which are
                // compiled and run by the recipes' own tests.
                root.fileTree("recipes") {
                    include("**/src/**/*.kt")
                    exclude("**/build/**")
                },
            )
            docsDirectory.set(root.layout.projectDirectory.dir("docs/ghpages"))
            this.update.set(update)
            failOnUnmarked.set(false)
            enroVersion.set(project.enroVersionName)
        }
        project.tasks.register("checkDocsSnippets", DocsSnippetsTask::class.java) {
            description = "Fails when a docs code sample differs from its compiled snippet."
            configure(update = false)
        }
        project.tasks.register("updateDocsSnippets", DocsSnippetsTask::class.java) {
            description = "Rewrites docs code samples from their compiled snippets."
            configure(update = true)
        }
    }
}

@UntrackedTask(because = "Reads and rewrites Markdown files in place")
abstract class DocsSnippetsTask : DefaultTask() {
    @get:Internal
    abstract val snippetSources: ConfigurableFileCollection

    @get:Internal
    abstract val docsDirectory: DirectoryProperty

    @get:Internal
    abstract val update: Property<Boolean>

    @get:Internal
    abstract val failOnUnmarked: Property<Boolean>

    @get:Internal
    abstract val enroVersion: Property<String>

    @TaskAction
    fun run() {
        val errors = mutableListOf<String>()
        val regions = readRegions(errors)
        val docsDir = docsDirectory.get().asFile
        val pages = docsDir.walkTopDown()
            .filter { it.isFile && it.extension == "md" }
            .sortedBy { it.path }
            .toList()

        val usedRegions = mutableSetOf<String>()
        var staleBlocks = 0
        var bound = 0
        var skipped = 0
        var unmarked = 0
        pages.forEach { page ->
            val name = page.relativeTo(docsDir).path
            val result = processPage(name, page.readLines(), regions, usedRegions, errors)
            staleBlocks += result.stale
            bound += result.bound
            skipped += result.skipped
            unmarked += result.unmarked
            if (result.bound + result.skipped + result.unmarked > 0) {
                // Shown with --info; the totals below are always shown.
                logger.info("$name: ${result.bound} bound, ${result.skipped} skipped, ${result.unmarked} unmarked")
            }
            if (update.get() && result.rewritten != null) {
                page.writeText(result.rewritten.joinToString("\n", postfix = "\n"))
            }
            if (failOnUnmarked.get() && result.unmarked > 0) {
                errors += "$name: ${result.unmarked} kotlin block(s) have no snippet marker"
            }
            if (!update.get()) checkVersions(name, page.readLines(), errors)
        }

        logger.lifecycle("Docs kotlin blocks: $bound bound to a snippet, $skipped skipped, $unmarked unmarked.")
        (regions.keys - usedRegions).sorted().forEach {
            logger.warn("Snippet region '$it' is not used by any docs page")
        }
        if (update.get() && errors.isEmpty()) {
            logger.lifecycle("Docs snippets updated.")
            return
        }
        if (errors.isNotEmpty()) {
            val hint = if (staleBlocks > 0) "\n\nRun `./gradlew updateDocsSnippets` to rewrite the stale blocks." else ""
            throw GradleException("Docs snippets check failed:\n" + errors.joinToString("\n") { "  - $it" } + hint)
        }
    }

    /** Region id to region text, read from every snippet source file. */
    private fun readRegions(errors: MutableList<String>): Map<String, List<String>> {
        val regions = mutableMapOf<String, List<String>>()
        snippetSources.files.sortedBy { it.path }.forEach { file ->
            // A null id is an ordinary `// region` that is not a docs snippet.
            val open = ArrayDeque<Pair<String?, MutableList<String>>>()
            file.readLines().forEachIndexed { index, line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith(REGION_PREFIX) -> {
                        open.addLast(trimmed.removePrefix(REGION_PREFIX).trim() to mutableListOf())
                    }
                    trimmed.startsWith("// region") -> open.addLast(null to mutableListOf())
                    trimmed.startsWith("// endregion") -> {
                        val (id, lines) = open.removeLastOrNull() ?: run {
                            errors += "${file.name}:${index + 1}: `// endregion` has no matching region"
                            return@forEachIndexed
                        }
                        if (id == null) return@forEachIndexed
                        if (id in regions) errors += "${file.name}:${index + 1}: duplicate snippet region '$id'"
                        regions[id] = normalise(lines)
                    }
                    line.trimEnd().endsWith(HIDE_SUFFIX) -> Unit
                    else -> open.forEach { (id, lines) -> if (id != null) lines += line }
                }
            }
            open.forEach { (id, _) ->
                if (id != null) errors += "${file.name}: snippet region '$id' has no `// endregion`"
            }
        }
        return regions
    }

    private class PageResult(
        val bound: Int,
        val skipped: Int,
        val unmarked: Int,
        val stale: Int,
        val rewritten: List<String>?,
    )

    private fun processPage(
        name: String,
        lines: List<String>,
        regions: Map<String, List<String>>,
        usedRegions: MutableSet<String>,
        errors: MutableList<String>,
    ): PageResult {
        val output = mutableListOf<String>()
        var bound = 0
        var skipped = 0
        var unmarked = 0
        var stale = 0
        var changed = false
        var pendingMarker: String? = null
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val marker = MARKER.matchEntire(line.trim())
            if (marker != null) {
                pendingMarker = marker.groupValues[1].trim()
                output += line
                index++
                continue
            }
            val fence = FENCE_OPEN.matchEntire(line)
            if (fence == null) {
                if (line.isNotBlank() && pendingMarker != null) {
                    errors += "$name:${index + 1}: snippet marker '$pendingMarker' is not followed by a code block"
                    pendingMarker = null
                }
                output += line
                index++
                continue
            }

            val indent = fence.groupValues[1]
            val language = fence.groupValues[2]
            val closing = (index + 1 until lines.size).firstOrNull { lines[it].trim() == "```" }
            if (closing == null) {
                errors += "$name:${index + 1}: code block is never closed"
                output += lines.subList(index, lines.size)
                break
            }
            val body = lines.subList(index + 1, closing)
            val markerId = pendingMarker
            pendingMarker = null
            output += line

            when {
                markerId == null -> {
                    if (language == "kotlin") unmarked++
                    output += body
                }
                markerId.startsWith("skip") -> {
                    skipped++
                    output += body
                }
                else -> {
                    bound++
                    usedRegions += markerId
                    val region = regions[markerId]
                    if (region == null) {
                        errors += "$name:${index + 1}: no snippet region named '$markerId'"
                        output += body
                    } else {
                        val expected = region.map { if (it.isEmpty()) it else indent + it }
                        if (body.map { it.trimEnd() } != expected) {
                            stale++
                            changed = true
                            if (!update.get()) {
                                errors += "$name:${index + 1}: block differs from snippet '$markerId'\n" +
                                    describeDifference(body.map { it.trimEnd() }, expected)
                            }
                        }
                        output += if (update.get()) expected else body
                    }
                }
            }
            output += lines[closing]
            index = closing + 1
        }
        return PageResult(bound, skipped, unmarked, stale, output.takeIf { changed })
    }

    private fun checkVersions(name: String, lines: List<String>, errors: MutableList<String>) {
        val expected = enroVersion.get()
        lines.forEachIndexed { index, line ->
            ARTIFACT_VERSION.findAll(line).forEach { match ->
                val version = match.groupValues[1]
                if (version != expected) {
                    errors += "$name:${index + 1}: ${match.value} should use version $expected"
                }
            }
        }
    }

    private fun describeDifference(actual: List<String>, expected: List<String>): String {
        val at = (0 until maxOf(actual.size, expected.size)).first { actual.getOrNull(it) != expected.getOrNull(it) }
        return "      first difference at block line ${at + 1}:\n" +
            "        docs:    ${actual.getOrNull(at) ?: "<end of block>"}\n" +
            "        snippet: ${expected.getOrNull(at) ?: "<end of snippet>"}"
    }

    /** Removes the common indent, trailing whitespace, and leading and trailing blank lines. */
    private fun normalise(lines: List<String>): List<String> {
        val indent = lines.filter { it.isNotBlank() }.minOfOrNull { line -> line.takeWhile { it == ' ' }.length } ?: 0
        return lines
            .map { if (it.isBlank()) "" else it.drop(indent).trimEnd() }
            .dropWhile { it.isEmpty() }
            .dropLastWhile { it.isEmpty() }
    }

    private companion object {
        const val REGION_PREFIX = "// region docs:"
        const val HIDE_SUFFIX = "// docs:hide"
        val MARKER = Regex("""<!--\s*snippet:\s*(.+?)\s*-->""")
        val FENCE_OPEN = Regex("""^(\s*)```(\w*)\s*$""")
        val ARTIFACT_VERSION = Regex("""dev\.enro:[a-z-]+:(\d+\.\d+\.\d+[\w.-]*)""")
    }
}
