plugins {
    id("kidsclock.android.library")
    id("kidsclock.android.compose")
    id("kidsclock.android.snapshot")
}

android {
    namespace = "com.kidsclock.core.designsystem"
}

dependencies {
    // Only this module may use Material 3; it is an implementation detail behind the Kc* blocks.
    implementation(libs.androidx.compose.material3)
}
