package com.qrcodescanner.barcodereader.qrgenerator.utils

import apero.aperosg.monetization.adgroup.BannerAdGroup
import apero.aperosg.monetization.adgroup.InterstitialAdGroup
import apero.aperosg.monetization.adgroup.NativeAdGroup
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig

object AdsProvider {
    val interSplash = InterstitialAdGroup(
        BuildConfig.inter_spl_2 to "inter_spl_2",
        BuildConfig.inter_spl_0 to "inter_spl_0",
        name = "banner_spl_group"
    )

    val bannerSplash = BannerAdGroup(
        BuildConfig.banner_spl_0 to "banner_spl_0",
        name = "banner_spl_group"
    )

    val nativeLanguageOne = NativeAdGroup(
        BuildConfig.native_lang1_2 to "native_lang1_2",
        BuildConfig.native_lang1_0 to "native_lang1_0",
        name = "native_lang_group_one"
    )

    val nativeLanguageTwo = NativeAdGroup(
        BuildConfig.native_lang2_2 to "native_lang2_2",
        BuildConfig.native_lang2_0 to "native_lang2_0",
        name = "native_lang_group_two"
    )

    val bannerAll = BannerAdGroup(
        BuildConfig.banner to "banner",
        name = "banner_group"
    )

    val nativeWalkThroughOne = NativeAdGroup(
        BuildConfig.native_onb1_0 to "native_onb1_0",
        BuildConfig.native_onb1_2 to "native_onb1_2",

        name = "native_wt_group_one"
    )

    val nativeWalkThroughFullScreen = NativeAdGroup(
        BuildConfig.native_onb2_f_2_2 to "native_onb2_f_2_2",
        BuildConfig.native_onb2_f_0 to "native_onb_0_f",
        name = "native_wt_group_f"
    )



    val nativeHome = NativeAdGroup(
        BuildConfig.native_home to "native_home",
        name = "native_home_group"
    )

    val nativeResult = NativeAdGroup(
        BuildConfig.native_result to "native_result",
        name = "native_result_group"
    )

    val interScan = InterstitialAdGroup(
        BuildConfig.inter_scan to "inter_scan",
        name = "inter_scan_group"
    )

    val interCreate = InterstitialAdGroup(
        BuildConfig.inter_create to "inter_create",
        name = "inter_create_group"
    )

    val nativeCreate = NativeAdGroup(
        BuildConfig.native_create to "native_create",
        name = "native_create_group"
    )

    val nativeBottomSheet = NativeAdGroup(
        BuildConfig.native_bottom_sheet to "native_bottom_sheet",
        name = "native_home_group"
    )
}