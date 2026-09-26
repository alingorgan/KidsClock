import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature's implementation (`feature/<name>/impl`): Android library + Compose + snapshot tests +
 * instrumented tests, built only from design-system blocks. Depends on `core/model` and
 * `core/designsystem`, and automatically on its own `feature/<name>/api` sibling if one exists
 * (derived from this module's Gradle path — no per-feature wiring needed). Must never depend on
 * another feature's `impl`; depend on that feature's `api` instead. See
 * docs/adr/0003-module-boundaries.md.
 */
class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("kidsclock.android.library")
            pluginManager.apply("kidsclock.android.compose")
            pluginManager.apply("kidsclock.android.snapshot")
            pluginManager.apply("kidsclock.android.uitest")
            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:designsystem"))
            }
            val apiPath = path.removeSuffix(":impl") + ":api"
            if (path.endsWith(":impl") && findProject(apiPath) != null) {
                dependencies { add("implementation", project(apiPath)) }
            }
        }
    }
}
