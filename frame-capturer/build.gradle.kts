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
    namespace = "com.splunk.android.instrumentation.recording.capturer"
}

dependencies {
    implementation(Dependencies.Android.Common.utils)
    implementation(project(":bridge"))
    api(project(":screenshot"))
    api(project(":wireframe"))

    compileOnly(Dependencies.Android.annotation)
}