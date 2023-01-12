# Creating a Camera App w/ DJI Widgets Tutorial
## Pre-requisite Knowledge
***`WARNING: THIS TUTORIAL ASSUMES YOU'VE COMPLETED THE PREVIOUS TUTORIALS`***

This tutorial is designed for you to gain a basic understanding of the DJI Mobile SDK. It will implement the FPV view and two basic camera functionalities: 
- Take Photo
- Record video.

You can download the tutorial's final sample project from this [Github Page](https://github.com/riis/teachable). <!-- Make sure to update when committed-->

## Application Activation and Aircraft Binding in China
For DJI SDK mobile application used in China, it's required to activate the application and bind the aircraft to the user's DJI account.

If an application is not activated, the aircraft not bound (if required), or a legacy version of the SDK (< 4.1) is being used, all camera live streams will be disabled, and flight will be limited to a zone of 100m diameter and 30m height to ensure the aircraft stays within line of sight.

To learn how to implement this feature, please check this tutorial [Application Activation and Aircraft Binding](https://developer.dji.com/mobile-sdk/documentation/android-tutorials/ActivationAndBinding.html).

## Preparation
Throughout this tutorial we will be using Android Studio Dolphin | 2021.3.1. You can download the latest version of Android Studio from [here](http://developer.android.com/sdk/index.html).

> Note: In this tutorial, we will use Mavic Mini for testing. However, most other DJI drone models should be capable of working with this code. It is recommended to use the latest version of Android Studio for using this application.

## Setting up the Application

### 1. Setting up the Project

*   Open Android Studio and on the start-up screen select **File -> New Project**

*   In the **New Project** screen:
    *   Set the device to **"Phone and Tablet"**.
    *   Set the template to **"Empty Activity"** and then press **"Next"**.

*   On the next screen:
    * Set the **Application name** to your desired app name. In this example we will use `Camera-App`.
    * The **Package name** is conventionally set to something like "com.companyName.applicationName". We will use `com.riis.cameraapp`.
    * Set **Language** to Kotlin
    * Set **Minimum SDK** to `API 21: Android 5.0 (Lollipop)`
    * Do **NOT** check the option to "Use legacy android.support.libraries"
    * Click **Finish** to create the project.

### 2. Import Maven Dependency
In our previous tutorial, [Importing and Activating DJI SDK](https://github.com/godfreynolan/DJITutorialsKotlin/tree/main/1-Registration) in Android Studio Project, you have learned how to import the Android SDK Maven Dependency and activate your application. If you haven't read that previously, please take a look at it and implement the related features. Please use the following files.

**build.gradle**
Please replace **everything** in the `build.gradle (Project)` with:

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext.kotlin_version = '1.6.10'
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```
This is a Gradle build file, which is used to configure and manage the build process for a project.

The first block, `buildscript`, is used to configure the build script for the project. Within this block, the Kotlin version being used is defined as `1.6.10` and the repositories that will be used to resolve dependencies are specified as `google()`, `mavenCentral()`. The dependencies that are needed for the build script are also defined here, including the Android Gradle plugin and the Kotlin Gradle plugin. It's important to note that application dependencies should not be placed here and should instead be placed in the individual module build files.

The next block, `allprojects`, is used to configure the settings for all projects within the build. In this case, the block is used to specify the repositories that will be used to resolve dependencies for all projects as `google()`, `jcenter()`, and `mavenCentral()`.

Finally, the last block is defining a task called `clean`, which is of type `Delete`. This task is used to delete the `rootProject.buildDir` when the task is executed. This is typically used to clean the build files and prepare for a fresh build.

**build.gradle (Module)**
Please replace **everything** in the `build.gradle (Module)` with:

```kotlin
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    namespace 'com.riis.cameraapp'
    compileSdkVersion 31
    buildToolsVersion "30.0.3"

    defaultConfig {
        manifestPlaceholders = [DJI_API_KEY: djiKey]
        applicationId 'com.riis.cameraapp'
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        multiDexEnabled true
        versionName "1.0"
        ndk {
            // On x86 devices that run Android API 23 or above, if the application is targeted with API 23 or
            // above, FFmpeg lib might lead to runtime crashes or warnings.
            abiFilters 'armeabi-v7a', 'x86', 'arm64-v8a'
        }

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            shrinkResources false
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    dexOptions {
        javaMaxHeapSize "4g"
    }

    packagingOptions {
        doNotStrip "*/*/libdjivideo.so"
        doNotStrip "*/*/libSDKRelativeJNI.so"
        doNotStrip "*/*/libFlyForbid.so"
        doNotStrip "*/*/libduml_vision_bokeh.so"
        doNotStrip "*/*/libyuv2.so"
        doNotStrip "*/*/libGroudStation.so"
        doNotStrip "*/*/libFRCorkscrew.so"
        doNotStrip "*/*/libUpgradeVerify.so"
        doNotStrip "*/*/libFR.so"
        doNotStrip "*/*/libDJIFlySafeCore.so"
        doNotStrip "*/*/libdjifs_jni.so"
        doNotStrip "*/*/libsfjni.so"
        doNotStrip "*/*/libDJICommonJNI.so"
        doNotStrip "*/*/libDJICSDKCommon.so"
        doNotStrip "*/*/libDJIUpgradeCore.so"
        doNotStrip "*/*/libDJIUpgradeJNI.so"
        exclude 'META-INF/rxjava.properties'
    }
}

dependencies {

    implementation 'androidx.documentfile:documentfile:1.0.1'
    //DJI Dependencies
    implementation 'androidx.multidex:multidex:2.0.0'
    implementation ('com.dji:dji-sdk:4.16', {
        exclude module: 'library-anti-distortion'
        exclude module: 'fly-safe-database'
    })
    implementation ('com.dji:dji-uxsdk:4.16', {
        exclude module: 'library-anti-distortion'
        exclude module: 'fly-safe-database'
    })
    compileOnly ('com.dji:dji-sdk-provided:4.16')

    // ViewModels and Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2'
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.2.4")


    //Default
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.lifecycle:lifecycle-extensions:2.0.0-rc01'
    implementation 'androidx.annotation:annotation:1.2.0'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.3.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
```

This is also a Gradle build file for an Android application that uses the Kotlin programming language.

At the top of the file, the `plugins` block is used to apply two plugins to the project: `com.android.application` and `kotlin-android`. The first plugin is used to build Android applications, while the second plugin is used to build Kotlin Android applications.

The `android` block is used to configure various settings for the Android application. The `namespace` is set to 'com.riis.cameraapp', `compileSdkVersion` is set to 31 and `buildToolsVersion` is set to "30.0.3"

The `defaultConfig` block is used to configure settings for the default build configuration. The `applicationId` is set to 'com.riis.cameraapp', `minSdkVersion` is set to 21, `targetSdkVersion` is set to 30, `versionCode` is set to 1, `multiDexEnabled` is set to true, `versionName` is set to "1.0" and more.

The `buildTypes` block is used to configure settings for different build types, such as release and debug. The `minifyEnabled` is set to false, and `proguardFiles` are set to the default ProGuard configuration file and a custom ProGuard configuration file.

The `compileOptions` block is used to configure the Java version for the project. The `sourceCompatibility` is set to `JavaVersion.VERSION_1_8` and the `targetCompatibility` is also set to `JavaVersion.VERSION_1_8`. The `kotlinOptions` block is used to configure the Kotlin version for the project. The `jvmTarget` is set to '1.8'. The `dexOptions` block is used to configure the maximum heap size for the project. The `javaMaxHeapSize` is set to "4g".

The `packagingOptions` block is used to configure settings for packaging the application. There are several `doNotStrip` options which are used to specify that certain native libraries should not be stripped during packaging. There is also an exclude option for the `META-INF/rxjava.properties` file.

The `dependencies` block is used to specify the dependencies for the application. The application depends on several libraries such as `androidx.documentfile:documentfile:1.0.1`, `androidx.multidex:multidex:2.0.0`, and `com.dji:dji-sdk:4.16`, `com.dji:dji-uxsdk:4.16`, `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2` and more. The dependencies are specified using the `implementation` and `compileOnly` configuration.

**Android Jetifier**

Please **add** the following line to the `gradle.properties` file
```kotlin
android.enableJetifier=true
```

#### settings.gradle
Please **replace everything** in the `settings.gradle` with
```kotlin
rootProject.name = "Camera-App"
include ':app'
```
