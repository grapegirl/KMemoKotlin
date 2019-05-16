# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\cosquare\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontshrink
-dontoptimize
-dontpreverify
-renamesourcefileattribute SourceFile

-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.auth.**
-dontwarn com.google.firebase.crash.**
-dontwarn com.google.firebase.database.**



# RetroFit2
-dontwarn retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
#OKIo
-dontwarn okio.**
# AndroidX
-keep class androidx.core.app.CoreComponentFactory { *; }