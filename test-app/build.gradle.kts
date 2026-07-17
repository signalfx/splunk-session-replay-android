import plugins.ConfigAndroidApp
import utils.Version

plugins {
    id("com.android.application")
    id("kotlin-android")
}

apply<ConfigAndroidApp>()

android {
    namespace = "com.splunk.android.sr.testapp"
    compileSdk = Configurations.Android.appCompileVersion

    defaultConfig {
        applicationId = "com.splunk.android.sr.testapp"
        versionCode = Configurations.sdkVersionCode
        versionName = Configurations.sdkVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = project.file("keystore.jks")
            storePassword = "!RTbCXa95-6ueu$@"
            keyAlias = "sample"
            keyPassword = "!RTbCXa95-6ueu$@"
        }
    }

    buildTypes {
        getByName("debug") {
            resValue("bool", "leak_canary_add_launcher_icon", "false")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = Configurations.Compilation.sourceCompatibility
        targetCompatibility = Configurations.Compilation.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = Configurations.Compilation.jvmTarget
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Dependencies.Android.Compose.compilerVersion
    }

    packagingOptions {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }

    lint {
        abortOnError = false
    }

    sourceSets {
        val composeVersion = Version(Dependencies.Android.Compose.version)

        when {
            composeVersion >= Version(1, 7, 0) -> {
                getByName("main") {
                    java.srcDir("src/compose17/java")
                }

                listOf("debug", "release").forEach { buildType ->
                    getByName(buildType).manifest.srcFile("src/compose17/AndroidManifest.xml")
                }
            }
        }
    }
}

dependencies {
    implementation(Dependencies.Android.Common.id)
    implementation(Dependencies.Android.Common.utils)
    implementation(Dependencies.Android.Common.http)
    implementation(Dependencies.Android.Common.logger)
    implementation(Dependencies.Android.Common.storage)
    implementation(project(":instrumentation:session-recording:core"))
    implementation(project(":instrumentation:session-recording:wireframe"))
    implementation(project(":instrumentation:session-recording:interactions"))
    implementation(project(":instrumentation:session-recording:frame-capturer"))
    implementation(project(":debugger"))
    implementation(project(":bridge"))

    implementation(Dependencies.Android.appcompat)
    implementation(Dependencies.Android.constraintLayout)
    implementation(Dependencies.Android.activityKtx)
    implementation(Dependencies.Android.fragmentKtx)

    implementation(Dependencies.Android.cardView)
    implementation(Dependencies.Android.material)

    implementation(Dependencies.Android.Compose.activity)
    implementation(Dependencies.Android.Compose.ui)
    implementation(Dependencies.Android.Compose.animation)
    implementation(Dependencies.Android.Compose.materialIconsExtended)
    implementation(Dependencies.Android.Compose.toolingPreview)
    implementation(Dependencies.Android.Compose.foundation)

    debugImplementation(Dependencies.Android.Compose.uiTooling)
    debugImplementation(Dependencies.AndroidDebug.leakCanary)

    androidTestImplementation(Dependencies.AndroidTest.junit)
    androidTestImplementation(Dependencies.AndroidTest.testRules)
    androidTestImplementation(Dependencies.AndroidTest.mockk)
    androidTestImplementation(Dependencies.Test.jsonassert)

    implementation(Dependencies.Android.cameraLifecycle)
    implementation(Dependencies.Android.cameraExtensions)
    implementation(Dependencies.Android.cameraView)

    implementation(Dependencies.Android.exoPlayer)

    implementation(Dependencies.Android.splashScreen)
}

configurations.all {
    resolutionStrategy {
        force(Dependencies.Test.guava)
    }
}