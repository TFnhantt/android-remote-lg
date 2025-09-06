import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.uni.remote.tech.compositebuild.configureKotlinAndroid
import com.uni.remote.tech.compositebuild.libs
import com.uni.remote.tech.compositebuild.plugin

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("kotlin.android").pluginId)
            apply(libs.plugin("android.library").pluginId)
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = ProjectConfigure.targetSdk
        }
    }
}
