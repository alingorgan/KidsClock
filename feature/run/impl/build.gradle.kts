plugins {
    id("kidsclock.feature.impl")
}

android {
    namespace = "com.kidsclock.feature.run"
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
