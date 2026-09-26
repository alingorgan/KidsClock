import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature's public contract (`feature/<name>/api`): pure Kotlin, no Android, no Compose, no
 * `core/designsystem`. Depends on `core/model` only. Kept deliberately cheap and stable, so other
 * features (or `app`) can depend on what a feature exposes without pulling in its implementation,
 * the Android SDK or the UI toolkit. See docs/adr/0003-module-boundaries.md.
 */
class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("kidsclock.kotlin.jvm")
            dependencies {
                add("implementation", project(":core:model"))
            }
        }
    }
}
