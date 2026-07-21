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
    implementation(Dependencies.Android.Common.encoder)
    implementation(Dependencies.Android.Common.id)
    implementation(Dependencies.Android.Common.storage)
    implementation(Dependencies.Android.Common.utils)
    implementation(Dependencies.Android.Common.logger)
    implementation(Dependencies.Android.Common.http)
    implementation(Dependencies.Android.Common.job)
    implementation(project(":bridge"))
    implementation(project(":frame-capturer"))
    implementation(project(":interactions"))
    api(project(":screenshot"))
    api(project(":wireframe"))

    compileOnly(Dependencies.Android.annotation)
    compileOnly(Dependencies.Android.appcompat)
    compileOnly(Dependencies.Android.material)
}
