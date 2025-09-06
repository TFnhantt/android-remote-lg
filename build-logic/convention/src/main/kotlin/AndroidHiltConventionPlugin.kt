import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.uni.remote.tech.compositebuild.get
import com.uni.remote.tech.compositebuild.implementation
import com.uni.remote.tech.compositebuild.kapt
import com.uni.remote.tech.compositebuild.libs
import com.uni.remote.tech.compositebuild.plugin

class AndroidHiltConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("kotlin.kapt").pluginId)
            apply(libs.plugin("hilt").pluginId)
        }

        dependencies {
            implementation(libs["hilt.android"])
            implementation(libs["hilt.ext.work"])
            kapt(libs["hilt.compiler"])
            kapt(libs["hilt.ext.compiler"])
        }
    }
}
