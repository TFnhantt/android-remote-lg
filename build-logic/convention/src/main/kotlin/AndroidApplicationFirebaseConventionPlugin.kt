import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.uni.remote.tech.compositebuild.get
import com.uni.remote.tech.compositebuild.implementation
import com.uni.remote.tech.compositebuild.libs
import com.uni.remote.tech.compositebuild.plugin

class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("gms").pluginId)
            apply(libs.plugin("firebase.perf").pluginId)
            apply(libs.plugin("firebase.crashlytics").pluginId)
        }

        dependencies {
            val firebaseBom = libs["firebase-bom"]
            implementation(platform(firebaseBom))
            implementation(libs["firebase.analytics"])
            implementation(libs["firebase.performance"])
            implementation(libs["firebase.crashlytics"])
            implementation(libs["firebase.config"])
            implementation(libs["firebase.cloud.messaging"])
        }
    }
}
