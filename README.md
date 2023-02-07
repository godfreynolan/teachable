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
In the project file navigator, go to app -> java -> com -> riis -> cameraapp, and right-click on the cameraapp directory. Select New -> Kotlin Class to create a new kotlin class and name it as MApplication.kt.

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
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val action = intent?.action
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == action) {
            val attachedIntent = Intent()
            attachedIntent.action = DJISDKManager.USB_ACCESSORY_ATTACHED
            sendBroadcast(attachedIntent)
        }
    }
}
```
This code defines an Android activity called MainActivity that is part of the package com.riis.cameraapp. The activity is a subclass of AppCompatActivity, which is a class from the Android support library that provides compatibility with older versions of Android.

The onCreate method is called when the activity is first created. In this method, the activity sets its layout by calling setContentView(R.layout.main_activity). The layout is defined in a file called main_activity.xml in the res/layout directory. The supportActionBar?.hide() line hides the action bar of the activity.

The onNewIntent method is called when a new intent is delivered to the activity. In this method, the code first checks if the intent's action is UsbManager.ACTION_USB_ACCESSORY_ATTACHED. If it is, it creates a new intent with the action DJISDKManager.USB_ACCESSORY_ATTACHED and sends it as a broadcast. This broadcast is used by the DJI SDK to handle the USB accessory being attached.

### 3. Implementing the MainActivity Layout
Open the `activity_main.xml` layout file and replace the code with the following:
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

#### DJIResourceManager.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> models, and right-click on the models directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `DJIResourceManager`.

Next, replace the code of the `DJIResourceManager.kt` file with the following:
```kotlin
package com.riis.cameraapp.models

import android.content.Context
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.flightcontroller.FlightController
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
@FlowPreview
class DJIResourceManager {
    private object HOLDER {
        val INSTANCE = DJIResourceManager()
    }

    companion object {
        val instance: DJIResourceManager by lazy { HOLDER.INSTANCE }
    }

    var flightController: FlightController? = null
    var aircraft: Aircraft? = null

    private var _connectionStatus = ConflatedBroadcastChannel(false)
    val connectionStatus: Flow<Boolean> = _connectionStatus.asFlow()

    private var isRegistrationInProgress = false;

    suspend fun registerApp(context: Context?): Boolean {
        if (isRegistrationInProgress) {
            return false
        }

        isRegistrationInProgress = true

        return suspendCoroutine { continuation ->
            DJISDKManager.getInstance()
                .registerApp(context, object : DJISDKManager.SDKManagerCallback {
                    override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {}

                    override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {

                    }

                    override fun onComponentChange(
                        componentKey: BaseProduct.ComponentKey?,
                        old: BaseComponent?,
                        new: BaseComponent?
                    ) {

                    }

                    override fun onProductDisconnect() {
                        resetDefaultValues()
                    }

                    override fun onProductConnect(product: BaseProduct?) {
                        product?.let {
                            try {
                                aircraft = (it as Aircraft)
                                flightController = it.flightController
                                _connectionStatus.offer(true)
                                setupCallbacks()
                            } catch (ex: Exception) {
                                flightController = null
                            }
                        }
                    }

                    override fun onProductChanged(p0: BaseProduct?) {

                    }

                    override fun onRegister(error: DJIError?) {
                        error?.let {
                            if (it == DJISDKError.REGISTRATION_SUCCESS) {
                                DJISDKManager.getInstance().startConnectionToProduct()
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }

                            isRegistrationInProgress = false
                        }
                    }
                })
        }
    }

    private fun resetDefaultValues() {
        flightController = null
        _connectionStatus.offer(false)
    }

    private fun setupCallbacks() {
        initDroneStateCallback()
        initRCLocationCallback()
        initDroneBatteryCallback()
        initRCBatteryCallback()
    }

    private fun initDroneStateCallback() {
        flightController?.setStateCallback { state ->
        }
    }

    private fun initRCLocationCallback() {
        aircraft?.remoteController?.setGPSDataCallback { data ->
        }
    }

    private fun initDroneBatteryCallback() {
        aircraft?.battery?.setStateCallback { state ->
        }
    }

    private fun initRCBatteryCallback() {
        aircraft?.remoteController?.setChargeRemainingCallback { charge ->
        }
    }
}
```

#### RTMPCallback.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> models, and right-click on the models directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `RTMPCallback`.

Next, replace the code of the `RTMPCallback.kt` file with the following:
```kotlin
package com.riis.cameraapp.models

