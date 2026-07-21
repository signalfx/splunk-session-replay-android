import plugins.ConfigAndroidLibrary
import plugins.ConfigPublish
import utils.artifactIdProperty
import utils.artifactPrefix
import utils.isReleasedProperty
import utils.versionProperty

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

apply<ConfigAndroidLibrary>()
apply<ConfigPublish>()

ext {
    set(artifactIdProperty, "$artifactPrefix${project.name}")
    set(versionProperty, Configurations.sdkVersionName)
    //set(isReleasedProperty, false)
}

android {
    namespace = "com.splunk.android.debugger"
    defaultConfig {
        minSdk = 21
    }

    buildFeatures.viewBinding = true
}

dependencies {
    implementation(Dependencies.Android.fragment) { exclude("androidx.viewpager", "viewpager") }
    implementation(Dependencies.Android.constraintLayout)

    implementation(Dependencies.Android.Common.utils)
    implementation(project(":core"))
    implementation(project(":wireframe"))
    implementation(project(":screenshot"))
    implementation(project(":frame-capturer"))
    implementation(project(":interactions"))
}