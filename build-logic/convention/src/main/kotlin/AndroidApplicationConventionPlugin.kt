import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("kidsclock.quality")
            extensions.configure(ApplicationExtension::class.java) {
                configureAndroidCommon(this)
                configureLint(lint)
                defaultConfig.targetSdk = TARGET_SDK
            }
            configureUnitTestDependencies()
        }
    }
}