import android.content.Context
import android.widget.Toast
import com.pedro.rtmp.utils.ConnectCheckerRtmp

class RTMPCallback(private val context: Context) : ConnectCheckerRtmp {
    override fun onAuthSuccessRtmp() {
        // We don't care?
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        // We don't care?
    }

    override fun onConnectionSuccessRtmp() {
        // We don't care?
    }

    override fun onConnectionFailedRtmp(reason: String) {
        Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        Toast.makeText(context, "RTMP Connection Started", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthErrorRtmp() {
        Toast.makeText(context, "RTMP Authorization Error", Toast.LENGTH_SHORT).show()
    }

    override fun onDisconnectRtmp() {
        Toast.makeText(context, "Stream disconnected", Toast.LENGTH_SHORT).show()
    }
}
```

This is Kotlin code for `RTMPCallback.kt` which handless the callbacks related to the RTMP connection in the app.

The class starts by importing necessary classes from the Android SDK and a class called "ConnectCheckerRtmp" which is a class from a third-party library used to handle the RTMP connection.

The class is implemented as a wrapper of "ConnectCheckerRtmp" interface which has several callback methods: "onAuthSuccessRtmp," "onNewBitrateRtmp," "onConnectionSuccessRtmp," "onConnectionFailedRtmp," "onConnectionStartedRtmp," "onAuthErrorRtmp," and "onDisconnectRtmp."

The "RTMPCallback" class constructor takes a parameter "context" of the type Context.

All the callback methods are overridden in the class. In all the methods, it is showing a Toast message to the user to notify the user about the current status of the RTMP connection. For example, in the "onConnectionFailedRtmp" method, it is showing a Toast message with the reason why the connection failed. Similarly, in the "onAuthErrorRtmp" method, it is showing a Toast message with the message "RTMP Authorization Error" to the user.

The Toast message is used to show a quick message to the user on the screen, in this case, it's used to notify the user about the status of the RTMP connection.

#### RTPProvider.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> models, and right-click on the models directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `RTPProvider`.

Next, replace the code of the `RTPProvider.kt` file with the following:
```kotlin
package com.riis.cameraapp.models

import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import com.pedro.rtplibrary.base.DisplayBase

class RTPProvider(
    private val streamCamera: DisplayBase
) {
    private val requestCodeScreenProjector = 179

    fun onStreamingActivityResult(
        destination: String,
        display: Display,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode != requestCodeScreenProjector || resultCode != Activity.RESULT_OK) {
            return
        }

        streamCamera.setIntentResult(Activity.RESULT_OK, data)

        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        if (streamCamera.prepareAudio() && streamCamera.prepareVideo(
                width,
                height,
                30,
                1200 * 1024,
                0,
                displayMetrics.densityDpi
            )
        ) {
            streamCamera.startStream(destination)
        } else {
            /*This device cant init encoders, this could be for 2 reasons: The encoder selected
                 * doesn't support any configuration setted or your device hasn't a H264 or AAC encoder
                 * (in this case you can see log error valid encoder not found)
                 * */
            Log.e("STREAM", "Could not start stream")
        }
    }

    fun stopStreaming() {
        if (streamCamera.isStreaming) {
            streamCamera.stopStream()
        }
    }

    fun toggleStreaming(fragment: Fragment) {
        if (streamCamera.isStreaming) {
            streamCamera.stopStream()
        } else {
            fragment.startActivityForResult(streamCamera.sendIntent(), requestCodeScreenProjector)
        }
    }
}
```

This is a Kotlin code for a class called "RTPProvider" which handles the RTMP streaming in the app.

The class starts by importing necessary classes from the Android SDK and a class called "DisplayBase" which is a class from a third-party library used to handle the RTMP streaming.

The class has a constructor which takes an instance of "DisplayBase" as a parameter.

The class has 3 methods:

    onStreamingActivityResult: it is used to handle the result of the activity started for RTMP streaming. It is checking if the request code and result code are correct and then it's calling setIntentResult and prepareAudio and prepareVideo methods on streamCamera object. If the preparation was successful, it starts the stream by calling startStream method on streamCamera object.
    stopStreaming: it is used to stop the RTMP streaming. It's checking if the streamCamera is streaming, if it's true then it stops the stream by calling the stopStream method on streamCamera object.
    toggleStreaming: it is used to toggle the RTMP streaming. It's checking if the streamCamera is streaming, if it's true then it stops the stream by calling the stopStream method on streamCamera object. If it's not streaming, it starts the activity for RTMP streaming by calling the startActivityForResult method on the fragment object and passing the intent returned by the sendIntent method on streamCamera object.

#### StreamBinder.kt

In the project navigator, go to app -> java -> com -> riis -> cameraapp, and right-click on the cameraapp directory. Select New -> Package to create a new package and name it as `service` so that the structure looks like `com.riis.cameraapp.service`.

Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> service, and right-click on the service directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `StreamBinder`.

Next, replace the code of the `StreamBinder.kt` file with the following:
```kotlin
package com.riis.cameraapp.service

import android.os.Binder

class StreamBinder(private val service: StreamService): Binder() {
    fun getService(): StreamService {
        return service
    }
}
```

#### StreamService.kt
Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> service, and right-click on the service directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `StreamService`.

Next, replace the code of the `StreamService.kt` file with the following:
```kotlin
package com.riis.cameraapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Display
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.pedro.rtplibrary.rtmp.RtmpDisplay
import com.riis.cameraapp.models.RTMPCallback
import com.riis.cameraapp.models.RTPProvider

class StreamService : Service() {
    private lateinit var rtpProvider: RTPProvider
    private val streamBinder = StreamBinder(this)

