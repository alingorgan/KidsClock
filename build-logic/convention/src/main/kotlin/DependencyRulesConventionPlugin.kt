import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Root-only plugin: enforces the module dependency direction across the whole build, by Gradle
 * path pattern (docs/adr/0003-module-boundaries.md), in one place — so a new `core/<concern>` or
 * `feature/<name>` module is covered automatically, with no per-module guardrail code needed.
 * Checks the `implementation`/`api` configurations only (the production dependency graph); test
 * configurations are out of scope.
 */
class DependencyRulesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target == target.rootProject) { "kidsclock.dependency.rules must be applied to the root project" }
        // The root project has no `check` task of its own by default; `lifecycle-base` provides one.
        // `./gradlew check` (no project path) already runs `check` in every project that has one, so
        // this makes the root's `check` -> verifyDependencyRules run alongside every subproject's.
        target.pluginManager.apply("lifecycle-base")
        val task =
            target.tasks.register("verifyDependencyRules") {
                group = "verification"
                description = "Fails if a module depends on another module its type may not depend on (ADR 0003)."
                doLast {
                    val violations = mutableListOf<String>()
                    target.subprojects.forEach { p ->
                        val isAllowed = allowedDependency(p.path) ?: return@forEach
                        listOfNotNull(p.configurations.findByName("implementation"), p.configurations.findByName("api"))
                            .forEach { cfg ->
                                cfg.dependencies.filterIsInstance<ProjectDependency>().forEach { dep ->
                                    if (!isAllowed(dep.path)) {
                                        violations += "${p.path} (${cfg.name}) -> ${dep.path}"
                                    }
                                }
                            }
                    }
                    if (violations.isNotEmpty()) {
                        throw GradleException(
                            buildString {
                                appendLine("Dependency not allowed for this module type (docs/adr/0003-module-boundaries.md):")
                                violations.forEach { appendLine("  $it") }
                            },
                        )
                    }
                }
            }
        target.tasks.named("check") { dependsOn(task) }
    }
}

private val coreModel = Regex("^:core:model$")
private val coreOther = Regex("^:core:(?!model$)[^:]+$")
private val featureApi = Regex("^:feature:[^:]+:api$")
private val featureImpl = Regex("^:feature:[^:]+:impl$")
private val app = Regex("^:app$")

/** A predicate for paths [path] is allowed to declare as an `implementation`/`api` dependency, or null if unrecognised (not checked). */
private fun allowedDependency(path: String): ((String) -> Boolean)? =
    when {
        coreModel.matches(path) -> { _ -> false }
        coreOther.matches(path) -> { dep -> coreModel.matches(dep) }
        featureApi.matches(path) -> { dep -> coreModel.matches(dep) }
        featureImpl.matches(path) -> { dep -> coreModel.matches(dep) || coreOther.matches(dep) || featureApi.matches(dep) }
        app.matches(path) -> { _ -> true }
        else -> null
    }
