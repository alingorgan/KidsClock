import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies

internal fun configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = COMPILE_SDK
        defaultConfig.minSdk = MIN_SDK
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        testOptions.unitTests.all {
            it.useJUnitPlatform()
            it.applyIsolationSettings()
        }
    }
}

/** Android lint: every warning fails the build; MissingPermission and HardcodedText are also explicit errors. */
internal fun configureLint(lint: Lint) {
    lint.abortOnError = true
    lint.warningsAsErrors = true
    lint.error += setOf("MissingPermission", "HardcodedText")
    // Versions are pinned on purpose (see ADR 0001) and these checks need the network, which breaks offline hooks.
    lint.disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
}

/** Same JVM unit-test stack as core/model (JUnit Jupiter + kotlin.test), so tests look alike everywhere. */
internal fun Project.configureUnitTestDependencies() {
    dependencies {
        add("testImplementation", platform(libs.findLibrary("junit-bom").get()))
        add("testImplementation", libs.findLibrary("junit-jupiter").get())
        add("testImplementation", libs.findLibrary("kotlin-test-junit5").get())
        add("testImplementation", libs.findLibrary("mockk").get())
        add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
    }
}

/**
 * Test isolation: random class and method order (a fixed seed is logged by JUnit for reproduction),
 * so tests that depend on state left behind by another test fail instead of passing by luck.
 */
internal fun Test.applyIsolationSettings() {
    // Robolectric native graphics (used by snapshot tests) needs reflective access on JDK 17+.
    jvmArgs(
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
    )
    systemProperty("junit.jupiter.testmethod.order.default", "org.junit.jupiter.api.MethodOrderer\$Random")
    systemProperty("junit.jupiter.testclass.order.default", "org.junit.jupiter.api.ClassOrderer\$Random")
}
