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
    namespace = "com.splunk.android.instrumentation.recording.core"
}

dependencies {
    implementation(project(":common:encoder"))
    implementation(project(":common:id"))
    implementation(project(":common:storage"))
    implementation(project(":common:utils"))
    implementation(project(":common:logger"))
    implementation(project(":common:http"))
    implementation(project(":common:job"))
    implementation(project(":bridge"))
    implementation(project(":instrumentation:session-recording:frame-capturer"))
    implementation(project(":instrumentation:session-recording:interactions"))
    api(project(":instrumentation:session-recording:screenshot"))
    api(project(":instrumentation:session-recording:wireframe"))

    compileOnly(Dependencies.Android.annotation)
    compileOnly(Dependencies.Android.appcompat)
    compileOnly(Dependencies.Android.material)
}