import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.uni.remote.tech.compositebuild.get
import com.uni.remote.tech.compositebuild.implementation
import com.uni.remote.tech.compositebuild.ksp
import com.uni.remote.tech.compositebuild.libs
import com.uni.remote.tech.compositebuild.plugin

class AndroidRoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.apply(libs.plugin("ksp").pluginId)

        dependencies {
            implementation(libs["room.runtime"])
            implementation(libs["room.ktx"])
            implementation(libs["room.paging"])
            ksp(libs["room.compiler"])
        }
    }
}
