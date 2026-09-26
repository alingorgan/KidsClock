import org.gradle.api.Plugin
import org.gradle.api.Project

/** ktlint on every module; `./gradlew check` runs it. */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        }
    }
}
