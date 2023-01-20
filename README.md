# Creating a Camera App w/ Live Streaming Tutorial
## Pre-requisite Knowledge
***`WARNING: THIS TUTORIAL ASSUMES YOU'VE COMPLETED THE PREVIOUS TUTORIALS`***

This tutorial is designed for you to gain a basic understanding of the DJI Mobile SDK and a simple RTMP livestream. It will implement the FPV view and two basic camera functionalities: 
- Take Photo
- Record video.
- Livestrean your screen

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
    namespace 'com.riis.cameraapp'
    compileSdkVersion 31
    buildToolsVersion "30.0.3"
    dataBinding {
        enabled = true
    }

    defaultConfig {
        applicationId 'com.riis.cameraapp'
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
rootProject.name = "Camera-App"
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

### Building the Layouts of Activity
### 1. Creating the MApplication Class
In the project file navigator, go to app -> java -> com -> riis -> fpv, and right-click on the fpv directory. Select New -> Kotlin Class to create a new kotlin class and name it as MApplication.kt.

Then, open the MApplication.kt file and replace the content with the following:
```kotlin
package com.riis.cameraapp

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
package com.riis.cameraapp

import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dji.sdk.sdkmanager.DJISDKManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        
        // hide the support action bar
        supportActionBar?.hide()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // check if the intent action is USB_ACCESSORY_ATTACHED
        val action = intent?.action
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == action) {
            // create a new intent with the action USB_ACCESSORY_ATTACHED
            val attachedIntent = Intent()
            attachedIntent.action = DJISDKManager.USB_ACCESSORY_ATTACHED
            // broadcast the intent
            sendBroadcast(attachedIntent)
        }
    }
}
```
This code defines an Android activity called MainActivity that is part of the package com.riis.cameraapp. The activity is a subclass of AppCompatActivity, which is a class from the Android support library that provides compatibility with older versions of Android.

The onCreate method is called when the activity is first created. In this method, the activity sets its layout by calling setContentView(R.layout.main_activity). The layout is defined in a file called main_activity.xml in the res/layout directory. The supportActionBar?.hide() line hides the action bar of the activity.

The onNewIntent method is called when a new intent is delivered to the activity. In this method, the code first checks if the intent's action is UsbManager.ACTION_USB_ACCESSORY_ATTACHED. If it is, it creates a new intent with the action DJISDKManager.USB_ACCESSORY_ATTACHED and sends it as a broadcast. This broadcast is used by the DJI SDK to handle the USB accessory being attached.

### 3. Implementing the MainActivity Layout
Open the `main_activity.xml` layout file and replace the code with the following:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/main_nav_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="true"
        app:navGraph="@navigation/main_nav_graph" />
</FrameLayout>
```
This is an XML layout file for an Android app that uses the FrameLayout container. It sets up a FragmentContainerView, which is a view group that can contain fragments, and sets its id to "main_nav_fragment". The FragmentContainerView's width and height are set to match the parent's, and the app:defaultNavHost and app:navGraph attributes are set to "true" and "@navigation/main_nav_graph" respectively. This is likely used to navigate between different fragments within the app using the Navigation component provided by Android Jetpack. The xmlns attributes define namespaces for different tools and resources used in the layout file.

### 4. Implementing the Live stream code
It's time to implement the live stream part of our app. We're going to be implementing several Kotlin classes in addition to a few xml files.

> Note: It will be normal to come across errors in your code, don't start debugging until everything from the tutorial is done.

In the project navigator, go to app -> java -> com -> riis -> cameraapp, and right-click on the cameraapp directory. Select New -> Package to create a new package and name it as `main` so that the structure looks like `com.riis.cameraapp.main`.

#### MainFragment.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> main, and right-click on the main directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `MainFragment`.

Next, replace the code of the `MainFragment.kt` file with the following:
```kotlin
package com.riis.cameraapp.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.riis.cameraapp.R
import com.riis.cameraapp.databinding.MainFragmentBinding
import dji.keysdk.KeyManager
import dji.keysdk.ProductKey
import kotlinx.android.synthetic.main.main_fragment.view.*

class MainFragment : Fragment() {
    private val missingPermissions = ArrayList<String>()
    private val requestPermissionCode = 12345
    private val requiredPermissionList = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECORD_AUDIO
    )
    private val firmKey = ProductKey.create(ProductKey.FIRMWARE_PACKAGE_VERSION)

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLiveData()
        checkAndRequestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestPermissionCode) {
            for (i in grantResults.indices.reversed()) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.remove(permissions[i])
                }
            }
        }

        if (missingPermissions.isEmpty()) {
            viewModel.registerDJI(requireActivity())
        } else {
            Toast.makeText(requireActivity(), "Missing permissions!!!", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun checkAndRequestPermissions() {
        for (eachPermission in requiredPermissionList) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    eachPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(eachPermission)
            }
        }

        if (missingPermissions.isEmpty()) {
            viewModel.registerDJI(requireActivity())
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                missingPermissions.toTypedArray(),
                requestPermissionCode
            )
        }
    }

    private fun setUpLiveData() {
        viewModel.notifyStatusChanged.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            refreshSDKRelativeUI()
        })

        viewModel.progressToVideo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                requireView().findNavController().navigate(R.id.action_main_to_video)
                viewModel.progressToVideo.value = false
            }
        })

        viewModel.promptLogin.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.loginDJIUserAccount(requireContext())
                viewModel.promptLogin.value = false
            }
        })
    }

    private fun refreshSDKRelativeUI() {
        val mProduct = viewModel.product

        val view = requireView()
        if (null != mProduct && mProduct.isConnected) {
            view.open_button.isEnabled = true

            view.connection_status_text_view.text = "Status: Connected"

            if (null != mProduct.model) {
                view.product_info_text_view.text = mProduct.model.displayName
            } else {
                view.product_info_text_view.text = "Product Information"
            }

            if (KeyManager.getInstance() != null) {
                KeyManager.getInstance().removeKey(firmKey)
                KeyManager.getInstance().addListener(firmKey) { _, _ ->
                }
            }
        } else {
            view.open_button.isEnabled = false
            view.product_info_text_view.text = "Product Information"
            view.connection_status_text_view.text = "Status: Not Connected"
        }
    }
}
```
So this is a class in an Android app that handles the main functionality of the app. The class is called "MainFragment" and it is a type of Fragment, which is a kind of Android class that represents a portion of the UI in an app.

First, the class imports several necessary Android classes and one class from a package called "dji." These imports are necessary for the class to be able to access certain features or functionality in the app.

Next, the class declares several variables. One of these is an ArrayList of missing permissions, which is used to keep track of any permissions that the app needs but the user has not yet granted. Another variable is a request permission code, which is used when requesting permissions from the user. And there's an array of required permissions, which lists all the permissions that the app needs to function properly. These required permissions include things like access to the device's location, storage, and microphone.

The class also overrides several methods from the Fragment class. The "onCreateView" method is called when the fragment's UI is being created. In this method, the class inflates a layout file and sets up data binding for a MainViewModel.

The "onViewCreated" method is called after the fragment's UI is created. In this method, the class sets up LiveData and calls a method to check and request any necessary permissions.

The "onRequestPermissionsResult" method is called when the user responds to a permission request. In this method, the class handles the result of the permission request and takes action depending on if all necessary permissions were granted.

The class also includes several other methods like "checkAndRequestPermissions", which checks for any missing permissions and requests them if necessary, and "setUpLiveData", which sets up LiveData observation and likely updates the UI with new information.

And this is the main functionality of the MainFragment class, it's checking and requesting necessary permissions, registering with DJI, and observing LiveData.

#### MainViewModel.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> main, and right-click on the main directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `MainViewModel`.

Next, replace the code of the `MainViewModel.kt` file with the following:
```kotlin
package com.riis.cameraapp.main

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.riis.cameraapp.models.DJIResourceManager
import dji.common.error.DJIError
import dji.common.useraccount.UserAccountState
import dji.common.util.CommonCallbacks
import dji.sdk.products.Aircraft
import dji.sdk.useraccount.UserAccountManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel : ViewModel() {
    val notifyStatusChanged = DJIResourceManager.instance.connectionStatus.asLiveData()
    val progressToVideo = MutableLiveData(false)
    val promptLogin = MutableLiveData(false)

    var product: Aircraft? = null
        get() {
            field = DJIResourceManager.instance.aircraft

            return field
        }
        private set

    fun openVideoFragment() {
        progressToVideo.value = true
    }

    fun registerDJI(activity: Activity) {
        viewModelScope.launch {
            DJIResourceManager.instance.registerApp(activity)
        }
    }

    fun loginDJIUserAccount(context: Context) {
        UserAccountManager.getInstance().logIntoDJIUserAccount(context,
            object : CommonCallbacks.CompletionCallbackWith<UserAccountState> {
                override fun onSuccess(userAccountState: UserAccountState) {
//                    showToast("login success! Account state is:" + userAccountState.name)
                }

                override fun onFailure(error: DJIError) {
//                    showToast(error.description)
                }
            })
    }
}
```
The class starts by importing several necessary Android classes, as well as some classes from the app's package. It also uses some experimental features from the Kotlin coroutines library.

The class has three MutableLiveData properties, "notifyStatusChanged," "progressToVideo," and "promptLogin." These are used to hold data that can be observed and updates to the UI. "notifyStatusChanged" holds the connection status of the DJI SDK, "progressToVideo" is used to determine whether or not the app should navigate to the video fragment, and "promptLogin" is used to determine whether or not the app should prompt the user to log in to their DJI account.

The class also has a property called "product" which is an aircraft object, it's used to get the aircraft object from the DJI SDK.

The class has three methods: "openVideoFragment," "registerDJI," and "loginDJIUserAccount." "openVideoFragment" sets the "progressToVideo" property to true which triggers the navigation to the video fragment. "registerDJI" method is used to register the app with the DJI SDK, it's using a coroutine to do that in a background thread. And "loginDJIUserAccount" is used to log the user into their DJI account, it's using a callback to get the result of the login process.

And this is the main functionality of the MainViewModel class, it's handling the app's data and business logic like observing the connection status of the DJI SDK, navigating to the video fragment, and logging the user into their DJI account.

---

In the project navigator, go to app -> java -> com -> riis -> cameraapp, and right-click on the cameraapp directory. Select New -> Package to create a new package and name it as `models` so that the structure looks like `com.riis.cameraapp.models`.

#### ServiceConnectionEvent.kt
In the project navigator, go to app -> java -> com -> riis -> cameraapp -> models, and right-click on the models directory. Select New -> Package to create a new package and name it as `eventbus` so that the structure looks like `com.riis.cameraapp.models.eventbus`.

Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> models -> eventbus, and right-click on the eventbus directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `ServiceConnectionEvent`.

Next, replace the code of the `ServiceConnectionEvent.kt` file with the following:
```kotlin
package com.riis.cameraapp.models.eventbus

class ServiceConnectionEvent(val connected: Boolean)
```

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
    package="com.riis.cameraapp">

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
        android:name="com.riis.cameraapp.MApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CameraApp">

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

## Summary
In this tutorial, you’ve learned how to use DJI Mobile SDK to show the FPV View from the aircraft's camera and control the camera of DJI's Aircraft to shoot photo and record video. These are the most basic and common features in a typical drone mobile app: Capture and Record. However, if you want to create a drone app which is more fancy, you still have a long way to go. More advanced features should be implemented, including previewing the photo and video in the SD Card, showing the OSD data of the aircraft and so on. Hope you enjoy this tutorial, and stay tuned for our next one!

## License
MIT
