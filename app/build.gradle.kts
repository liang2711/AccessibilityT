plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.accessibilityt"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.accessibilityt"
        minSdk = 28
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.scwang.smart:refresh-layout-kernel:2.0.3")      //核心必须依赖
    implementation("com.scwang.smart:refresh-header-classics:2.0.3")    //经典刷新头
    implementation("com.scwang.smart:refresh-header-radar:2.0.3")       //雷达刷新头
    implementation("com.scwang.smart:refresh-header-falsify:2.0.3")     //虚拟刷新头
    implementation("com.scwang.smart:refresh-header-material:2.0.3")    //谷歌刷新头
    implementation("com.scwang.smart:refresh-header-two-level:2.0.3")   //二级刷新头
    implementation("com.scwang.smart:refresh-footer-ball:2.0.3")        //球脉冲加载
    implementation("com.scwang.smart:refresh-footer-classics:2.0.3")    //经典加载


    var room_version = "2.4.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version")
}