    private val channelId = "rtpDisplayStreamChannel"

    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder {
        return streamBinder
    }

    override fun onCreate() {
        super.onCreate()

        rtpProvider = RTPProvider(RtmpDisplay(this, true, RTMPCallback(this)))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        keepAliveTrick()
    }

    override fun onDestroy() {
        super.onDestroy()
        rtpProvider.stopStreaming()
        stopForeground(true)
    }

    private fun keepAliveTrick() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        } else {
            startForeground(1, Notification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun onStreamingActivityResult(
        destination: String,
        display: Display,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        rtpProvider.onStreamingActivityResult(
            destination,
            display,
            requestCode,
            resultCode,
            data
        )
    }

    fun stopStreaming() {
        rtpProvider.stopStreaming()
    }

    fun toggleStreaming(fragment: Fragment) {
        rtpProvider.toggleStreaming(fragment)
    }
}
```

This code is for an Android service called StreamService. A service is a background task that runs independently of the main app, and it can run even when the app is not in the foreground. This StreamService is used to stream video from the camera app to a remote server using RTMP protocol.

The service is implemented by extending the Android Service class and overriding some of its methods. The onCreate method is called when the service is first created and it initializes the RTPProvider and a NotificationManager, which is used to create a notification channel for the service. The onStartCommand method is called when the service is started and it returns a flag to start the service in a "sticky" mode, which means that it will be restarted if it is stopped by the system.

The service also has a keepAliveTrick method, which is used to make sure that the service continues running even if the app is not in the foreground. It creates a notification with no content that keeps the service alive and running.

The onStreamingActivityResult, stopStreaming, and toggleStreaming methods are used to control the streaming process. The onStreamingActivityResult method is used to handle the result of the activity that starts when the user wants to start streaming. The stopStreaming method stops the streaming process and the toggleStreaming method starts or stops the streaming process depending on the current state of the streaming.

The service also has a StreamBinder inner class that is used to bind the service to the activity, which allows the activity to access the service's methods.

#### StreamServiceConnection.kt

Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> service, and right-click on the service directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `StreamServiceConnection`.

Next, replace the code of the `StreamServiceConnection.kt` file with the following:
```kotlin
package com.riis.cameraapp.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.riis.cameraapp.models.eventbus.ServiceConnectionEvent
import org.greenrobot.eventbus.EventBus

class StreamServiceConnection : ServiceConnection {
    var isBound = false
    var service: StreamService? = null
        private set

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        EventBus.getDefault().post(ServiceConnectionEvent(false))
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as StreamBinder
        this.service = binder.getService()
        EventBus.getDefault().post(ServiceConnectionEvent(true))
    }
}
```

#### VideoFragment.kt

In the project navigator, go to app -> java -> com -> riis -> cameraapp, and right-click on the cameraapp directory. Select New -> Package to create a new package and name it as `video` so that the structure looks like `com.riis.cameraapp.video`.

Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> video, and right-click on the service directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `VideoFragment.kt`.

Next, replace the code of the `VideoFragment.kt` file with the following:
```kotlin
package com.riis.cameraapp.video

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.riis.cameraapp.BuildConfig
import com.riis.cameraapp.databinding.VideoFragmentBinding
import com.riis.cameraapp.models.eventbus.ServiceConnectionEvent
import com.riis.cameraapp.service.StreamService
import com.riis.cameraapp.service.StreamServiceConnection
import dji.common.product.Model
import dji.sdk.camera.VideoFeeder
import dji.sdk.codec.DJICodecManager
import kotlinx.android.synthetic.main.video_fragment.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VideoFragment : Fragment() {
    private val viewModel: VideoViewModel by viewModels()

    private var codecManager: DJICodecManager? = null

    // The callback for receiving the raw H264 video data for camera live view
    private var mReceivedVideoDataListener =
        VideoFeeder.VideoDataListener { videoBuffer: ByteArray?, size: Int ->
            codecManager?.sendDataToDecoder(videoBuffer, size)
        }

    private val streamServiceConnection = StreamServiceConnection()

    private var streamService: StreamService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = VideoFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews(view)

        streamService?.toggleStreaming(this)
    }

