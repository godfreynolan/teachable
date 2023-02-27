# Creating an Augmented Reality HTML Overlay for the Camera App
## Pre-requisite Knowledge
***`WARNING: THIS TUTORIAL ASSUMES YOU'VE COMPLETED THE PREVIOUS TUTORIALS`***

This tutorial is designed for you to gain a basic understanding of the DJI Mobile SDK and a simple RTMP livestream. It will implement the FPV view and a basic overlay which displays street names and houses: 

You can download the tutorial's final sample project from this [Github Page](https://github.com/riis/teachable/tree/arCamera). <!-- Make sure to update when committed -->

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
```kotlin
DJI_API_KEY=<insert dji api key here>
```

### Building the Layouts of Activity
### 1. Creating the MApplication Class
In the project file navigator, go to app -> java -> com -> riis -> aroverlayapp, and right-click on the aroverlayapp directory. Select New -> Kotlin Class to create a new kotlin class and name it as MApplication.kt.

Then, open the MApplication.kt file and replace the content with the following:
```kotlin
package com.riis.aroverlayapp

import android.app.Application
import android.content.Context
import com.secneo.sdk.Helper

class MApplication: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Helper.install(this)
    }
}
```
Here we override the attachBaseContext() method to invoke the install() method of Helper class to load the SDK classes before using any SDK functionality. Failing to do so will result in unexpected crashes.

### 2. Implementing the MainActivity Class
The MainActivity.kt file is created by Android Studio by default. Let's replace its code with the following:
```kotlin
package com.riis.aroverlayapp

import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.TextureView
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dji.common.product.Model
import dji.sdk.base.BaseProduct
import dji.sdk.camera.Camera
import dji.sdk.camera.VideoFeeder
import dji.sdk.codec.DJICodecManager
import dji.sdk.flightcontroller.FlightController
import dji.sdk.products.Aircraft
import dji.sdk.products.HandHeld
import dji.sdk.sdkmanager.DJISDKManager
import android.os.Handler

/*
This activity provides an interface to access a connected DJI Product's camera and use
it to take photos and record videos
*/
class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, View.OnClickListener {
    //listener that is used to receive video data coming from the connected DJI product
    private var receivedVideoDataListener: VideoFeeder.VideoDataListener? = null
    private var codecManager: DJICodecManager? = null //handles the encoding and decoding of video data

    private var myAircraft: Aircraft = getProductInstance() as Aircraft
    private var myFlightController: FlightController = myAircraft.flightController
    private var pitch: Float = 0.0f;
    private var altitude: Float = 0.0f;

    //Creating the Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //inflating the activity_main.xml layout as the activity's view
        val webView: WebView = findViewById(R.id.ARview)
        webView.getSettings().setJavaScriptEnabled(true)
        webView.loadUrl("file:///android_asset/overlay.html")
        webView.setBackgroundColor(Color.TRANSPARENT);
        //We need to make a function that repeatedly loops and calls the javascript
        //While also obtaining the above data to send
        /*
        The receivedVideoDataListener receives the raw video data and the size of the data from the DJI product.
        It then sends this data to the codec manager for decoding.
        */
        receivedVideoDataListener = VideoFeeder.VideoDataListener { videoBuffer, size ->
            codecManager?.sendDataToDecoder(videoBuffer, size)
        }

        myFlightController.setStateCallback { controller ->
            altitude = controller.aircraftLocation.altitude
        }
        myAircraft.gimbal.setStateCallback { gimbalState ->
            if (gimbalState != null) {
                pitch = gimbalState.attitudeInDegrees.pitch
            }
        }

        var myHandler = Handler(Looper.getMainLooper())
        myHandler.post(object : Runnable {
            override fun run() {
                var lat = myFlightController.state.aircraftLocation.latitude;
                var lon = myFlightController.state.aircraftLocation.longitude;
                var heading = myFlightController.compass.heading;
                webView.loadUrl("javascript:change($altitude, $lat, $lon, $heading, $pitch)")
                myHandler.postDelayed(this, 50)
            }
        })
    }

    //Function that initializes the display for the videoSurface TextureView
    private fun initPreviewer() {

        //gets an instance of the connected DJI product (null if nonexistent)
        val product: BaseProduct = getProductInstance() ?: return

        //if DJI product is disconnected, alert the user
        if (!product.isConnected) {
            showToast(getString(R.string.disconnected))
        } else {
            /*
            if the DJI product is connected and the aircraft model is not unknown, add the
            receivedVideoDataListener to the primary video feed.
            */
            if (product.model != Model.UNKNOWN_AIRCRAFT) {
                receivedVideoDataListener?.let {
                    VideoFeeder.getInstance().primaryVideoFeed.addVideoDataListener(
                        it
                    )
                }
            }
        }
    }

    //Function that displays toast messages to the user
    private fun showToast(msg: String?) {
        runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
    }

    //When the MainActivity is created or resumed, initialize the video feed display
    override fun onResume() {
        super.onResume()
        initPreviewer()
    }

    //When a TextureView's SurfaceTexture is ready for use, use it to initialize the codecManager
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (codecManager == null) {
            codecManager = DJICodecManager(this, surface, width, height)
        }
    }

    //when a SurfaceTexture's size changes...
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    //when a SurfaceTexture is about to be destroyed, un-initialize the codedManager
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        codecManager?.cleanSurface()
        codecManager = null
        return false
    }

    //When a SurfaceTexture is updated...
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    //Handling what happens when certain layout views are clicked
    override fun onClick(v: View?) {}

    /*
    Note:
    Depending on the DJI product, the mobile device is either connected directly to the drone,
    or it is connected to a remote controller (RC) which is then used to control the drone.
    */

    //Function used to get the DJI product that is directly connected to the mobile device
    private fun getProductInstance(): BaseProduct? {
        return DJISDKManager.getInstance().product
    }

    /*
    Function used to get an instance of the camera in use from the DJI product
    */
    private fun getCameraInstance(): Camera? {
        if (getProductInstance() == null) return null

        return when {
            getProductInstance() is Aircraft -> {
                (getProductInstance() as Aircraft).camera
            }
            getProductInstance() is HandHeld -> {
                (getProductInstance() as HandHeld).camera
            }
            else -> null
        }
    }
}
```
The purpose of this code is to provide an interface for an Android application that allows the user to access and control a connected DJI product's camera. The code uses the DJI SDK to interact with the camera and receives video data from the DJI product and sends it to the codec manager for decoding. The `MainActivity` class implements the `TextureView.SurfaceTextureListener` and `View.OnClickListener` interfaces and overrides their methods to handle the display and handling of the video feed.

