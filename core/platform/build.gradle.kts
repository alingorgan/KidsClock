plugins {
    id("kidsclock.android.library")
}

android {
    namespace = "com.kidsclock.core.platform"
}

dependencies {
    implementation(project(":core:model"))
}
