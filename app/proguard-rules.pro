# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keep class androidx.appcompat.widget.** { *; }

-keep class androidx.lifecycle.LiveData { *; }

-keep class androidx.navigation.** { *; }
-keep interface androidx.navigation.** { *; }
-keep class * implements androidx.navigation.NavArgs { *; }
-keep class * implements androidx.navigation.Navigator { *; }
-keep class * implements androidx.navigation.NavigatorProvider { *; }
-keep class * extends androidx.navigation.NavType { *; }
-keep class * extends androidx.navigation.NavOptions { *; }
-keep class * extends androidx.navigation.NavDirections { *; }
-keep class * extends androidx.navigation.NavGraph { *; }
-keep class * extends androidx.navigation.NavDestination { *; }
-keep class * extends androidx.navigation.NavController { *; }
-keep class * extends androidx.navigation.NavInflater { *; }
-keep class * extends androidx.navigation.NavigatorProvider { *; }


#-keep class com.google.android.gms.ads.mediation.** { *; }
#-keep class com.google.android.gms.ads.rewarded.** { *; }
#-keep class com.google.android.gms.ads.interstitial.** { *; }
#-keep class com.google.android.gms.ads.appopen.** { *; }
#-keep class com.google.android.gms.ads.MobileAds.** { *; }
#
#-keep class com.google.android.gms.ads.** { *; }
#-keep class com.google.android.gms.ads.identifier.** { *; }
#-keep class com.google.android.gms.common.** { *; }
#-keep class com.google.android.gms.common.api.** { *; }
#-keep class com.google.ads.mediation.** { *; }
#-keep interface com.google.android.gms.ads.** { *; }
#-keep interface com.google.android.gms.ads.identifier.** { *; }
#-keep interface com.google.android.gms.common.** { *; }
#-keep interface com.google.ads.mediation.** { *; }

-keep class * { public static <fields>; }

# AndroidX WebView
-keep class androidx.webkit.** { *; }

# Keep all classes in Firebase packages
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep all classes in Firebase Analytics
-keepnames class com.google.android.gms.measurement.** { *; }
-keep class com.google.android.gms.measurement.internal.** { *; }

# Keep all classes in Firebase Auth
-keepnames class com.google.firebase.auth.** { *; }

# Keep all classes in Firebase Firestore
-keepnames class com.google.firebase.firestore.** { *; }

# Keep all classes in Firebase Realtime Database
-keepnames class com.google.firebase.database.** { *; }

# Keep all classes in Firebase Cloud Messaging
-keepnames class com.google.firebase.messaging.** { *; }

# Keep all classes in Firebase Remote Config
-keepnames class com.google.firebase.remoteconfig.** { *; }

# Keep all classes in Firebase Crashlytics
-keepnames class com.google.firebase.crashlytics.** { *; }

# Keep all classes in Firebase Performance Monitoring
-keepnames class com.google.firebase.perf.** { *; }

# Keep all classes in Firebase In-App Messaging
-keepnames class com.google.firebase.inappmessaging.** { *; }

# Keep all classes in Firebase ML Kit
-keepnames class com.google.firebase.ml.** { *; }

-keep class com.qrcodescanner.barcodereader.qrgenerator.** { *; }
# Directory Wise : activities
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.AllLanguagesActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.CropActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.FirstOpenActivity.** { *; }
-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.LanguageActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.LanguageSelectedActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.MainActivity2.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.NewCreateActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.PhotoTranslaterActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.PreviewActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.SaveDocumentActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.SplashQRActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.SupportedLanguages.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.TextTranslator.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.activities.WalkThroughActivity.** { *; }