Here are explanations of some of the key functions in the code:

- `onCreate()` function: This function is called when the activity is first created. It sets the layout for the activity by inflating the activity_main.xml layout and it also sets up the receivedVideoDataListener which receives the raw video data from the DJI product and sends it to the codec manager for decoding.

- `initPreviewer()` function: This function initializes the display for the videoSurface TextureView. It gets an instance of the connected DJI product, and if the product is not connected, it alerts the user. If the product is connected, it adds the receivedVideoDataListener to the primary video feed.

- `onResume()` function: This function is called when the activity is resumed after being paused or stopped. It calls the initPreviewer function to initialize the video feed display.

- `onSurfaceTextureAvailable()` function: This function is called when the TextureView's SurfaceTexture is ready for use. It uses the SurfaceTexture to initialize the codecManager.

- `onSurfaceTextureDestroyed()` function: This function is called when a SurfaceTexture is about to be destroyed. It un-initializes the codecManager to release the resources.

- `onClick()` function: This function handles what happens when certain layout views are clicked. In this example it is empty but it can be used to handle user actions such as taking photos or recording videos.

- Handler/Runnable: This is a function that obtains information from the DJI drone and calls the Javascript function to change details of Mapbox, which will be added. It should be noted that the statecallback functions before hand are set once and the variables will be updated dynamically and implicitly. (Small Note: In high winds, the gimbal might be pushed, producing inaccurate results. Use a drone with a gimbal shield in high winds or change the heading to be that of the gimbal.) 

### 3. Implementing the MainActivity Layout
Open the activity_main.xml layout file and replace the code with the following:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
xmlns:custom="http://schemas.android.com/apk/res-auto"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="horizontal"
tools:context=".MainActivity">

