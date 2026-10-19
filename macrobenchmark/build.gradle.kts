plugins {
    id("com.android.test")
    id("kotlin-android")
}

android {
    namespace = "com.splunk.android.sr.macrobenchmark"
    compileSdk = Configurations.Android.appCompileVersion

    defaultConfig {
        minSdk = 23
        targetSdk = Configurations.Android.targetVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        (project.findProperty("suppressBenchmarkErrors") as String?)?.let {
            testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = it
        }
    }

    compileOptions {
        sourceCompatibility = Configurations.Compilation.sourceCompatibility
        targetCompatibility = Configurations.Compilation.targetCompatibility
    }

    kotlinOptions {
        jvmTarget = Configurations.Compilation.jvmTarget
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":test-app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

dependencies {
    implementation(Dependencies.AndroidTest.junit)
    implementation(Dependencies.AndroidTest.testRules)
    implementation(Dependencies.Macrobenchmark.uiAutomator)
    implementation(Dependencies.Macrobenchmark.benchmarkMacroJunit4)
}
