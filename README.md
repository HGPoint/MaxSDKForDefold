# AppLovin Max SDK

This is a native extension for [Defold engine](http://www.defold.com) with partial implementation of [AppLovin Max SDK](https://www.applovin.com/max/)

Android and iOS native implementations are included, with partial API support. See the platform-specific limitations and iOS consent-flow instructions below.

Supported ad types (Android):
- [Interstitials](https://dash.applovin.com/documentation/mediation/android/getting-started/interstitials)
- [Rewarded Ads](https://dash.applovin.com/documentation/mediation/android/getting-started/rewarded-ads)

Banner/MREC API symbols are present, but currently not implemented on Android (no-op).

Used AppLovin SDK v13.6.4.

*Disclaimer: This extension is independent and unofficial, and not associated with AppLovin in any way.*

## Installation

You can use the Max SDK extension in your own project by adding this project as a [Defold library dependency](http://www.defold.com/manuals/libraries/).
Open your game.project file and in the dependencies field under project add:

>https://github.com/alexeyfeskov/defold-maxsdk/archive/master.zip

or point to the ZIP file of a [specific release](https://github.com/alexeyfeskov/defold-maxsdk/releases).

#### Android

Since some mediation adapters requires D8 support use Defold 1.2.185 or later

Add [SDK key from Applovin Dashboard](https://dash.applovin.com/docs/integration#androidEventTracking) and [Google AdMob app ID](https://support.google.com/admob/answer/7356431) to `game.project` file by adding lines:

```
[applovin]
sdk_key = YOUR_SDK_KEY_HERE

; Optional (used for Terms/Privacy consent flow configuration)
privacy_policy_url = https://example.com/privacy
terms_of_service_url = https://example.com/terms

; Optional: force GDPR geography for testing (0/1)
debug_user_geography = 0

[admob]
app_id = YOUR_ADMOB_APP_ID_HERE
```

## Example

See the [example folder](https://github.com/alexeyfeskov/defold-maxsdk/tree/master/example) for understand how to use extension. Especially [ads.gui_script](https://github.com/alexeyfeskov/defold-maxsdk/blob/master/example/ads.gui_script) file.

## LUA Api

Please, read original [Android API docs](https://dash.applovin.com/documentation/mediation/android/getting-started/integration)

```lua
---------------------------------------
-- Initialize SDK and start load ads --
---------------------------------------
if maxsdk then
    maxsdk.set_has_user_consent(true) -- GDPR
    maxsdk.set_is_age_restricted_user(false)
    maxsdk.set_do_not_sell(false) -- CCPA for all others mediated networks

    maxsdk.set_muted(false)
    maxsdk.set_verbose_logging(true)
    
    maxsdk.set_callback(maxsdk_callback)
    -- Optional: pass user_id (string)
    maxsdk.initialize("USER_ID")

end

-----------------------
-- Start loading ads --
-----------------------
if maxsdk then
    -- After SDK was initialized (`maxsdk.MSG_INITIALIZATION` event) - you can start loading ads
    maxsdk.load_interstitial(interstitial_ad_unit)
    maxsdk.load_rewarded(rewarded_ad_unit)

    -- Banner API is currently not implemented on Android
    -- maxsdk.load_banner(banner_ad_unit, maxsdk.SIZE_BANNER)

    -- To validate integration you can use Mediation Debugger
    -- https://dash.applovin.com/documentation/mediation/android/testing-networks/mediation-debugger
    maxsdk.open_mediation_debugger()
end

--------------
-- Show ads --
--------------
-- all `show_***` functions have optional `string` parameter to define placement
if maxsdk and maxsdk.is_interstitial_loaded() then
    maxsdk.show_interstitial()
end

if maxsdk and maxsdk.is_rewarded_loaded() then
    maxsdk.show_rewarded()
end

end

------------------------
-- Receive SDK events --
------------------------
function maxsdk_callback(self, message_id, message)
    if message_id == maxsdk.MSG_INITIALIZATION then
        print("MSG_INITIALIZATION")

    elseif message_id == maxsdk.MSG_CONSENT_FLOW then
        if message.event == maxsdk.EVENT_CONSENT_FLOW_COMPLETED then
            print("Consent flow completed")
        elseif message.event == maxsdk.EVENT_CONSENT_FLOW_FAILED then
            print("Consent flow failed", message.code, message.error, message.cmp_code, message.cmp_error)
        end

    elseif message_id == maxsdk.MSG_INTERSTITIAL then
        if message.event == maxsdk.EVENT_CLOSED then
            print("EVENT_CLOSED: Interstitial AD closed")
        elseif message.event == maxsdk.EVENT_CLICKED then
            print("EVENT_CLICKED: Interstitial AD clicked")
        elseif message.event == maxsdk.EVENT_FAILED_TO_SHOW then
            print("EVENT_FAILED_TO_SHOW: Interstitial AD failed to show", message.code, message.error)
        elseif message.event == maxsdk.EVENT_OPENING then
            print("EVENT_OPENING: Interstitial AD is opening")
        elseif message.event == maxsdk.EVENT_FAILED_TO_LOAD then
            print("EVENT_FAILED_TO_LOAD: Interstitial AD failed to load", message.code, message.error)
        elseif message.event == maxsdk.EVENT_FAILED_TO_LOAD_WATERFALL then
            print("EVENT_FAILED_TO_LOAD_WATERFALL:", message.code, message.error, message.ad_network)
        elseif message.event == maxsdk.EVENT_LOADED then
            print("EVENT_LOADED: Interstitial AD loaded. Network:", message.network)
        elseif message.event == maxsdk.EVENT_NOT_LOADED then
            print("EVENT_NOT_LOADED: can't call show_interstitial() before EVENT_LOADED", message.code, message.error)
        elseif message.event == maxsdk.EVENT_REVENUE_PAID then
            print("EVENT_REVENUE_PAID: Interstitial AD revenue: ", message.revenue, message.network)
        end

    elseif message_id == maxsdk.MSG_REWARDED then
        if message.event == maxsdk.EVENT_CLOSED then
            print("EVENT_CLOSED: Rewarded AD closed")
        elseif message.event == maxsdk.EVENT_FAILED_TO_SHOW then
            print("EVENT_FAILED_TO_SHOW: Rewarded AD failed to show", message.code, message.error)
        elseif message.event == maxsdk.EVENT_OPENING then
            print("EVENT_OPENING: Rewarded AD is opening")
        elseif message.event == maxsdk.EVENT_FAILED_TO_LOAD then
            print("EVENT_FAILED_TO_LOAD: Rewarded AD failed to load", message.code, message.error)
        elseif message.event == maxsdk.EVENT_LOADED then
            print("EVENT_LOADED: Rewarded AD loaded. Network:", message.network)
        elseif message.event == maxsdk.EVENT_NOT_LOADED then
            print("EVENT_NOT_LOADED: can't call show_rewarded() before EVENT_LOADED", message.code, message.error)
        elseif message.event == maxsdk.EVENT_EARNED_REWARD then
            print("EVENT_EARNED_REWARD: Reward: ", message.amount, message.type)
        elseif message.event == maxsdk.EVENT_REVENUE_PAID then
            print("EVENT_REVENUE_PAID: Rewarded AD revenue: ", message.revenue, message.network)
        end

    elseif message_id == maxsdk.MSG_BANNER then
        if message.event == maxsdk.EVENT_LOADED then
            print("EVENT_LOADED: Banner AD loaded. Network:", message.network)
        elseif message.event == maxsdk.EVENT_OPENING then
            print("EVENT_OPENING: Banner AD is opening")
        elseif message.event == maxsdk.EVENT_FAILED_TO_LOAD then
            print("EVENT_FAILED_TO_LOAD: Banner AD failed to load", message.code, message.error)
        elseif message.event == maxsdk.EVENT_FAILED_TO_LOAD_WATERFALL then
            print("EVENT_FAILED_TO_LOAD_WATERFALL:", message.code, message.error, message.ad_network)
        elseif message.event == maxsdk.EVENT_FAILED_TO_SHOW then
            print("EVENT_FAILED_TO_SHOW: Banner AD failed to show", message.code, message.error)
        elseif message.event == maxsdk.EVENT_EXPANDED then
            print("EVENT_EXPANDED: Banner AD expanded")
        elseif message.event == maxsdk.EVENT_COLLAPSED then
            print("EVENT_COLLAPSED: Banner AD coppalsed")
        elseif message.event == maxsdk.EVENT_CLICKED then
            print("EVENT_CLICKED: Banner AD clicked")
        elseif message.event == maxsdk.EVENT_CLOSED then
            print("EVENT_CLOSED: Banner AD closed")
        elseif message.event == maxsdk.EVENT_DESTROYED then
            print("EVENT_DESTROYED: Banner AD destroyed")
        elseif message.event == maxsdk.EVENT_NOT_LOADED then
            print("EVENT_NOT_LOADED: can't call show_banner() before EVENT_LOADED", message.code, message.error)
        elseif message.event == maxsdk.EVENT_REVENUE_PAID then
            print("EVENT_REVENUE_PAID: Banner AD revenue: ", message.revenue, message.network)
        end
    end
end
```

## Consent flow on iOS

`maxsdk.show_consent_flow()` opens the integrated CMP for an existing user. Call it from a privacy-settings button after receiving `MSG_INITIALIZATION`:

```lua
-- Run from a user action after SDK initialization.
maxsdk.show_consent_flow()
```

The result is delivered to the callback registered with `maxsdk.set_callback()` as `MSG_CONSENT_FLOW`. On iOS, an early call reports `EVENT_CONSENT_FLOW_FAILED` with `code = -1`; SDK failures include the SDK's `code` and `error`, plus the underlying CMP's `cmp_code` and `cmp_error`, and are also logged natively. Completion does not indicate whether the user granted consent.

For the automatic consent flow during initialization, set `applovin.privacy_policy_url` in the consuming game's `game.project` before initialization. The bundled example does not supply this URL. The iOS Podfile includes Google Mobile Ads, which provides the Google UMP dependency. Configure and publish a GDPR message for the matching iOS app in AdMob's Privacy & messaging settings. Check the CMP entry in `maxsdk.open_mediation_debugger()` to verify the integration.

To test outside a GDPR region, set `applovin.debug_user_geography = 1` or call `maxsdk.set_debug_user_geography(true)` before `maxsdk.initialize()`. This extension applies that setting only when `applovin.privacy_policy_url` is configured. Reinstall the app to retest the automatic first-user flow. Disable the debug override for release builds.

See the [AppLovin iOS consent-flow guide](https://support.applovin.com/en/max/ios/overview/terms-and-privacy-policy-flow) for CMP configuration and testing. Rebuild the iOS native extension after changing its source; a game that depends on a released ZIP will continue using that ZIP until its dependency is updated.

Feel free to push a Pull Request with other features implementation.
