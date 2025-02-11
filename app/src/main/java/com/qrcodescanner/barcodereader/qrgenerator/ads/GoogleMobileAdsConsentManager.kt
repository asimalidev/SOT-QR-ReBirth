/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {
  private val consentInformation: ConsentInformation =
    UserMessagingPlatform.getConsentInformation(context)

  /** Interface definition for a callback to be invoked when consent gathering is complete. */
  fun interface OnConsentGatheringCompleteListener {
    fun consentGatheringComplete(error: FormError?)
  }

  /** Helper variable to determine if the app can request ads. */
  val canRequestAds: Boolean
    get() = consentInformation.canRequestAds()

  /** Helper variable to determine if the privacy options form is required. */
  val isPrivacyOptionsRequired: Boolean
    get() =
      consentInformation.privacyOptionsRequirementStatus ==
              ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

  // Helper method to check if the user is in a region that requires consent
  fun isUserInConsentRequiredRegion(): Boolean {
    return consentInformation.isConsentFormAvailable
  }

  /**
   * Helper method to call the UMP SDK methods to request consent information and load/show a
   * consent form if necessary.
   */
  fun gatherConsent(
    activity: Activity,
    removeSlowInternetCallBack: (() -> Unit)? = null,
    onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
  ) {
    val debugSettings = ConsentDebugSettings.Builder(activity)
      .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
      .addTestDeviceHashedId("7F8A2B2F68066A0B5426833E7D0C9691")
      .addTestDeviceHashedId("7CA0708269BCB21A935D0E5EFC153D14")
      .addTestDeviceHashedId("3F8FB4EE64D851EDBA704E705EC63A62")
      .addTestDeviceHashedId("6151093C0F4119C1D1CA5942D7D332F7")
      .addTestDeviceHashedId("9AC476D76BC0872ED44ED0CA9CFA5B2F")
      .addTestDeviceHashedId("C8A44471B6FC220F55047A8656CF7FDC")
      .build()

    val params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

    // Requesting an update to consent information should be called on every app launch.
    consentInformation.requestConsentInfoUpdate(
      activity,
      params,
      {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
          Log.e("SplashScreen", "SplashScreen: loadAndShowConsentFormIfRequired: Gathered")
          onConsentGatheringCompleteListener.consentGatheringComplete(formError)
        }
        Log.e("SplashScreen", "SplashScreen: loadAndShowConsentFormIfRequired")
        removeSlowInternetCallBack!!.invoke()
      },
      { requestConsentError ->
        Log.e("SplashScreen", "SplashScreen: requestConsentError: "+requestConsentError.message)
        onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
      }
    )
  }

  /** Helper method to call the UMP SDK method to show the privacy options form. */
  fun showPrivacyOptionsForm(
    activity: Activity,
    onConsentFormDismissedListener: OnConsentFormDismissedListener
  ) {
    UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
  }

  companion object {
    @Volatile private var instance: GoogleMobileAdsConsentManager? = null

    fun getInstance(context: Context) =
      instance
        ?: synchronized(this) {
          instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
  }
}