<!-- Widget to see first person view (FPV) -->
 <RelativeLayout
     android:id="@+id/fpv_container"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     android:gravity="center">
     <dji.ux.widget.FPVWidget
         android:layout_width="match_parent"
         android:layout_height="match_parent"
         android:layout_gravity="center"
         android:layout_marginBottom="-2dp"/>
 </RelativeLayout>

 <!-- Web view for HTML AR Overlay -->
 <WebView
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     android:id="@+id/ARview"/>

<dji.ux.widget.FPVOverlayWidget
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

<dji.ux.workflow.CompassCalibratingWorkFlow
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

<!-- Widgets in top status bar -->
<LinearLayout
    android:id="@+id/signal"
    android:layout_width="match_parent"
    android:layout_height="25dp"
    android:background="@color/dark_gray"
    android:orientation="horizontal">

 <dji.ux.widget.GPSSignalWidget
     android:layout_width="44dp"
     android:layout_height="22dp"/>

 <dji.ux.widget.VisionWidget
     android:layout_width="22dp"
     android:layout_height="22dp"/>

 <dji.ux.widget.RemoteControlSignalWidget
     android:layout_width="38dp"
     android:layout_height="22dp"/>

 <dji.ux.widget.VideoSignalWidget
     android:layout_width="38dp"
     android:layout_height="22dp"/>

 <dji.ux.widget.WiFiSignalWidget
     android:layout_width="22dp"
     android:layout_height="20dp"/>

 <dji.ux.widget.BatteryWidget
     android:layout_width="96dp"
     android:layout_height="22dp"
     custom:excludeView="singleVoltage"/>

 <dji.ux.widget.ConnectionWidget
     android:layout_marginTop="3dp"
     android:layout_width="18dp"
     android:layout_height="18dp"/>
</LinearLayout>

 <!--Take off and return home buttons on left -->
 <LinearLayout
     android:layout_width="40dp"
     android:layout_height="wrap_content"
     android:layout_centerVertical="true"
     android:layout_marginStart="12dp"
     android:orientation="vertical">

  <dji.ux.widget.TakeOffWidget
      android:layout_width="40dp"
      android:layout_height="40dp"
      android:layout_marginBottom="12dp"/>

  <dji.ux.widget.ReturnHomeWidget
      android:layout_width="40dp"
      android:layout_height="40dp"
      android:layout_marginTop="12dp"/>
 </LinearLayout>