    override fun onResume() {
        super.onResume()

        val product = viewModel.product

        if (product?.isConnected == false) {
            Toast.makeText(requireActivity(), "Product is null or not connected", Toast.LENGTH_LONG).show()
        } else if (product?.model != Model.UNKNOWN_AIRCRAFT) {
            VideoFeeder.getInstance().primaryVideoFeed.addVideoDataListener(
                mReceivedVideoDataListener
            )
        }

        if (streamService != null) {
            return
        }

        val intent = Intent(requireActivity(), StreamService::class.java)

        if (!streamServiceConnection.isBound) {
            val result = requireActivity().bindService(intent, streamServiceConnection, Context.BIND_AUTO_CREATE)

            streamServiceConnection.isBound = result
            Log.e("STREAM", "Service has been bound: $result")
        }

        streamService = streamServiceConnection.service
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        if (mReceivedVideoDataListener != null) {
            VideoFeeder.getInstance().primaryVideoFeed
                .removeVideoDataListener(mReceivedVideoDataListener)
        }

        streamService?.stopStreaming()
        super.onPause()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)

        if (streamService != null) {
            if (streamServiceConnection.isBound) {
                requireActivity().unbindService(streamServiceConnection)
                streamServiceConnection.isBound = false
            }

            streamService?.stopSelf()
            streamService = null
        }

        super.onStop()
    }

    override fun onDestroy() {
        codecManager?.cleanSurface()
        codecManager?.destroyCodec()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Broadcasts to appriis twitch (twitch.tv/appriis)
        streamService?.onStreamingActivityResult(
            "rtmp://live.twitch.tv/app/" + BuildConfig.TWITCH_KEY,
            requireActivity().windowManager.defaultDisplay,
            requestCode,
            resultCode,
            data
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onServiceConnectionEvent(event: ServiceConnectionEvent) {
        if (event.connected) {
            streamService = streamServiceConnection.service

            streamService?.toggleStreaming(this)
        } else {
            streamService = null
        }
    }

    private fun setUpViews(view: View) {
        view.video_texture_view.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    codecManager?.cleanSurface()
                    return false
                }

                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    if (codecManager == null) {
                        codecManager = DJICodecManager(requireActivity(), surface, width, height)
                    }
                }
            }
    }
}
```

This code is for a fragment called VideoFragment which is used to display live video feed from a DJI drone camera. It is using the DJI SDK to receive the raw H.264 video data for camera live view and display it on the screen.

The fragment sets up views, binds to a StreamService, and receives video data to display. The DJICodecManager is also used to decode the video data and send it to the display. The fragment also uses the EventBus library to listen for events related to the service connection and handle them appropriately.

When the fragment is created, it sets up views and starts the streaming service. When it is resumed, it starts listening for video data and binds to the streaming service. When it is paused, it stops listening for video data and stops the streaming service. When it is stopped, it unregisters from the EventBus and unbinds from the streaming service. Additionally, it uses the keepAliveTrick method to keep the service running in the background.

#### VideoViewModel.kt

Next in the project navigator, go to app -> java -> com -> riis -> cameraapp -> video, and right-click on the service directory. Select New -> Kotlin Class/File to create a new Kotlin Class and name it as `VideoViewModel.kt`.

Next, replace the code of the `VideoViewModel.kt` file with the following:
```kotlin
package com.riis.cameraapp.video

