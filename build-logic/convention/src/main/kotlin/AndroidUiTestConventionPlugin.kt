import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * On-device (instrumented) UI testing: Compose UI test APIs (built on Espresso/UiAutomator) plus
 * raw Espresso and UiAutomator for interop tests that prove `testTagsAsResourceId` works the way
 * Maestro relies on it. Applied to `app` and to feature modules (see docs/UI_AUTOMATION.md).
 */
class AndroidUiTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure(CommonExtension::class.java) {
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            dependencies {
                add("androidTestImplementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-runner").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-rules").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-espresso-core").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-uiautomator").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
            }
        }
    }
}