</RelativeLayout>
```
In the xml file, we created each widget to access the DJI UXSDK widget elements for the app to use. More widgets can be found on the DJI UXSDK documentation page. Most importantly, we add the `<dji.ux.widget.FPVWidget/>`, which allows the application to see the FPV of the camera from the drone. Normally this would be done with a textureView however the UX widget allows us to skip that step.

Furthermore, we add a webview to contain the future HTML overlay which will be implemented in the next step. 

### 4. Creating HTML Files
To create the overlay, we need to create an HTML file to access MapBox, an API used in javascript to display house and street numbers. First create a folder named "assets" and put it inside of the same directory the "res" folder is in. Inside of the "assets" directory, create 2 files. 
1. local.css
2. overlay.html

Edit overlay.html and add the following 
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Display buildings in 3D</title>
    <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no"/>
    <link href="https://api.mapbox.com/mapbox-gl-js/v2.10.0/mapbox-gl.css" rel="stylesheet"/>
    <link href="local.css" rel="stylesheet" />
    <script src="https://api.mapbox.com/mapbox-gl-js/v2.10.0/mapbox-gl.js"></script>
</head>
<body>
<div id="map"></div>
<script>
mapboxgl.accessToken =
  "pk.eyJ1IjoidG1heSIsImEiOiJjbDExMzEwM3AwM2FpM2lwNTVjd2k3bHFxIn0.6wBbnKZx-nYTno4oyXl-og";
const map = new mapboxgl.Map({
  style: 'mapbox://styles/riis/cl9yoplca002d15r76m7bwaeh',
  center: [-82.3623292777778, 27.44881725],
  zoom: 19.6612483141484,
  maxPitch: 80,
  unit: 'metric',
  // pitch: 9.3,
  bearingSnap:0,
  bearing: -96.4,
  container: "map",
  antialias: true,
});
function updateCameraPosition(position, altitude, target) {
  const camera = map.getFreeCameraOptions();
  camera.position = mapboxgl.MercatorCoordinate.fromLngLat(position,altitude);
  camera.lookAtPoint(target);
  map.setFreeCameraOptions(camera);
}
map.once("load", () => {
  updateCameraPosition([-82.3623292777778, 27.44881725], 30, [-82.3623292777778, 27.44881725]);
  const marker = new mapboxgl.Marker()
    .setLngLat([-82.3623292777778, 27.44881725])
    .addTo(map);
  map.setBearing(-96.4);
});
function change(droneHeight, droneLat, droneLong, droneHeading, gimbalPitch){
  let d = distance(droneHeight, gimbalPitch + 90);
  let target = destinationPoint(Number(droneLat), Number(droneLong), d, droneHeading, radius=6371e3);
  updateCameraPosition([droneLong, droneLat], droneHeight, [String(target[0]),String(target[1])]);
  map.setBearing(droneHeading);
}
function distance(RelativeAltitude, pitch){
  return Math.tan(pitch * Math.PI / 180) * RelativeAltitude;
}
function destinationPoint(lat, lon, distance, bearing, radius=6371e3) {
  const δ = distance / radius; // angular distance in radians
  const θ = Number(bearing).toRadians();
  const φ1 = lat.toRadians(), λ1 = lon.toRadians();
  const sinφ2 = Math.sin(φ1) * Math.cos(δ) + Math.cos(φ1) * Math.sin(δ) * Math.cos(θ);
  const φ2 = Math.asin(sinφ2);
  const y = Math.sin(θ) * Math.sin(δ) * Math.cos(φ1);
  const x = Math.cos(δ) - Math.sin(φ1) * sinφ2;
  const λ2 = λ1 + Math.atan2(y, x);
  const lt = φ2.toDegrees();
  const ln = λ2.toDegrees();
  return [ln,lt]
}
// Extend Number object with methods to convert between degrees & radians
Number.prototype.toRadians = function() { return this * Math.PI / 180; };
Number.prototype.toDegrees = function() { return this * 180 / Math.PI; };
</script>
</body>
</html>
```

Here in the HTML we add the mapbox api and initialize it to provide the map. Change() is one of the most important functions here as it updates the map whenever the function is called with the values provided. Change is called from our main activity to update the map with the location and data of the drone. 

Many of the functions here use the pythagorean theorem to determine where the point is in space the drone is looking at by calculating the distance. 

Edit local.css and add the following 
```css
body {
  margin: 0;
  padding: 0;
}
#map {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index:100;

  /* use opacity while comparing satellite view with drone image */
  opacity: 0.7;
}
#container {
    width: 100vw;
    height: 100vh;
  position: relative;
}
#vid {
  /* position: absolute;
  top: 0;
  left: 0; */
  display: block;
  margin: auto;
  z-index: -100;
  height:100vh;
  text-align: center;
  /* width: 100vw; */
}
```

A simple CSS file which turns the map transparent so the camera underneath may be seen. Since the CSS is simple, it will not be explained inside of this tutorial. 

### 4. Implementing the ConnectionActivity Class
To improve the user experience, we had better create an activity to show the connection status between the DJI Product and the SDK, once it's connected, the user can press the OPEN button to enter the MainActivity.

In the project navigator, go to app -> java -> com -> riis -> fpv, and right-click on the fpv directory. Select New -> Kotlin Class/File to create a new kotlin class and name it as ConnectionActivity.kt.

