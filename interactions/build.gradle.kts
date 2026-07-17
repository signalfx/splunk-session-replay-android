import plugins.ConfigAndroidLibrary
import plugins.ConfigPublish
import utils.artifactIdProperty
import utils.artifactPrefix
import utils.instrumentationPrefix
import utils.sessionRecordingPrefix
import utils.versionProperty

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

apply<ConfigAndroidLibrary>()
apply<ConfigPublish>()

ext {
    set(artifactIdProperty, "$artifactPrefix$instrumentationPrefix$sessionRecordingPrefix${project.name}")
    set(versionProperty, Configurations.sdkVersionName)
}

android {
    namespace = "com.splunk.android.instrumentation.recording.interactions"

    defaultConfig {
        gradle.startParameter.taskNames.onEach {
            minSdk = if ("AndroidTest" in it) Configurations.Android.appMinVersion else Configurations.Android.minVersion
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        animationsDisabled = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Dependencies.Android.Compose.compilerVersion
    }
}

dependencies {
    implementation(Dependencies.Android.Common.utils)
    implementation(Dependencies.Android.Common.logger)
    implementation(project(":wireframe"))
    implementation(project(":screenshot"))

    compileOnly(Dependencies.Android.Compose.ui)

    androidTestImplementation(Dependencies.Android.Compose.ui)
    androidTestImplementation(Dependencies.Android.Compose.foundation)
    androidTestImplementation(Dependencies.AndroidTest.espresso)
    androidTestImplementation(Dependencies.AndroidTest.composeUiTestJunit4)
}