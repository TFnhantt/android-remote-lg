@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.uni.remote.tech.android.application")
    id("com.uni.remote.tech.android.hilt")
    id("com.uni.remote.tech.android.application.firebase")
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

android {
    namespace = "com.uni.remote.tech"

    defaultConfig {
        applicationId = "com.wonbo.exonumia.identifier"
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "META-INF/**"
    }
}

dependencies {

    implementation(project(":module_common"))
    implementation(project(":module_billing"))
    implementation(project(":module_admob"))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraint)
    implementation(libs.androidx.annotation)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.process)

    // Play service
    implementation(libs.play.review)
    implementation(libs.play.app.update)
    implementation(libs.gms.ads)

    // Billing
    implementation(libs.billing.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Kotpref
    implementation(libs.kotpref.core)
    implementation(libs.kotpref.initializer)
    implementation(libs.kotpref.enumSupport)

    // Remote Konfig
    implementation(libs.remote.konfig)

    // Others
    implementation(libs.timber)
    implementation(libs.material)
    implementation(libs.glide)

}