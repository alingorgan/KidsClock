import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

/**
 * Screenshot (snapshot) testing with Roborazzi + Robolectric on the JVM: no emulator needed.
 * - `./gradlew check` verifies against committed baselines in `src/test/snapshots`.
 * - `./gradlew recordRoborazziDebug` (re)records them.
 * - `verifySnapshotCoverage` fails if a public @Composable file has no `<File>SnapshotTest.kt`.
 */
class AndroidSnapshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            extensions.configure(LibraryExtension::class.java) {
                testOptions.unitTests.isIncludeAndroidResources = true
            }

            dependencies {
                add("testImplementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
                add("testImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("testImplementation", libs.findLibrary("robolectric").get())
                add("testImplementation", libs.findLibrary("roborazzi").get())
                add("testImplementation", libs.findLibrary("roborazzi-compose").get())
                // Robolectric/Compose test rules are JUnit 4; run them on the same JUnit Platform.
                add("testRuntimeOnly", libs.findLibrary("junit-vintage-engine").get())
            }

            val composableFiles = fileTree("src/main") { include("**/*.kt") }
            val snapshotTests = fileTree("src/test") { include("**/*SnapshotTest.kt") }
            val moduleDir = projectDir
            val coverage = tasks.register("verifySnapshotCoverage") {
                group = "verification"
                description = "Fails if a public @Composable file has no matching <File>SnapshotTest.kt."
                inputs.files(composableFiles, snapshotTests)
                doLast {
                    val tested = snapshotTests.files.map { it.name.removeSuffix("SnapshotTest.kt") }.toSet()
                    val missing = composableFiles.files.filter { file ->
                        val lines = file.readLines()
                        lines.indices.any { i ->
                            lines[i].trim().startsWith("@Composable") &&
                                lines.drop(i + 1).firstOrNull { it.contains("fun ") }?.contains("private ") == false
                        }
                    }.map { it.nameWithoutExtension }.filter { it !in tested }
                    if (missing.isNotEmpty()) {
                        throw GradleException(
                            buildString {
                                appendLine("Every UI component needs a snapshot test (docs/DESIGN_SYSTEM.md). Missing in $moduleDir:")
                                missing.forEach { appendLine("  ${it}SnapshotTest.kt  (for $it.kt)") }
                            },
                        )
                    }
                }
            }
            tasks.named("check") { dependsOn(coverage) }
        }
    }
}
