import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(shared.plugins.android.library)
    alias(shared.plugins.kotlin.compose)
    alias(shared.plugins.kotlin.android)
}

android {
    namespace = "io.oliverapps.media3.compose.shared"
    compileSdk {
        version = release(shared.versions.android.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    implementation(platform(shared.androidx.compose.bom))
    implementation(shared.androidx.core.ktx)
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
    implementation(shared.androidx.lifecycle.runtime.ktx)
    implementation(shared.androidx.activity.compose)
    implementation(shared.androidx.compose.ui)
    implementation(shared.androidx.compose.ui.graphics)
    implementation(shared.androidx.compose.ui.tooling.preview)

    implementation(shared.androidx.material3.icons.extended)

    debugImplementation(shared.androidx.compose.ui.tooling)
    debugImplementation(shared.androidx.compose.ui.test.manifest)
}