Next, replace the code of the ConnectionActivity.kt file with the following:
```kotlin
package com.riis.aroverlayapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.riis.aroverlayapp.R
import dji.sdk.sdkmanager.DJISDKManager

/*
This activity manages SDK registration and establishing a connection between the
DJI product and the user's mobile phone.
 */
class ConnectionActivity : AppCompatActivity() {

    //Class Variables
    private lateinit var mTextConnectionStatus: TextView
    private lateinit var mTextProduct: TextView
    private lateinit var mTextModelAvailable: TextView
    private lateinit var mBtnOpen: Button
    private lateinit var mVersionTv: TextView

    private val model: ConnectionViewModel by viewModels() //linking the activity to a viewModel

    companion object {
        const val TAG = "ConnectionActivity"
    }

    //Creating the Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inflating the activity_connection.xml layout as the activity's view
        setContentView(R.layout.activity_connection)

        /*
        Request the following permissions defined in the AndroidManifest.
        1 is the integer constant we chose to use when requesting app permissions
        */
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.VIBRATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.READ_PHONE_STATE
            ), 1)

        //Initialize the UI, register the app with DJI's mobile SDK, and set up the observers
        initUI()
        model.registerApp()
        observers()
    }

    //Function to initialize the activity's UI
    private fun initUI() {

        //referencing the layout views using their resource ids
        mTextConnectionStatus = findViewById(R.id.text_connection_status)
        mTextModelAvailable = findViewById(R.id.text_model_available)
        mTextProduct = findViewById(R.id.text_product_info)
        mBtnOpen = findViewById(R.id.btn_open)
        mVersionTv = findViewById(R.id.textView2)

        //Getting the DJI SDK version and displaying it on mVersionTv TextView
        mVersionTv.text = resources.getString(R.string.sdk_version, DJISDKManager.getInstance().sdkVersion)

        mBtnOpen.isEnabled = false //mBtnOpen Button is initially disabled

        //If mBtnOpen Button is clicked on, start MainActivity (only works when button is enabled)
        mBtnOpen.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //Function to setup observers
    private fun observers() {
        //observer listens to changes to the connectionStatus variable stored in the viewModel
        model.connectionStatus.observe(this, Observer<Boolean> { isConnected ->
            //If boolean is True, enable mBtnOpen button. If false, disable the button.
            if (isConnected) {
                mTextConnectionStatus.text = "Status: Connected"
                mBtnOpen.isEnabled = true
            }
            else {
                mTextConnectionStatus.text = "Status: Disconnected"
                mBtnOpen.isEnabled = false
            }
        })

        /*
        Observer listens to changes to the product variable stored in the viewModel.
        product is a BaseProduct object and represents the DJI product connected to the mobile device
        */
        model.product.observe(this, Observer { baseProduct ->
            //if baseProduct is connected to the mobile device, display its firmware version and model name.
            if (baseProduct != null && baseProduct.isConnected) {
                mTextModelAvailable.text = baseProduct.firmwarePackageVersion

                //name of the aircraft attached to the remote controller
                mTextProduct.text = baseProduct.model.displayName
            }
        })
    }
}
```
In the code shown above, we implement the following:

1. Create the layout UI elements variables, including four TextViews `mTextConnectionStatus`, `mTextProduct`, `mTextModelAvailable`, `mVersionTv` and one Button `mBtnOpen`.

2. Link the activity to a ViewModel that stores the connection state and DJI SDK functions

3. In the `onCreate()` method, we request all the neccessary permissions for this application to work using the `ActivityCompat.requestPermissions()` method. We then invoke the `initUI()` method to initialize the four TextViews and the Button. We also setup the observers for this activity using the `observers()` method.

4. In the `initUI()` method, The `mBtnOpen` button is initially diabled. We invoke the `setOnClickListener()` method of `mBtnOpen` and set the Button's click action to start the MainActivity (only works when button is enabled). The `mVersionTv` TextView is set to display the DJI SDK version.

5. In the `observers()` method, we are observing changes (from the ViewModel) to the connection state between app and the DJI product as well as any changes to the product itself. Based on this, the `mTextConnectionStatus` will display the connection status, `mTextProduct` will the display the product's name, and `mTextModelAvailable` will display the DJI product's firmware version. If a DJI product is connected, the `mBtnOpen` Button becomes enabled.

