plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    //id("org.jetbrains.kotlin.android") // Add this line

}

android {
    namespace = "com.example.moodsync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.moodsync"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {
    //implementation(files("/Users/saumya/Library/Android/sdk/platforms/35/android.jar"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.google.firebase.storage)
//    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
//    implementation(files("/Users/saumya/Library/Android/sdk/platforms/android-35/android.jar"))

}

//tasks.withType<Javadoc> {
//    exclude("**/module-info.java")
//    options.encoding = "UTF-8"
//
//    // Add these lines to handle Kotlin modules
//    classpath += files(android.bootClasspath.joinToString(File.pathSeparator))
//    classpath += configurations.getByName("implementation")
//
//    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
//
//    // This is critical - set failOnError to false
//    failOnError = false
//}

tasks.withType<Javadoc> {
    exclude("**/module-info.java")
    options.encoding = "UTF-8"

    // Properly set failOnError
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xdoclint:none", true)
    }

    // Use the setFailOnError method directly on the task
    setFailOnError(false)
}






