plugins {
    id("kidsclock.android.library")
}

android {
    namespace = "com.kidsclock.core.data"
}

dependencies {
    implementation(project(":core:model"))
}
