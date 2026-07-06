import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(shared.plugins.android.application)
    alias(shared.plugins.kotlin.android)
    alias(shared.plugins.kotlin.compose)
}

android {
    namespace = "io.oliverapps.sample.media3.compose"
    compileSdk {
        version = release(shared.versions.android.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = shared.versions.applicationId.get()
        minSdk = 29
        targetSdk = shared.versions.android.targetSdk.get().toInt()
        versionCode = shared.versions.versionCode.get().toInt()
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(platform(shared.androidx.compose.bom))
    implementation(shared.androidx.core.ktx)
    implementation(shared.androidx.lifecycle.runtime.ktx)
    implementation(shared.androidx.activity.compose)
    implementation(shared.androidx.compose.ui)
    implementation(shared.androidx.compose.ui.graphics)
    implementation(shared.androidx.compose.ui.tooling.preview)
    implementation(shared.media3.exoplayer)
    implementation(shared.media3.exoplayer.dash)
    implementation(shared.media3.exoplayer.hls)
    implementation(shared.media3.exoplayer.rtsp)
    implementation(shared.media3.ui)
    implementation(shared.media3.session)
    implementation(shared.media3.extractor)
    implementation(shared.media3.datasource)
    implementation(shared.media3.common)
    implementation(shared.media3.common.ktx)
    implementation(shared.media3.database)
    implementation(shared.media3.ui.compose)
    implementation(shared.coil.compose)
    debugImplementation(shared.androidx.compose.ui.tooling)
    debugImplementation(shared.androidx.compose.ui.test.manifest)
    implementation(mobile.androidx.compose.material3)

}