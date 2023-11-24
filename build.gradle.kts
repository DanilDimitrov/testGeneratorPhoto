buildscript {
    val kotlin_version by extra("1.9.20-RC")
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20-RC" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false

}