import androidx.lifecycle.ViewModel
import com.riis.cameraapp.models.DJIResourceManager
import dji.sdk.products.Aircraft
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VideoViewModel : ViewModel() {
    var product: Aircraft? = null
        get() {
            field = DJIResourceManager.instance.aircraft

            return field
        }
        private set
}
```

### 5. Implementing the Layouts
Next, we create **res->layout->main_fragment and add the following xml code, replacing the old xml with it. 
```xml
<?xml version="1.0" encoding="utf-8"?>
<layout>

    <data>

        <variable
            name="viewModel"
            type="com.riis.cameraapp.main.MainViewModel" />
    </data>

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_margin="15dp"
        android:orientation="vertical">

        <TextView
            android:id="@+id/connection_status_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Status: No Product Connected"
            android:textColor="@android:color/black"
            android:textSize="20sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/product_info_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="5dp"
            android:text="Product Information" />

        <TextView
            android:id="@+id/model_info_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="5dp"
            android:text="Model Not Available" />

        <TextView
            android:id="@+id/livestream_url_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="5dp"
            android:text="Livestream URL" />

        <View
            android:layout_width="wrap_content"
            android:layout_height="0dp"
            android:layout_weight="1" />

        <Button
            android:id="@+id/open_button"
            android:layout_width="wrap_content"
            android:layout_height="60dp"
            android:layout_gravity="end"
            android:enabled="false"
            android:onClick="@{() -> viewModel.openVideoFragment()}"
            android:text="OPEN"
            android:textSize="20sp" />
    </LinearLayout>
</layout>
```
We are creating the new connection screen within this file, broviding a button "open_button" to connect to the drone. **TextView**s are added to give more information of the drone and the twitch connection. 

Finally, we create **res->layout->video_fragment.xml** and replace the old xml with the following.
```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:custom="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>

        <variable
            name="viewModel"
            type="com.riis.cameraapp.video.VideoViewModel" />
    </data>

    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
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
                custom:sourceCameraNameVisibility="false"
                android:id="@+id/video_texture_view"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_gravity="center"
                android:layout_marginBottom="-2dp"/>
        </RelativeLayout>

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


        <LinearLayout
            android:id="@+id/camera"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_below="@id/signal"
            android:layout_centerHorizontal="true"
            android:layout_marginTop="20dp"
            android:background="@color/dark_gray"
            android:orientation="horizontal">

            <dji.ux.widget.AutoExposureLockWidget
                android:layout_width="30dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.FocusExposureSwitchWidget
                android:layout_width="30dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.FocusModeWidget
                android:layout_width="30dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigISOAndEIWidget
                android:layout_width="60dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigShutterWidget
                android:layout_width="60dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigApertureWidget
                android:layout_width="60dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigEVWidget
                android:layout_width="60dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigWBWidget
                android:layout_width="70dp"
                android:layout_height="30dp"/>

            <dji.ux.widget.config.CameraConfigStorageWidget
                android:layout_width="130dp"
                android:layout_height="30dp"/>
        </LinearLayout>

        <dji.ux.widget.ManualFocusWidget
            android:layout_below="@id/camera"
            android:layout_alignLeft="@id/camera"
            android:layout_marginLeft="25dp"
            android:layout_marginTop="5dp"
            android:layout_width="42dp"
            android:layout_height="218dp"
            tools:ignore="RtlHardcoded"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:orientation="horizontal"
            android:padding="12dp">

            <dji.ux.widget.dashboard.DashboardWidget
                android:id="@+id/Compass"
                android:layout_width="405dp"
                android:layout_height="91dp"
                android:layout_marginRight="12dp"
                tools:ignore="RtlHardcoded"/>

        </LinearLayout>

        <dji.ux.widget.controls.CameraControlsWidget
            android:id="@+id/CameraCapturePanel"
            android:layout_alignParentRight="true"
            android:layout_below="@id/camera"
            android:layout_width="50dp"
            android:layout_height="213dp"
            tools:ignore="RtlHardcoded"/>


        <dji.ux.panel.CameraSettingExposurePanel
            android:layout_width="180dp"
            android:layout_below="@id/camera"
            android:layout_toLeftOf="@+id/CameraCapturePanel"
            android:background="@color/transparent"
            android:gravity="center"
            android:layout_height="263dp"
            android:visibility="invisible"
            tools:ignore="RtlHardcoded"/>

        <dji.ux.panel.CameraSettingAdvancedPanel
            android:layout_width="180dp"
            android:layout_height="263dp"
            android:layout_below="@id/camera"
            android:layout_toLeftOf="@+id/CameraCapturePanel"
            android:background="@color/transparent"
            android:gravity="center"
            android:visibility="invisible"
            tools:ignore="RtlHardcoded"/>

    </RelativeLayout>
