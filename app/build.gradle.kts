plugins {
    id("kidsclock.android.application")
    id("kidsclock.android.compose")
    id("kidsclock.android.uitest")
}

android {
    namespace = "com.kidsclock"

    defaultConfig {
        applicationId = "com.kidsclock"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:platform"))
    implementation(project(":feature:run:impl"))
    implementation(libs.androidx.activity.compose)
}

// Guardrail: privacy rule. The merged manifest must never request INTERNET.
val verifyNoInternetPermission =
    tasks.register("verifyNoInternetPermission") {
        group = "verification"
        description = "Fails if the merged debug/release manifest declares android.permission.INTERNET."
        val manifests =
            listOf("Debug", "Release").map { variant ->
                layout.buildDirectory.file(
                    "intermediates/merged_manifest/${variant.lowercase()}/process${variant}MainManifest/AndroidManifest.xml",
                )
            }
        dependsOn("processDebugMainManifest", "processReleaseMainManifest")
        doLast {
            manifests.forEach { f ->
                val file = f.get().asFile
                check(file.exists()) { "Merged manifest not found: $file" }
                check("android.permission.INTERNET" !in file.readText()) {
                    "INTERNET permission found in merged manifest $file. KidsClock must have no network access."
                }
            }
        }
    }

tasks.named("check") { dependsOn(verifyNoInternetPermission) }
