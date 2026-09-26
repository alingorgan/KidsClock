import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Automation guardrail: every interactive or icon-only Composable must be reachable by UI
 * automation (Maestro, Espresso/Compose UI tests) and, where it has no visible text, by
 * accessibility services. See docs/UI_AUTOMATION.md.
 *
 * This is a heuristic text scan (same approach as `verifyModelPurity`), not a type-aware check:
 * it looks at each top-level `@Composable` function body for interactive-widget calls
 * (`Button(`, `IconButton(`, `.clickable(`, `Switch(`, ...) and requires `testTag(` somewhere in
 * that function, and for `Icon(` calls requires a non-null `contentDescription`. A function can
 * opt out with a `// kc-a11y-ignore: <reason>` comment, for genuinely decorative composables.
 */
class AndroidAccessibilityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val task =
                tasks.register("verifyAccessibleInteractions") {
                    group = "verification"
                    description = "Fails if an interactive or icon-only Composable has no testTag/contentDescription."
                    val sources = fileTree("src/main") { include("**/*.kt") }
                    inputs.files(sources)
                    doLast {
                        val problems = mutableListOf<String>()
                        sources.files.forEach { file -> problems += scanFile(file) }
                        if (problems.isNotEmpty()) {
                            throw GradleException(
                                buildString {
                                    appendLine(
                                        "Every interactive/icon-only Composable needs a testTag/contentDescription " +
                                            "so automation and accessibility can reach it (docs/UI_AUTOMATION.md):",
                                    )
                                    problems.forEach { appendLine("  $it") }
                                },
                            )
                        }
                    }
                }
            tasks.named("check") { dependsOn(task) }
        }
    }
}

private val interactiveMarkers =
    listOf(
        "Button(", "IconButton(", "TextButton(", "FilledTonalButton(", "OutlinedButton(",
        // No trailing "(" for these: Kotlin allows a bare trailing lambda, e.g. `.clickable { ... }`.
        ".clickable", ".combinedClickable", ".toggleable", ".selectable",
        "Switch(", "Checkbox(", "RadioButton(", "Slider(",
    )

private fun scanFile(file: java.io.File): List<String> {
    val text = file.readText()
    val problems = mutableListOf<String>()
    var searchFrom = 0
    while (true) {
        val at = text.indexOf("@Composable", searchFrom)
        if (at == -1) break
        val funAt = text.indexOf("fun ", at)
        val bodyEnd = nextTopLevelClose(text, at)
        if (funAt == -1 || funAt > bodyEnd) {
            searchFrom = at + 1
            continue
        }
        val name = text.substring(funAt + 4, text.indexOf('(', funAt)).trim()
        val body = text.substring(at, bodyEnd)
        val ignored = body.lineSequence().any { it.trimStart().startsWith("// kc-a11y-ignore") }
        if (!ignored) {
            val isInteractive = interactiveMarkers.any { it in body }
            if (isInteractive && "testTag(" !in body) {
                problems += "${file.name}: $name() is interactive but has no testTag(...)"
            }
            if ("Icon(" in body && ("contentDescription = null" in body || "contentDescription" !in body)) {
                problems += "${file.name}: $name() has an Icon() with no non-null contentDescription"
            }
        }
        searchFrom = bodyEnd
    }
    return problems
}

/** From a `@Composable` at [from], finds the end of its top-level function: a line that is just "}". */
private fun nextTopLevelClose(text: String, from: Int): Int {
    val lines = text.substring(from).lines()
    var offset = from
    for (line in lines) {
        if (line == "}") return offset + line.length
        offset += line.length + 1
    }
    return text.length
}