</layout>
```
Widgets from the DJI UX SDK are added to provide functionality and user interface to the GUI and the camera from the drone. 

The **<RelativeLayout> fpv_container** is the most important here as it creates the DJI drone camera and displays it on screen for the user. This widget is then accessed by it's child widget **dji.ux.widget.FPVWidget** from the video_fragment kotlin class file. 

If the functionality of any of these widgets needs to be known, they may be found on the DJI SDK documentation website. 

### 6. Configuring the Resource XMLs
Once you finish the above steps, let's change some of the files in **(app -> res -> values)** to some useful colours and strings. 
First, open the `colors.xml` file and update the content as shown below:
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

    <style name="AppTheme" parent="@style/Theme.AppCompat.DayNight.NoActionBar">
    </style>
</resources>
```

### 7. Misc XMLs
Let's create (or open) **res->xml->accessory_filter.xml** (right click on xml and create accessory_filter like you have been doing this tutorial)
Replace or add in the following code:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-accessory model="T600" manufacturer="DJI"/>
    <usb-accessory model="AG410" manufacturer="DJI"/>
    <usb-accessory model="com.dji.logiclink" manufacturer="DJI"/>
    <usb-accessory model="WM160" manufacturer="DJI"/>
</resources>
```

In your project structure right click on **res** and click New Directory, name this directory "navigation", inside of navigation create a new XML file called `main_nav_graph.xml`.

Copy all of the following code into that file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main_nav_graph"
    app:startDestination="@id/main_fragment">

    <fragment
        android:id="@+id/main_fragment"
        android:name="com.riis.cameraapp.main.MainFragment"
        tools:layout="@layout/main_fragment">

        <action
            android:id="@+id/action_main_to_video"
            app:destination="@id/video_fragment" />
    </fragment>

    <fragment
        android:id="@+id/video_fragment"
        android:name="com.riis.cameraapp.video.VideoFragment"
        tools:layout="@layout/video_fragment" />
</navigation>
```


### 8. AndroidManifest.xml
Under `manifests` open up `AndroidManifest.xml` and copy in the following code:
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
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature
        android:name="android.hardware.usb.host"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.usb.accessory"
        android:required="true" />

    <application
        android:name=".MApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <meta-data
            android:name="com.dji.sdk.API_KEY"
            android:value="${DJI_API_KEY}" />

        <service android:name="dji.sdk.sdkmanager.DJIGlobalService" />

        <uses-library
            android:name="org.apache.http.legacy"
            android:required="false" />
        <uses-library android:name="com.android.future.usb.accessory" />

        <activity
            android:name=".MainActivity"
            android:launchMode="singleTop"
            android:screenOrientation="reverseLandscape"
            tools:ignore="LockedOrientationActivity">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <meta-data
                android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED"
                android:resource="@xml/accessory_filter" />
        </activity>

        <service
            android:name=".service.StreamService"
            android:foregroundServiceType="mediaProjection" />
    </application>
</manifest>
```

~~~~
Congratulations! Your Live Streaming FPV Camera android app is complete, you can now use this app to control the camera of your DJI Product now.
~~~~

## Summary
In this tutorial, you’ve learned how to use DJI Mobile SDK to live stream the FPV View from the aircraft's camera and control the camera of DJI's Aircraft to shoot photo and record video. These are the most basic and common features in a typical drone mobile app: Capture and Record. However, if you want to create a drone app which is more fancy, you still have a long way to go. More advanced features should be implemented, including previewing the photo and video in the SD Card, showing the OSD data of the aircraft and so on. Hope you enjoy this tutorial, and stay tuned for our next one!

## License
MIT