# Directory Wise : adapters
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundGradientAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundGradientColorAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundRecyclerAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.ColorRecyclerAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.CountryAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.GradientBackground.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.GradientRecyclerAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.ImageRecyclerAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.LanguageAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.ScanListAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.ScanListAdapter1.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.SocialAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.adapters.WalkThroughAdapter.** { *; }
#
## Directory Wise : ads
#-keep class com.qrcodescanner.barcodereader.qrgenerator.ads.CustomDialog.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.ads.ForegroundCheckTask.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.ads.GoogleMobileAdsConsentManager.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck.** { *; }
#
## Directory Wise : database
#-keep class com.qrcodescanner.barcodereader.qrgenerator.database.OnQRCodeClickListener.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeAdapter.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeData.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper.** { *; }
#
## Directory Wise : firebase
#-keep class com.qrcodescanner.barcodereader.qrgenerator.firebase.FirebaseMessageReceiver.** { *; }

## Directory Wise : models
#-keep class com.qrcodescanner.barcodereader.qrgenerator.models.AllCountryModel.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.models.QRCodeViewModel.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.models.ScannedItem.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.models.ShareHelper.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.models.SocialItem.** { *; }
#
## Directory Wise : myapplication
#-keep class com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication.** { *; }

# Directory Wise : fragments
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.AppFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.BatchFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CalenderFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ClipboardFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ContactsFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CreateBarCodeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CreateEmailQRFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CreateLinksQRCodeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CreateQRFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.CreateQRorBarCodeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.EmailFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.FinalImageFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.HelpFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.HistoryFragment.** { *; }
#-keep interface com.qrcodescanner.barcodereader.qrgenerator.fragments.HistoryListener.** { *; }
-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.HomeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.HowToUseFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.LanguageFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.LocationFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.QRCodeCreationURLFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ScanCode.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ScanDocumentFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ScanningTips.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.SettingFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowBarcodeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowBatchFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowCalenderFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowClipboardQR.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowContactQRFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowLocationQRFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowQRForAppFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowScanCode.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ShowWifiQrFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ViewQRCodeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.ViewQRCodeFragment1.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughFullScreenAdFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughOneFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughThreeFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WalkThroughTwoFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WebViewFragment.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.fragments.WifiFragment.** { *; }

# Directory Wise : utils
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.AllCountrylistMe.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.AppLanguages.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.BackgroundItem.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.BaseActivity.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.BitmapAnnotator.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.CameraConfigurationUtils.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.ColorItem.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.CountryList.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.CountryModel.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.GradientItem.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.ImageItem.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.LanguageRecognizer.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.LocaleManager.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.MyLocaleHelper.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.NetworkUtil.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.NewOCR.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.StorageCommon.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.Utils.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.ZoomableImageView.** { *; }
#-keep class com.qrcodescanner.barcodereader.qrgenerator.utils.ZoomableImageViewTouchListener.** { *; }

## Firebase
#-keepattributes *Annotation*
#-keep class com.google.firebase.** { *; }
#-dontwarn com.google.firebase.**
#-keepclassmembers class * extends com.google.firebase.components.ComponentRegistrar { *; }
#-keepclassmembers class * extends com.google.firebase.components.ComponentFactory { *; }
#
## Glide
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep class com.bumptech.glide.** { *; }
#-dontwarn com.bumptech.glide.**
#
## ML Kit
#-keep class com.google.mlkit.** { *; }
#-dontwarn com.google.mlkit.**
#-keepattributes *Annotation*
#
## CameraX
#-keep class androidx.camera.** { *; }
#-dontwarn androidx.camera.**
#
## Material, AppCompat, Core KTX
#-dontwarn androidx.**
#
## Lottie
#-keep class com.airbnb.lottie.** { *; }
#-dontwarn com.airbnb.lottie.**
#
## ZXing
#-keep class com.google.zxing.** { *; }
#-dontwarn com.google.zxing.**
#
## Gson
#-keep class com.google.gson.** { *; }
#-keepattributes *Annotation*
#-dontwarn com.google.gson.**
#
## Apero and Monetization
#-keep class apero.** { *; }
#-dontwarn apero.**
#
## iText
#-keep class com.itextpdf.** { *; }
#-dontwarn com.itextpdf.**
#
## ColorPickerView
#-keep class com.skydoves.colorpickerview.** { *; }
#-dontwarn com.skydoves.colorpickerview.**

