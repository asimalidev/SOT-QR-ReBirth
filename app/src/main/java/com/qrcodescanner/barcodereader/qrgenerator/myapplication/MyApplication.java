package com.qrcodescanner.barcodereader.qrgenerator.myapplication;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.AperoAd;
import com.ads.control.application.AdsMultiDexApplication;
import com.ads.control.billing.AppPurchase;
import com.ads.control.billing.PurchaseItem;
import com.ads.control.config.AdjustConfig;
import com.ads.control.config.AperoAdConfig;
import com.google.android.ump.ConsentDebugSettings;
import com.google.firebase.FirebaseApp;
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig;
import com.qrcodescanner.barcodereader.qrgenerator.activities.FirstOpenActivity;
import com.qrcodescanner.barcodereader.qrgenerator.notification.AppAlarmManager;
import com.qrcodescanner.barcodereader.qrgenerator.utils.NetworkUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsMultiDexApplication {
    private static MyApplication context;

    public static MyApplication getApplication() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

//        // Here, call the function again, everytime it is called, the previous one with the same request code will be overwritten
//        // I have to go now, hope you understood
//        // But implement steps by steps and read the sample and documentation carefully
//        //ok sure thankyou so much for the help
//        AppAlarmManager.INSTANCE.scheduleLockscreenWidget(
//                this, AppAlarmManager.lockscreenWidgetRequestCode, /* Hour from shared pref */, /* Minute from shared pref */
//        );


        // Retrieve saved time from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("NotificationTimePrefs", MODE_PRIVATE);
        int hour = sharedPreferences.getInt("hour", -1);
        int minute = sharedPreferences.getInt("minute", -1);

        // Schedule the notification if a valid time is saved
        if (hour != -1 && minute != -1) {
            AppAlarmManager.INSTANCE.scheduleLockscreenWidget(
                    this,
                    AppAlarmManager.lockscreenWidgetRequestCode,
                    hour,
                    minute
            );
        }


        new ConsentDebugSettings.Builder(this).addTestDeviceHashedId("788DC0EF295EAB080084C4D989D15221");
        new Thread(() -> FirebaseApp.initializeApp(context)).start();
        AppOpenManager.getInstance().disableAppResumeWithActivity(FirstOpenActivity.class);
        Admob.getInstance().setNumToShowAds(0);
        NetworkUtil.initNetwork(this);
        initBilling();
        initAds();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.qrcodescanner.barcodereader.qrgenerator",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? AperoAdConfig.ENVIRONMENT_DEVELOP : AperoAdConfig.ENVIRONMENT_PRODUCTION;
//        String API_KEY_ADS = "sRwPbWXnbJicCBsXn1TMLS0k4jdpZFu/LCSdW9vNHLA1OnN1bIBbOQBwdE2bvc5/KxnL3tYRdc03lWxwUUuWO2/8X/qRGyh2/A8+4c+6xVPZnNej+L0tF7DBIxRAwKhXUBZSFX7pw59SJGPWO5D5v9wbTfpAUWeJ/eaPPXrLWUc=";
        String API_KEY_ADS = "Qn2hnLaLs4b5UEzJvy35xfmDKEVNFVFO1O64pINEbAXad4dYtotU6PYy2bLbqne89Gs38Bejxnm+J30Vzr44m5hdChDRNZB9d6fIllGLcJYjb7CuCJUmPLxga81ylL2ds7zqQ8AxIBz5m/GZL4xmJNiiGGPXsyUk1WD+QiKGPVM=";
        aperoAdConfig = new AperoAdConfig(this, API_KEY_ADS, AperoAdConfig.PROVIDER_ADMOB, environment);

        // Optional: setup Adjust event
        String ADJUST_TOKEN = "adel766vwd1c";
        AdjustConfig adjustConfig = new AdjustConfig(ADJUST_TOKEN);
//        String EVENT_AD_IMPRESSION_ADJUST = "igny8f";
//        adjustConfig.setEventAdImpression(EVENT_AD_IMPRESSION_ADJUST);
//        String EVENT_PURCHASE_ADJUST = "gzel1k";
//        adjustConfig.setEventNamePurchase(EVENT_PURCHASE_ADJUST);
        aperoAdConfig.setAdjustConfig(adjustConfig);

        /* Optional: enable ads resume
        aperoAdConfig.setIdAdResume(BuildConfig.open_resume);*/

        /*// set id app_open_app ( if use )
        AppOpenManager.getInstance().setSplashAdId(BuildConfig.ads_open_app);*/

        // Optional: setup list device test - recommended to use
        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        listTestDevice.add("84C3994693FB491110A5A4AEF8C5561B");
        listTestDevice.add("496C40F3C0D6510144A8B06FE6A20953");
        aperoAdConfig.setListDeviceTest(listTestDevice);
        AperoAd.getInstance().init(this, aperoAdConfig, false);

        /*// Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(false);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(false);

        AppOpenManager.getInstance().disableAppResumeWithActivity(FirstOpenActivity.class);*/
    }

    private void initBilling() {
        List<PurchaseItem> listPurchaseItem = new ArrayList<>();
        listPurchaseItem.add(new PurchaseItem("PRODUCT_ID", AppPurchase.TYPE_IAP.PURCHASE));
        listPurchaseItem.add(new PurchaseItem("ID_SUBS_WITH_FREE_TRIAL", "trial_id", AppPurchase.TYPE_IAP.SUBSCRIPTION));
        listPurchaseItem.add(new PurchaseItem("ID_SUBS_WITHOUT_FREE_TRIAL", AppPurchase.TYPE_IAP.SUBSCRIPTION));
        AppPurchase.getInstance().initBilling(this, listPurchaseItem);
    }
}
