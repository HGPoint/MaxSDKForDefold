package com.defold.maxsdk;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdWaterfallInfo;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxMediatedNetworkInfo;
import com.applovin.mediation.MaxNetworkResponseInfo;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinCmpService;
import com.applovin.sdk.AppLovinCmpError;
import com.applovin.sdk.AppLovinSdkConfiguration;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

public class AppLovinMaxJNI {

//    private static final String TAG = "AppLovinMaxJNI";

    public static native void maxsdkAddToQueue(int msg, String json);

    // CONSTANTS:
    // duplicate of enums from maxsdk_callback_private.h:
    private static final int MSG_INTERSTITIAL = 1;
    private static final int MSG_REWARDED = 2;
    private static final int MSG_BANNER = 3;
    private static final int MSG_INITIALIZATION = 4;
    private static final int MSG_CONSENT_FLOW = 5;

    private static final int EVENT_CLOSED = 1;
    private static final int EVENT_FAILED_TO_SHOW = 2;
    private static final int EVENT_OPENING = 3;
    private static final int EVENT_FAILED_TO_LOAD = 4;
    private static final int EVENT_LOADED = 5;
    private static final int EVENT_NOT_LOADED = 6;
    private static final int EVENT_EARNED_REWARD = 7;
    private static final int EVENT_COMPLETE = 8;
    private static final int EVENT_CLICKED = 9;
    private static final int EVENT_DESTROYED = 10;
    private static final int EVENT_EXPANDED = 11;
    private static final int EVENT_COLLAPSED = 12;
    private static final int EVENT_REVENUE_PAID = 13;
    private static final int EVENT_SIZE_UPDATE = 14;
    private static final int EVENT_FAILED_TO_LOAD_WATERFALL = 15;
    private static final int EVENT_CONSENT_FLOW_COMPLETED = 16;
    private static final int EVENT_CONSENT_FLOW_FAILED = 17;

    // duplicate of enums from maxsdk_private.h:
    private static final int SIZE_BANNER = 0;
    private static final int SIZE_LEADER = 1;
    private static final int SIZE_MREC = 2;

    private static final int POS_NONE = 0;
    private static final int POS_TOP_LEFT = 1;
    private static final int POS_TOP_CENTER = 2;
    private static final int POS_TOP_RIGHT = 3;
    private static final int POS_BOTTOM_LEFT = 4;
    private static final int POS_BOTTOM_CENTER = 5;
    private static final int POS_BOTTOM_RIGHT = 6;
    private static final int POS_CENTER = 7;


    private static final String MSG_KEY_EVENT = "event";
    private static final String MSG_KEY_IS_USER_GDPR_REGION = "is_user_gdpr_region";
    private static final String MSG_KEY_AD_NETWORK = "ad_network";
    private static final String MSG_KEY_REVENUE = "revenue";
    private static final String MSG_KEY_AD_UNIT_ID = "ad_unit_id";
    private static final String MSG_KEY_PLACEMENT = "placement";
    private static final String MSG_KEY_AD_NETWORK_PLACEMENT = "ad_network_placement";
    private static final String MSG_KEY_CODE = "code";
    private static final String MSG_KEY_ERROR = "error";
    private static final String MSG_KEY_X_POS = "x";
    private static final String MSG_KEY_Y_POS = "y";

    // END CONSTANTS

    // Fullscreen Ad Fields
    private final Map<String, MaxInterstitialAd> mInterstitials = new HashMap<>(2);
    private final Map<String, MaxRewardedAd> mRewardedAds = new HashMap<>(2);

    private final Activity mActivity;
    private Boolean isUserGdprRegion = false;

    public AppLovinMaxJNI(final Activity activity) {
        mActivity = activity;
    }