### 5. Implementing the ConnectionActivity Layout
Open the `activity_connection.xml` layout file and replace the code with the following:
```xml
<?xml version="1.0" encoding="utf-8"?>  
<RelativeLayout  
  xmlns:android="http://schemas.android.com/apk/res/android"  
  xmlns:tools="http://schemas.android.com/tools"  
  xmlns:app="http://schemas.android.com/apk/res-auto"  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"  
  tools:context=".ConnectionActivity">  
  
 <TextView  android:id="@+id/text_connection_status"  
  android:layout_width="wrap_content"  
  android:layout_height="wrap_content"  
  android:layout_alignBottom="@+id/text_product_info"  
  android:layout_centerHorizontal="true"  
  android:layout_marginBottom="89dp"  
  android:gravity="center"  
  android:text="Status: No Product Connected"  
  android:textColor="@android:color/black"  
  android:textSize="20dp"  
  android:textStyle="bold" />  
  
 <TextView  android:id="@+id/text_product_info"  
  android:layout_width="wrap_content"  
  android:layout_height="wrap_content"  
  android:layout_centerHorizontal="true"  
  android:layout_marginTop="270dp"  
  android:text="@string/product_information"  
  android:textColor="@android:color/black"  
  android:textSize="20dp"  
  android:gravity="center"  
  android:textStyle="bold"  
  />  
  
 <TextView  android:id="@+id/text_model_available"  
  android:layout_width="match_parent"  
  android:layout_height="wrap_content"  
  android:layout_centerHorizontal="true"  
  android:gravity="center"  
  android:layout_marginTop="300dp"  
  android:text="@string/model_not_available"  
  android:textSize="15dp"/>  
  
 <Button  android:id="@+id/btn_open"  
  android:layout_width="150dp"  
  android:layout_height="55dp"  
  android:layout_centerHorizontal="true"  
  android:layout_marginTop="350dp"  
  android:background="@drawable/round_btn"  
  android:text="Open"  
  android:textColor="@color/colorWhite"  
  android:textSize="20dp"  
  />  
  
 <TextView  android:layout_width="wrap_content"  
  android:layout_height="wrap_content"  
  android:layout_centerHorizontal="true"  
  android:layout_marginTop="430dp"  
  android:text="@string/sdk_version"  
  android:textSize="15dp"  
  android:id="@+id/textView2" />  
  
 <TextView  android:id="@+id/textView"  
  android:layout_width="wrap_content"  
  android:layout_height="wrap_content"  
  android:layout_marginTop="58dp"  
  android:text="@string/app_name"  
  android:textAppearance="?android:attr/textAppearanceSmall"  
  android:textColor="@color/black_overlay"  
  android:textSize="20dp"  
  android:textStyle="bold"  
  android:layout_alignParentTop="true"  
  android:layout_centerHorizontal="true" />  
  
</RelativeLayout>
```
In the xml file, we create four TextViews and one Button within a RelativeLayout. We use the `TextView(id:` `text_connection_status)` to show the product connection status and use the `TextView(id:text_product_info)` to show the connected product name. The `Button(id: btn_open)` is used to open the **MainActivity**.

### 6. Implementing the ConnectionViewModel Class
To store important variables and functions needed for mobile SDK registration and connection to the DJI product, an AndroidViewModel class is needed. This allows the app to maintain its connection state across rotation death.

In the project navigator, go to **app -> java -> com -> riis -> fpv**, and right-click on the fpv directory. Select **New -> Kotlin Class/File** to create a new kotlin class and name it as `ConnectionViewModel.kt`.

