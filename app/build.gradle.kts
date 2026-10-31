plugins {
    id("com.android.application")
}

android {
    namespace = "com.kevinsrm.ide"

    // Sintaxe do DSL publico do AGP 9 (android.newDsl)
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "com.kevinsrm.ide"
        versionCode = 1
        versionName = "1.0.0"

        minSdk {
            version = release(24)
        }
        targetSdk {
            version = release(34)
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Termux:API opcional (integracao direta com o app Termux)
    // implementation("com.termux:termux-api:1.0")
}
