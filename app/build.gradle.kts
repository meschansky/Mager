import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

data class ReleaseVersion(
    val versionCode: Int,
    val versionName: String
)

fun releaseVersionFromTag(tag: String?): ReleaseVersion {
    val match = Regex("""^v(\d+)\.(\d+)\.(\d+)$""").matchEntire(tag.orEmpty())
        ?: return ReleaseVersion(versionCode = 1, versionName = "0.0.0-dev")

    val (major, minor, patch) = match.destructured
    val majorInt = major.toInt()
    val minorInt = minor.toInt()
    val patchInt = patch.toInt()

    return ReleaseVersion(
        versionCode = (majorInt * 1_000_000) + (minorInt * 1_000) + patchInt,
        versionName = "$majorInt.$minorInt.$patchInt"
    )
}

val releaseVersion = releaseVersionFromTag(
    providers.environmentVariable("RELEASE_TAG")
        .orElse(providers.environmentVariable("GITHUB_REF_NAME"))
        .orNull
)

android {
    namespace = "com.example.armoredage"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.armoredage"
        minSdk = 26
        targetSdk = 36
        versionCode = releaseVersion.versionCode
        versionName = releaseVersion.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val ciSigning = System.getenv("CI_SIGNING") == "true"
    if (ciSigning) {
        signingConfigs {
            create("ciRelease") {
                storeFile = file(System.getenv("CI_KEYSTORE_PATH") ?: "")
                storePassword = System.getenv("CI_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CI_KEY_ALIAS")
                keyPassword = System.getenv("CI_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (ciSigning) {
                signingConfig = signingConfigs.getByName("ciRelease")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okio:okio:3.9.0")
    implementation("com.github.android-password-store:kage:0.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
