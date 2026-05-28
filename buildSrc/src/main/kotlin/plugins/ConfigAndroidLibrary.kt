package plugins

import Configurations
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ConfigAndroidLibrary : Plugin<Project> by local plugin {
    apply<ConfigLint>()

    android {
        compileSdk = Configurations.Android.compileVersion

        defaultConfig {
            minSdk = Configurations.Android.minVersion
            targetSdk = Configurations.Android.targetVersion

            consumerProguardFiles("consumer-rules.pro")

            buildConfigField("String", "VERSION_NAME", "\"${Configurations.sdkVersionName}\"")
            buildConfigField("String", "VERSION_CODE", "\"${Configurations.sdkVersionCode}\"")
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }

        compileOptions {
            sourceCompatibility = Configurations.Compilation.sourceCompatibility
            targetCompatibility = Configurations.Compilation.targetCompatibility
        }

        kotlinOptions {
            jvmTarget = Configurations.Compilation.jvmTarget
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }

        testOptions {
            unitTests.isIncludeAndroidResources = true
        }

        lint {
            disable += "UseKtx"
            disable += "UnknownIssueId"
        }
    }
}
