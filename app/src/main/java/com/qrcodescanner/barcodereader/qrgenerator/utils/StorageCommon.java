package com.qrcodescanner.barcodereader.qrgenerator.utils;

import androidx.lifecycle.MutableLiveData;

import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.ads.control.ads.wrapper.ApRewardAd;
import com.google.android.gms.ads.nativead.NativeAd;

public class StorageCommon {
    public MutableLiveData<ApNativeAd> nativeAdsLanguage = new MutableLiveData<>();
    public ApInterstitialAd interstitialAd;
    public ApInterstitialAd interstitialAdByClick;
    public ApRewardAd rewardAd;
    public ApInterstitialAd interPriority;
    public ApInterstitialAd interNormal;

    public ApNativeAd apNativeAdHigh;
    public ApNativeAd apNativeAdMedium;
    public ApNativeAd apNativeAdNormal;

    public NativeAd nativeAdHigh;
    public NativeAd nativeAdMedium;
    public NativeAd nativeAdNormal;
}