Next, replace the code of the `ConnectionViewModel.kt` file with the following:
```kotlin
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager

/*
This ViewModel stores important variables and functions needed for mobile SDK registration
and connection to the DJI product. This allows the app to maintain its connection state
across rotation death.
 */
class ConnectionViewModel(application: Application) : AndroidViewModel(application) {

    //product is a BaseProduct object which stores an instance of the currently connected DJI product
    val product: MutableLiveData<BaseProduct?> by lazy {
        MutableLiveData<BaseProduct?>()
    }

    //connectionStatus boolean describes whether or not a DJI product is connected
    val connectionStatus: MutableLiveData<Boolean> = MutableLiveData(false)

    //DJI SDK app registration
    fun registerApp() {
        /*
        Getting an instance of the DJISDKManager and using it to register the app
        (requires API key in AndroidManifest). After installation, the app connects to the DJI server via
        internet and verifies the API key. Subsequent app starts will use locally cached verification
        information to register the app when the cached information is still valid.
        */
        DJISDKManager.getInstance().registerApp(getApplication(), object: DJISDKManager.SDKManagerCallback {
            //Logging the success or failure of the registration
            override fun onRegister(error: DJIError?) {
                if (error == DJISDKError.REGISTRATION_SUCCESS) {
                    Log.i(ConnectionActivity.TAG, "onRegister: Registration Successful")
                } else {
                    Log.i(ConnectionActivity.TAG, "onRegister: Registration Failed - ${error?.description}")
                }
            }
            //called when the remote controller disconnects from the user's mobile device
            override fun onProductDisconnect() {
                Log.i(ConnectionActivity.TAG, "onProductDisconnect: Product Disconnected")
                connectionStatus.postValue(false) //setting connectionStatus to false
            }
            //called when the remote controller connects to the user's mobile device
            override fun onProductConnect(baseProduct: BaseProduct?) {
                Log.i(ConnectionActivity.TAG, "onProductConnect: Product Connected")
                product.postValue(baseProduct)
                connectionStatus.postValue(true) //setting connectionStatus to true
            }
            //called when the DJI aircraft changes
            override fun onProductChanged(baseProduct: BaseProduct?) {
                Log.i(ConnectionActivity.TAG, "onProductChanged: Product Changed - $baseProduct")
                product.postValue(baseProduct)

            }
            //Called when a component object changes. This method is not called if the component is already disconnected
            override fun onComponentChange(componentKey: BaseProduct.ComponentKey?, oldComponent: BaseComponent?, newComponent: BaseComponent?) {
                //Alert the user which component has changed, and mention what new component replaced the old component (can be null)
                Log.i(ConnectionActivity.TAG, "onComponentChange key: $componentKey, oldComponent: $oldComponent, newComponent: $newComponent")

                //Listens to connectivity changes in each new component
                newComponent?.let { component ->
                    component.setComponentListener { connected ->
                        Log.i(ConnectionActivity.TAG, "onComponentConnectivityChange: $connected")
                    }
                }
            }
            //called when loading SDK resources
            override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {}

            //Called when Fly Safe database download progress is updated
            override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {}
        })
    }
}
```
Here, we implement several features:

* variable product is used to store an instance of the currently connected DJI product
* variable connectionStatus describes whether or not a DJI product is connected
* The app is registered with the DJI SDK and an instance of `SDKManagerCallback` is initialized to provide feedback from the SDK.
* Four interface methods of `SDKManagerCallback` are used. The `onRegister()` method is used to check the Application registration status and show text message here. When the product is connected or disconnected, the `onProductConnect()` and `onProductDisconnect()` methods will be invoked. Moreover, we use the `onComponentChange()` method to check the component changes.
~~~~
Note: Permissions must be requested by the application and granted by the user in order to register the DJI SDK correctly. This is taken care of in ConnectionActivity before it calls on the ViewModel's registerApp() method. Furthermore, the camera and USB hardwares must be declared in the AndroidManifest for DJI SDK to work.
~~~~

### 8. Configuring the Resource XMLs
Once you finish the above steps, let's copy all the images (xml files) from this Github project's drawable folder **(app -> res -> drawable)** to the same folder in your project. Their names can be found below. 
* round_btn.xml
* round_btn_disable.xml
* round_btn_normal.xml
* rount_btn_pressed.xml
These images are used in the ConnectionActivity class and must be imported otherwise the project will not build. 
Moreover, open the `colors.xml` file and update the content as shown below:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="black_overlay">#000000</color>
    <color name="colorWhite">#FFFFFF</color>
    <color name="background_blue">#242d34</color>
    <color name="transparent">#00000000</color>
    <color name="dark_gray">#80000000</color>
</resources>
```
Furthermore, open the `strings.xml` file and replace the content with the following:
```xml
<resources>  
 <string name="app_name">DJIFPV-Kotlin</string>  
 <string name="action_settings">Settings</string>  
 <string name="disconnected">Disconnected</string>  
 <string name="product_information">Product Information</string>  
 <string name="connection_loose">Status: No Product Connected</string>  
 <string name="model_not_available">Model Not Available</string>  
 <string name="push_info">Push Info</string>  
 <string name="sdk_version">DJI SDK Version: %1$s</string>  
