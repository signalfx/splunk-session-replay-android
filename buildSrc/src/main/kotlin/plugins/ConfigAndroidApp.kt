package plugins

import Configurations
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ConfigAndroidApp : Plugin<Project> by local plugin {
    apply<ConfigLint>()

    androidApplication {
        compileSdk = Configurations.Android.appCompileVersion

        defaultConfig {
            minSdk = Configurations.Android.appMinVersion
            targetSdk = Configurations.Android.targetVersion
        }

        compileOptions {
            sourceCompatibility = Configurations.Compilation.sourceCompatibility
            targetCompatibility = Configurations.Compilation.targetCompatibility
        }

        kotlinOptions {
            jvmTarget = Configurations.Compilation.jvmTarget
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
}