    public void initialize(String SdkKey, String PrivacyPolicyUrl, String TermsOfServiceUrl, String UserId, boolean DebugUserGeography) {

        AppLovinSdk sdk = AppLovinSdk.getInstance(mActivity);
        AppLovinSdkSettings settings = sdk.getSettings();
            settings.setUserIdentifier(UserId);

        if (PrivacyPolicyUrl != null && !PrivacyPolicyUrl.trim().isEmpty()) {
            settings.getTermsAndPrivacyPolicyFlowSettings().setEnabled(true);
            settings.getTermsAndPrivacyPolicyFlowSettings().setPrivacyPolicyUri(Uri.parse(PrivacyPolicyUrl));

            if (TermsOfServiceUrl != null && !TermsOfServiceUrl.trim().isEmpty()) {
                settings.getTermsAndPrivacyPolicyFlowSettings().setTermsOfServiceUri(Uri.parse(TermsOfServiceUrl));
            }

            if (DebugUserGeography) {
                settings.getTermsAndPrivacyPolicyFlowSettings().setDebugUserGeography(AppLovinSdkConfiguration.ConsentFlowUserGeography.GDPR);
            }
        }

        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(SdkKey, mActivity)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .build();

        sdk.initialize(initConfig, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration sdkConfig) {
                if (sdkConfig.getConsentFlowUserGeography() == AppLovinSdkConfiguration.ConsentFlowUserGeography.GDPR) {
                    isUserGdprRegion = true;
                }
                sendSimpleMessage(MSG_INITIALIZATION, EVENT_COMPLETE);
            }
        });
    }

    public boolean isUserGdprRegion() {
        return isUserGdprRegion;
    }

    public void showConsentFlow() {
        AppLovinCmpService cmpService = AppLovinSdk.getInstance(mActivity).getCmpService();

        cmpService.showCmpForExistingUser(mActivity, new AppLovinCmpService.OnCompletedListener() {
            @Override
            public void onCompleted(final AppLovinCmpError error) {
                if (null == error) {
                    // The CMP alert was shown successfully.
                    sendSimpleMessage(MSG_CONSENT_FLOW, EVENT_CONSENT_FLOW_COMPLETED);
                } else {
                    // Handle error case
                    sendConsentFlowErrorMessage(error);
                }
            }
        });
    }

    private MaxInterstitialAd retrieveInterstitial(String adUnitId) {
        MaxInterstitialAd result = mInterstitials.get(adUnitId);
        if (result == null) {
            result = new MaxInterstitialAd(adUnitId, mActivity);
            result.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    sendSimpleMessage(MSG_INTERSTITIAL, EVENT_LOADED, ad);
                }

                @Override
                public void onAdLoadFailed(String adUnitId, final MaxError maxError) {
                    sendFailedToLoadMessage(MSG_INTERSTITIAL, adUnitId, maxError);
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sendSimpleMessage(MSG_INTERSTITIAL, EVENT_OPENING, ad);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, final MaxError maxError) {
                    sendFailedToShowMessage(MSG_INTERSTITIAL, ad, maxError);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    sendSimpleMessage(MSG_INTERSTITIAL, EVENT_CLOSED, ad);
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    sendSimpleMessage(MSG_INTERSTITIAL, EVENT_CLICKED, ad);
                }
            });

            result.setRevenueListener(ad -> sendSimpleMessage(MSG_INTERSTITIAL, EVENT_REVENUE_PAID, ad));

            mInterstitials.put(adUnitId, result);
        }

        return result;
    }

    private MaxRewardedAd retrieveRewardedAd(String adUnitId) {
        MaxRewardedAd result = mRewardedAds.get(adUnitId);
        if (result == null) {
            result = MaxRewardedAd.getInstance(adUnitId, mActivity);
            result.setListener(new MaxRewardedAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    sendSimpleMessage(MSG_REWARDED, EVENT_LOADED, ad);
                }

                @Override
                public void onAdLoadFailed(String adUnitId, final MaxError maxError) {
                    sendFailedToLoadMessage(MSG_REWARDED, adUnitId, maxError);
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sendSimpleMessage(MSG_REWARDED, EVENT_OPENING, ad);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, final MaxError maxError) {
                    sendFailedToShowMessage(MSG_REWARDED, ad, maxError);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    sendSimpleMessage(MSG_REWARDED, EVENT_CLOSED, ad);
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    sendSimpleMessage(MSG_REWARDED, EVENT_CLICKED, ad);
                }
                @Override
                public void onUserRewarded(MaxAd ad, MaxReward reward) {
                    sendSimpleMessage(MSG_REWARDED, EVENT_EARNED_REWARD, ad);
                }
            });

            result.setRevenueListener(ad -> sendSimpleMessage(MSG_REWARDED, EVENT_REVENUE_PAID, ad));

            mRewardedAds.put(adUnitId, result);
        }

        return result;
    }

    public void onActivateApp() {
        // No implementation
    }

    public void onDeactivateApp() {
        // No implementation
    }

    public void setMuted(boolean muted) {
        AppLovinSdk.getInstance(mActivity).getSettings().setMuted(muted);
    }

    public void setVerboseLogging(boolean isVerboseLoggingEnabled) {
        AppLovinSdk.getInstance(mActivity).getSettings().setVerboseLogging(isVerboseLoggingEnabled);
    }

    public void setHasUserConsent(boolean hasUserConsent) {
        AppLovinPrivacySettings.setHasUserConsent(hasUserConsent, mActivity);
    }

    public boolean hasUserConsent() {
        return AppLovinPrivacySettings.hasUserConsent(mActivity);
    }

    public void setIsAgeRestrictedUser(boolean isAgeRestrictedUser) {
//        AppLovinPrivacySettings.setIsAgeRestrictedUser(isAgeRestrictedUser, mActivity);
    }

    public void setDoNotSell(boolean doNotSell) {
        AppLovinPrivacySettings.setDoNotSell(doNotSell, mActivity);
    }

    public void openMediationDebugger() {
        AppLovinSdk.getInstance(mActivity).showMediationDebugger();
    }

    // https://www.baeldung.com/java-json-escaping
    private String getJsonConversionErrorMessage(String messageText) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_ERROR, messageText);
            message = obj.toString();
        } catch (JSONException e) {
            message = "{ \"error\": \"Error while converting simple message to JSON.\" }";
        }
        return message;
    }

    private String getErrorMessage(final String adUnitId, final MaxError maxError) {
        return String.format("%s\n%s\nAdUnitId:%s", maxError.getMessage(), maxError.getMediatedNetworkErrorMessage(), adUnitId);
    }

    private String getErrorMessage(final MaxAd ad, MaxError maxError) {
        return String.format("%s\nFormat:%s AdUnitId:%s Network:%s",
                maxError.getMessage(), ad.getFormat(), ad.getAdUnitId(), ad.getNetworkName());
    }

    private void sendSimpleMessage(int msg, int eventId, MaxAd ad) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, eventId);
            obj.put(MSG_KEY_AD_NETWORK, ad.getNetworkName());
            obj.put(MSG_KEY_REVENUE, ad.getRevenue());
            obj.put(MSG_KEY_AD_UNIT_ID, ad.getAdUnitId());
            obj.put(MSG_KEY_PLACEMENT, ad.getPlacement());
            obj.put(MSG_KEY_AD_NETWORK_PLACEMENT, ad.getNetworkPlacement());
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(msg, message);
    }

    private void sendFailedToShowMessage(int msg, final MaxAd ad, MaxError maxError) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, EVENT_FAILED_TO_SHOW);
            obj.put(MSG_KEY_AD_NETWORK, ad.getNetworkName());
            obj.put(MSG_KEY_REVENUE, ad.getRevenue());
            obj.put(MSG_KEY_AD_UNIT_ID, ad.getAdUnitId());
            obj.put(MSG_KEY_CODE, maxError.getCode());
            obj.put(MSG_KEY_ERROR, getErrorMessage(ad, maxError));
            obj.put(MSG_KEY_PLACEMENT, ad.getPlacement());
            obj.put(MSG_KEY_AD_NETWORK_PLACEMENT, ad.getNetworkPlacement());
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(msg, message);
    }


    private void sendFailedToLoadMessage(int msg, String adUnitId, MaxError maxError) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, EVENT_FAILED_TO_LOAD);
            obj.put(MSG_KEY_CODE, maxError.getCode());
            obj.put(MSG_KEY_ERROR, getErrorMessage(adUnitId, maxError));
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(msg, message);

        MaxAdWaterfallInfo waterfall = maxError.getWaterfall();
        if (waterfall != null) {
            for (MaxNetworkResponseInfo networkResponse : waterfall.getNetworkResponses()) {
                MaxMediatedNetworkInfo network = networkResponse.getMediatedNetwork();
                if (network != null) {
                    String waterfall_message;
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put(MSG_KEY_EVENT, EVENT_FAILED_TO_LOAD_WATERFALL);
                        obj.put(MSG_KEY_CODE, networkResponse.getError().getCode());
                        obj.put(MSG_KEY_ERROR, getErrorMessage(adUnitId, maxError));
                        obj.put(MSG_KEY_AD_NETWORK, network.getName());
                        waterfall_message = obj.toString();
                    } catch (JSONException e) {
                        waterfall_message = getJsonConversionErrorMessage(e.getMessage());
                    }
                    maxsdkAddToQueue(msg, waterfall_message);
                }
            }
        }
    }

    private void sendSimpleMessage(int msg, int eventId) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, eventId);
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(msg, message);
    }

    private void sendNotLoadedMessage(int msg, String messageStr) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, EVENT_NOT_LOADED);
            obj.put(MSG_KEY_ERROR, messageStr);
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(msg, message);
    }

    private void sendConsentFlowErrorMessage(final AppLovinCmpError cmpError) {
        String message;
        try {
            JSONObject obj = new JSONObject();
            obj.put(MSG_KEY_EVENT, EVENT_CONSENT_FLOW_FAILED);
            obj.put(MSG_KEY_CODE, cmpError.getCode());
            obj.put(MSG_KEY_ERROR, cmpError.getMessage());
            message = obj.toString();
        } catch (JSONException e) {
            message = getJsonConversionErrorMessage(e.getMessage());
        }
        maxsdkAddToQueue(MSG_CONSENT_FLOW, message);
    }

