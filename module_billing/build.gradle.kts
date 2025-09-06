plugins {
    id("com.uni.remote.tech.android.library")
}

android {
    namespace = "com.uni.remote.tech.billing"
}

dependencies {
    implementation(project(":module_common"))

    // Common
    implementation(libs.androidx.startup)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    // Billing (using KTX for Kotlin)
    implementation(libs.billing.ktx)
}
