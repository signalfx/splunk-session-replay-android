object Dependencies {

    // Project level dependencies

    private const val gradleVersion = "7.3.1" // 7.3.1, 8.13.2
    private const val kotlinVersion = "1.7.20" // 1.7.20, 1.9.0
    private const val ktlintVersion = "1.8.0"

    const val gradle = "com.android.tools.build:gradle:$gradleVersion"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val ktlint = "com.pinterest.ktlint:ktlint-cli:$ktlintVersion"

    object NexusPublish {
        const val id = "io.github.gradle-nexus.publish-plugin"
        const val version = "2.0.0"
    }

    // SDK module level dependencies
    object Test {
        private const val junitVersion = "4.12"
        private const val jsonassertVersion = "1.5.0"
        private const val robolectricVersion = "4.11.1"
        private const val runnerVersion = "1.4.0"
        private const val junitExtVersion = "1.1.3"
        private const val guavaVersion = "32.0.0-android"

        const val junit = "junit:junit:$junitVersion"
        const val junitExt = "androidx.test.ext:junit:$junitExtVersion"
        const val jsonassert = "org.skyscreamer:jsonassert:$jsonassertVersion"
        const val robolectric = "org.robolectric:robolectric:$robolectricVersion"
        const val runner = "androidx.test:runner:$runnerVersion"
        const val guava = "com.google.guava:guava:$guavaVersion"
    }

    object Android {
        private const val annotationVersion = "1.6.0"
        private const val appcompatVersion = "1.6.1"
        private const val recyclerVersion = "1.2.1"
        private const val cardVersion = "1.0.0"
        private const val materialVersion = "1.9.0"

        const val annotation = "androidx.annotation:annotation:$annotationVersion"
        const val appcompat = "androidx.appcompat:appcompat:$appcompatVersion"
        const val recycler = "androidx.recyclerview:recyclerview:$recyclerVersion"
        const val cardView = "androidx.cardview:cardview:$cardVersion"
        const val material = "com.google.android.material:material:$materialVersion"

        // Test application

        private const val constraintLayoutVersion = "2.1.4"
        private const val activityVersion = "1.2.2"
        private const val fragmentVersion = "1.3.6" // 1.3.6, 1.4.1, 1.5.7, 1.6.2, 1.7.1, 1.8.9
        private const val cameraVersion = "1.2.0"
        private const val exoPlayerVersion = "2.18.7"
        private const val splashScreenVersion = "1.0.1"

        const val constraintLayout = "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
        const val activityKtx = "androidx.activity:activity-ktx:$activityVersion"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentVersion"
        const val fragment = "androidx.fragment:fragment:$fragmentVersion"
        const val cameraLifecycle = "androidx.camera:camera-lifecycle:$cameraVersion"
        const val cameraExtensions = "androidx.camera:camera-extensions:$cameraVersion"
        const val cameraView = "androidx.camera:camera-view:$cameraVersion"
        const val exoPlayer = "com.google.android.exoplayer:exoplayer:$exoPlayerVersion"
        const val splashScreen = "androidx.core:core-splashscreen:$splashScreenVersion"

        object Compose {
            const val version = "1.2.1" // 1.2.1, 1.3.3, 1.4.3, 1.5.4, 1.6.4, !1.7.8, !1.8.3, !1.9.5, !1.10.6
            private const val foundationVersion = "1.2.1" // 1.2.1, 1.3.1, 1.4.3, 1.5.4, 1.6.8, 1.7.8, 1.8.3, 1.9.5, 1.10.1
            private const val materialIconsVersion = "1.2.1"
            private const val activityVersion = "1.3.1"

            const val compilerVersion = "1.3.2" // 1.3.2, 1.5.0

            const val activity = "androidx.activity:activity-compose:$activityVersion"
            const val ui = "androidx.compose.ui:ui:$version"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
            const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
            const val materialIconsExtended = "androidx.compose.material:material-icons-extended:$materialIconsVersion"
            const val animation = "androidx.compose.animation:animation:$version"
            const val foundation = "androidx.compose.foundation:foundation:$foundationVersion"
        }
    }

    object AndroidTest {
        private const val junitVersion = "1.1.3"
        private const val testRulesVersion = "1.4.0"
        private const val mockkVersion = "1.12.4"
        private const val espressoVersion = "3.5.1" // Leave this as is for Espresso tests in interactions.
        private const val composeUiTestJunit4Version = Android.Compose.version

        const val junit = "androidx.test.ext:junit:$junitVersion"
        const val testRules = "androidx.test:rules:$testRulesVersion"
        const val mockk = "io.mockk:mockk-android:$mockkVersion"
        const val espresso = "androidx.test.espresso:espresso-core:$espressoVersion"
        const val composeUiTestJunit4 = "androidx.compose.ui:ui-test-junit4:$composeUiTestJunit4Version"
    }

    object AndroidDebug {
        private const val leakCanaryVersion = "2.14"

        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion"
    }
}
