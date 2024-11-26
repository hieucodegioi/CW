// Root-level build.gradle.kts

plugins {
    id("com.android.application") version "8.5.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    kotlin("android") version "1.8.0" apply false  // Đảm bảo phiên bản Kotlin đúng
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")  // Đảm bảo classpath cho Kotlin plugin
    }
}

allprojects {

}
