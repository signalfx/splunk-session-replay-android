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
    namespace = "com.splunk.android.instrumentation.recording.wireframe"
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        freeCompilerArgs += "-Xnullability-annotations=@android.annotation:ignore" // Because of Canvas.getClipBounds(Rect)
        freeCompilerArgs += "-module-name=ui-graphics-android"
    }
}

dependencies {
    implementation(project(":common:utils"))
    implementation(project(":common:logger"))
    implementation(project(":bridge"))

    compileOnly(Dependencies.Android.annotation)
    compileOnly(Dependencies.Android.appcompat)
    compileOnly(Dependencies.Android.material)
    compileOnly(Dependencies.Android.recycler)
    compileOnly(Dependencies.Android.Compose.ui)
}