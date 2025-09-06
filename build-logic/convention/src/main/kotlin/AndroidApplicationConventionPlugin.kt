import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.uni.remote.tech.compositebuild.configureKotlinAndroid
import com.uni.remote.tech.compositebuild.libs
import com.uni.remote.tech.compositebuild.plugin

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("kotlin.android").pluginId)
            apply(libs.plugin("android.application").pluginId)
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = ProjectConfigure.targetSdk
        }
    }
}