//--------------------------------------------------
// Interstitial ADS

    public void loadInterstitial(final String unitId) {
        mActivity.runOnUiThread(() -> {
            final MaxInterstitialAd adInstance = retrieveInterstitial(unitId);
            adInstance.loadAd();
        });
    }

    public void showInterstitial(final String unitId, final String placement) {
        mActivity.runOnUiThread(() -> {
            if (isInterstitialLoaded(unitId)) {
                retrieveInterstitial(unitId).showAd(placement);
            } else {
                sendNotLoadedMessage(MSG_INTERSTITIAL,
                        "Can't show Interstitial AD that wasn't loaded.");
            }
        });
    }

    public boolean isInterstitialLoaded(final String unitId) {
        return retrieveInterstitial(unitId).isReady();
    }

//--------------------------------------------------
// Rewarded ADS
    public void loadRewarded(final String unitId) {
        mActivity.runOnUiThread(() -> {
            final MaxRewardedAd adInstance = retrieveRewardedAd(unitId);
            adInstance.loadAd();
        });
    }

    public void showRewarded(final String unitId, final String placement) {
        mActivity.runOnUiThread(() -> {
            if (isRewardedLoaded(unitId)) {
                retrieveRewardedAd(unitId).showAd(placement);
            } else {
                sendNotLoadedMessage(MSG_REWARDED,
                        "Can't show Rewarded AD that wasn't loaded.");
            }
        });
    }

    public boolean isRewardedLoaded(final String unitId) {
        return retrieveRewardedAd(unitId).isReady();
    }

//--------------------------------------------------
// Banner ADS
// WARNING: Banner ads are not implemented yet

    public void loadBanner(final String unitId, final int bannerSize) {
        Log.w("AppLovinMaxJNI", "loadBanner() is not implemented yet");
    }

    public void destroyBanner() {
        Log.w("AppLovinMaxJNI", "destroyBanner() is not implemented yet");
    }

    public void showBanner(final int pos, final String placement) {
        Log.w("AppLovinMaxJNI", "showBanner() is not implemented yet");
    }

    public void hideBanner() {
        Log.w("AppLovinMaxJNI", "hideBanner() is not implemented yet");
    }

    public boolean isBannerLoaded() {
        Log.w("AppLovinMaxJNI", "isBannerLoaded() is not implemented yet");
        return false;
    }

    public boolean isBannerShown() {
        Log.w("AppLovinMaxJNI", "isBannerShown() is not implemented yet");
        return false;
    }
}
