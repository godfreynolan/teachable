# Creating a Camera App w/ Live Streaming Tutorial
## Pre-requisite Knowledge
***`WARNING: THIS TUTORIAL ASSUMES YOU'VE COMPLETED THE PREVIOUS TUTORIALS`***

This tutorial is designed for you to gain a basic understanding of the DJI Mobile SDK and a simple RTMP livestream. It will implement the FPV view and two basic camera functionalities: 
- Take Photo
- Record video.
- Livestream your screen

You can download the tutorial's final sample project from this [Github Page](https://github.com/riis/teachable/tree/livestream). <!-- Make sure to update when committed -->

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
    * Set the **Application name** to your desired app name. In this example we will use `AR-Overlay-App`.
    * The **Package name** is conventionally set to something like "com.companyName.applicationName". We will use `com.riis.aroverlayapp`.
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
    // Declare variable to store the kotlin version
    ext.kotlin_version = '1.6.10'
    repositories {
        // Add Google's Maven repository as a dependency source
        google()
        // Add Maven Central as a dependency source
        mavenCentral()
    }
    dependencies {
        // Add Android gradle plugin as a dependency
        classpath 'com.android.tools.build:gradle:7.0.4'
        // Add Kotlin gradle plugin as a dependency
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        // Note: Do not place your application dependencies here; they belong in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        // Add Google's Maven repository as a dependency source
        google()
        // Add Jcenter as a dependency source
        jcenter()
        // Add Maven Central as a dependency source
        mavenCentral()
        // Add JitPack repository as a dependency source
        maven { url 'https://jitpack.io' }
    }
}

// create a gradle task that delete the build directory when executed
task clean(type: Delete) {
    // delete the root project build directory
    delete rootProject.buildDir
}
```

**build.gradle (Module)**
Please replace **everything** in the `build.gradle (Module)` with:

```kotlin
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-android-extensions'
    id 'kotlin-kapt'
}

Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())
def djiKey = properties.getProperty('DJI_API_KEY')

android {
    namespace 'com.riis.aroverlayapp'
    compileSdkVersion 31
    buildToolsVersion "30.0.3"
    dataBinding {
        enabled = true
    }

    defaultConfig {
        applicationId 'com.riis.aroverlayapp'
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        multiDexEnabled true
        manifestPlaceholders = [DJI_API_KEY: djiKey]
        buildConfigField "String", "TWITCH_KEY", properties['TWITCH_KEY']
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
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.fragment:fragment-ktx:1.2.4")

    implementation 'com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:2.2.2'
    implementation 'org.greenrobot:eventbus:3.2.0'

    //Default
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.3.5'
    implementation 'androidx.navigation:navigation-ui-ktx:2.3.5'
    implementation 'androidx.annotation:annotation:1.2.0'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.3.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.2'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
```
This code is a build script for an Android app. It defines the app's build configuration, including dependencies, build types, and packaging options.

In the first line, it applies 4 plugins:

    'com.android.application': the core plugin for building Android apps
    'kotlin-android': a plugin for working with the Kotlin programming language in Android apps
    'kotlin-android-extensions': a plugin that provides convenient extension properties for Android views
    'kotlin-kapt': a plugin for annotation processing in Kotlin

Then it loads a properties file named "local.properties" from the root project, and gets the property named "DJI_API_KEY" from this file.

In the android {} block, it sets up various configurations for the Android app:

    namespace: package name of the app
    compileSdkVersion, buildToolsVersion: versions of the Android SDK and build tools used for building the app
    dataBinding.enabled: whether to enable data binding in the app
    defaultConfig: various default settings for the app, such as the application ID, SDK versions, version code and name, test instrumentation runner, etc.
    buildTypes: settings for different build types of the app, such as release and debug
    compileOptions, kotlinOptions: settings for the Java and Kotlin compilers
    dexOptions: settings for the dex compiler
    packagingOptions: settings for the APK packaging process, such as which files to exclude or not to strip

Finally, in the dependencies {} block, it specifies the dependencies that the app needs, such as the DJI SDK, androidx libraries, and other libraries.

**Android Jetifier**

Please **add** the following line to the `gradle.properties` file
```kotlin
android.enableJetifier=true
```

#### settings.gradle
Please **replace everything** in the `settings.gradle` with
```kotlin
rootProject.name = "AR-Overlay-App"
include ':app'
```

#### local.properties
Please add your API keys here to keep them private by adding the following lines in `local.properties`
Please replace `<insert api key here>` with your actual API key.
For more help with getting your twitch key look [here](https://restream.io/integrations/twitch/how-to-find-twitch-stream-key/)
```
DJI_API_KEY=<insert api key here>
TWITCH_KEY="<insert api key here>"
```
