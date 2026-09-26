import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Compose basics for every UI module. Material 3 is deliberately NOT here: only `core/designsystem`
 * depends on it, so feature modules cannot bypass the design-system blocks (see docs/DESIGN_SYSTEM.md).
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("kidsclock.android.accessibility")
            dependencies {
                add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-foundation").get())
            }
        }
    }
}
