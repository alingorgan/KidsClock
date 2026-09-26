plugins {
    `kotlin-dsl`
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.ktlint.gradlePlugin)
    implementation(libs.roborazzi.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kidsclockQuality") {
            id = "kidsclock.quality"
            implementationClass = "QualityConventionPlugin"
        }
        register("kidsclockKotlinJvm") {
            id = "kidsclock.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
        register("kidsclockAndroidLibrary") {
            id = "kidsclock.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kidsclockAndroidApplication") {
            id = "kidsclock.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kidsclockAndroidSnapshot") {
            id = "kidsclock.android.snapshot"
            implementationClass = "AndroidSnapshotConventionPlugin"
        }
        register("kidsclockFeatureApi") {
            id = "kidsclock.feature.api"
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("kidsclockFeatureImpl") {
            id = "kidsclock.feature.impl"
            implementationClass = "FeatureImplConventionPlugin"
        }
        register("kidsclockAndroidCompose") {
            id = "kidsclock.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("kidsclockAndroidAccessibility") {
            id = "kidsclock.android.accessibility"
            implementationClass = "AndroidAccessibilityConventionPlugin"
        }
        register("kidsclockAndroidUiTest") {
            id = "kidsclock.android.uitest"
            implementationClass = "AndroidUiTestConventionPlugin"
        }
        register("kidsclockDependencyRules") {
            id = "kidsclock.dependency.rules"
            implementationClass = "DependencyRulesConventionPlugin"
        }
    }
}
