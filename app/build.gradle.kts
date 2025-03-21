import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.rehman.blurhash"
    compileSdk = 35



    defaultConfig {
        applicationId = "com.rehman.blurhash"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    tasks.register("printVersionName") {
        doLast {
            println(android.defaultConfig.versionName)
        }
    }

    applicationVariants.all {
        this.outputs
            .map { it as ApkVariantOutputImpl }
            .forEach { output ->
                val variant = this.buildType.name
                val apkName = "BlurHash-${this.versionName}-$variant.apk"
                output.outputFileName = apkName
            }
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")

        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Gson
    implementation(libs.gson)

    // Responsive Screens
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Dots Indicator
    implementation(libs.dotsindicator)

    // Image Picker
    implementation(libs.imagepicker)

    // Color Palette
    implementation(libs.androidx.palette.ktx)

    // Blur Hash
    implementation(libs.vanniktech.blurhash)

    // Retrofit for API Calls
    implementation(libs.retrofit)


}