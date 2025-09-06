plugins {
    id("com.uni.remote.tech.android.library")
}

android {
    namespace = "com.uni.remote.tech.common"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.startup)

    implementation(libs.kotpref.core)
    implementation(libs.kotpref.initializer)

    implementation(libs.play.review)
    implementation(libs.play.app.update)

    implementation(libs.eventBus)
    implementation(libs.timber)
}