</resources>
```
Lastly, create `styles.xml` and replace the content with the following:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="status_text">
        <item name="android:shadowColor">@color/black_overlay</item>
        <item name="android:shadowDx">2</item>
        <item name="android:shadowDy">1</item>
        <item name="android:shadowRadius">6</item>
        <item name="android:textSize">17sp</item>
        <item name="android:textColor">@color/white</item>
    </style>
</resources>
```

## Registering the Application
After you finish the above steps, let's register our application with the App Key you obtain from the DJI Developer Website. If you are not familiar with the App Key, please check the [Get Started](https://developer.dji.com/mobile-sdk/documentation/quick-start/index.html).

1. Let's open the `AndroidManifest.xml` file and specify the permissions that your application needs by adding `<uses-permission>` elements into the `<manifest>` element of the `AndroidManifest.xml` file. We also need to declare the camera and USB hardwares using `<uses-feature>` child elements since they will be used by the application.
2. Next, add `android:name=".MApplication"` inside of the `<application>` element in the `AndroidManifest.xml` file
3. Moreover, let's add the following elements as childs of the `<application>` element, right on top of the "ConnectionActivity" `<activity>` element as shown below
4. In the code above, you should substitute your App Key of the application for "Please enter your App Key here." in the value attribute under the `android:name="com.dji.sdk.API_KEY` attribute.
5. Lastly, update the "MainActivity" and "ConnectionActivity" `<activity>` elements as shown below:
   
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.riis.aroverlayapp">

    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
        tools:ignore="ProtectedPermissions" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        tools:ignore="ScopedStorage" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature
        android:name="android.hardware.usb.host"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.usb.accessory"
        android:required="true" />

    <application
        android:name="com.riis.aroverlayapp.MApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.aroverlayapp">

        <!-- DJI SDK -->
        <uses-library android:name="com.android.future.usb.accessory" />
        <uses-library
            android:name="org.apache.http.legacy"
            android:required="false" />
        <meta-data
            android:name="com.dji.sdk.API_KEY"
            android:value="${DJI_API_KEY}" />
        <!-- DJI SDK -->

        <activity
            android:name=".ConnectionActivity"
            android:screenOrientation="portrait"
            android:launchMode="singleTop"
            android:configChanges="orientation"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />
            </intent-filter>
            <meta-data
                android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED"
                android:resource="@xml/accessory_filter"/>
        </activity>

        <activity android:name=".MainActivity"
            android:screenOrientation="userLandscape"
            android:exported="true"/>
    </application>

</manifest>
```
In the code above, we add the attributes of `android:screenOrientation` to set "ConnectionActivity" as **portrait** and set "MainActivity" as **landscape**.

We must now add the accessory filter file to the project. With this file, the app can determine what devices are being plugged into the Android phone. Create a new Directory under **app/res/** called `xml`, if one has not already been made. Then, right click the newly created folder and create a new **XML Resource File** called `accessory_filter.xml`. Then press **OK**. Inside this resource file, replace all pre-existing code with the following code. The user will now be prompted to open the app when DJI controllers are plugged in.
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-accessory model="T600" manufacturer="DJI"/>
    <usb-accessory model="AG410" manufacturer="DJI"/>
    <usb-accessory model="com.dji.logiclink" manufacturer="DJI"/>
    <usb-accessory model="WM160" manufacturer="DJI"/>
</resources>
```

One final task must be completed before the DJI drone is able to be connected to the mobile device. In your `local.properties` gradle file, add the following line, however replace `"INSERT API KEY HERE"` with the API key you obtained from the previous steps. 
```gradle
DJI_API_KEY="INSERT API KEY HERE"
```
~~~~
Congratulations! Your Aerial FPV android app is complete, you can now use this app to control the camera of your DJI Product now.
~~~~

## Summary And Notes
In this tutorial, you’ve learned how to use DJI Mobile SDK to show the FPV View from the aircraft's camera and provide an overlay which uses the drone's location, heading, and gimbal information to project street names and house numbers on to the screen. It should be noted that sometimes the street names and house numbers will be slightly off, this is due to the FOV of the mapbox element as well as discrepencies in height comparitively to sea level. Furthermore, the amount of gitter in the program is entirely dependent on the drone's GPS signal strength. In the future, a smoother transition could be provided to new points in the javascript code so that the map gitter is not an issue. 

## License
MIT

