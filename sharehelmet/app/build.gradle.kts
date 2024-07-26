import org.gradle.kotlin.dsl.support.kotlinCompilerOptions


plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sharehelmet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sharehelmet"
        minSdk = 24
        targetSdk = 34
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
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:20.3.0")

    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.naver.maps:map-sdk:3.18.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //부트페이
    implementation("io.github.bootpay:android:4.4.3") //최신 버전 추천
    implementation("io.github.bootpay:android-bio:4.4.21") //생체인증 결제 사용시 추가

    //뷰페이저
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.material:material:1.12.0")

    //카톡 문의
//    implementation("com.kakao.sdk:plusfriend:+")
    implementation("com.kakao.sdk:v2-all:2.20.3") // 전체 모듈 설치, 2.11.0 버전부터 지원
    implementation("com.kakao.sdk:v2-user:2.20.3") // 카카오 로그인 API 모듈
    implementation("com.kakao.sdk:v2-share:2.20.3") // 카카오톡 공유 API 모듈
    implementation("com.kakao.sdk:v2-talk:2.20.3") // 카카오톡 채널, 카카오톡 소셜, 카카오톡 메시지 API 모듈
    implementation("com.kakao.sdk:v2-friend:2.20.3") // 피커 API 모듈
    implementation("com.kakao.sdk:v2-navi:2.20.3") // 카카오내비 API 모듈
    implementation("com.kakao.sdk:v2-cert:2.20.3") // 카카오톡 인증 서비스 API 모듈

    //카톡 서비스 위한 JVM 설정 관련.
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}