plugins {
    id("com.uni.remote.tech.android.library")
}

android {
    namespace = "com.uni.remote.tech.admob"

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":module_common"))

    // Common
    implementation(libs.androidx.constraint)
    implementation(libs.androidx.startup)
    implementation(libs.material)
    implementation(libs.timber)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Admob
    implementation(libs.gms.ads) // version 23.2.0
    implementation(libs.gms.adsIdentifier) // version 18.0.1
    implementation(libs.ump) // version 3.0.0
    implementation(libs.facebook.mediation) // version 6.17.0.0

    implementation(libs.skeletonlayout)
}
