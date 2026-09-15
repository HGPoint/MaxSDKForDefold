#if defined(DM_PLATFORM_IOS)

#include "../maxsdk_private.h"
#include "../maxsdk_callback_private.h"
#include "MADefoldPlugin.h"
#import <AppLovinSDK/AppLovinSDK.h>

namespace dmAppLovinMax {
static MADefoldPlugin *PluginInstance = nil;

extern "C" void ForwardIOSEvent(int type, NSString * json)
{
    const char *JsonCString = [json UTF8String];
    AddToQueueCallback((MessageId)type, JsonCString);
}

void Initialize_Ext(){
}
void OnActivateApp(){}
void OnDeactivateApp(){}


void Initialize(const char* sdkKey, const char* privacyPolicyUrl, const char* termsOfUseUrl, const char* userId, bool debugUserGeography) {
    NSString* userIdString = [NSString stringWithUTF8String:userId];
    NSString* sdkKeyString = [NSString stringWithUTF8String:sdkKey];
    NSString* policyUrlString = nil;
    NSString* termsOfUseUrlString = nil;
    if(privacyPolicyUrl!=NULL){
        policyUrlString = [NSString stringWithUTF8String:privacyPolicyUrl];
    }
    if(termsOfUseUrl!=NULL){
        termsOfUseUrlString = [NSString stringWithUTF8String:termsOfUseUrl];
    }
    PluginInstance = [[MADefoldPlugin alloc] init:&ForwardIOSEvent sdkKey:sdkKeyString privacyPolicyUrl:policyUrlString termsOfUseUrl:termsOfUseUrlString userId:userIdString debugUserGeography:debugUserGeography];
}

void SetMuted(bool muted){
    if(muted){
        [ALSdk shared].settings.muted = YES;
    }
    else{
        [ALSdk shared].settings.muted = NO;
    }
}

void SetVerboseLogging(bool verbose){
    if(verbose){
        [ALSdk shared].settings.verboseLoggingEnabled = YES;
    }
    else{
        [ALSdk shared].settings.verboseLoggingEnabled = NO;
    }
}

void SetHasUserConsent(bool hasConsent){
    if(hasConsent){
        [ALPrivacySettings setHasUserConsent: YES];
    }
    else{
        [ALPrivacySettings setHasUserConsent: NO];
    }
}

bool HasUserConsent()
{
    return [ALPrivacySettings hasUserConsent];
}

void SetIsAgeRestrictedUser(bool ageRestricted){
    // if(ageRestricted){
    //     [ALPrivacySettings setIsAgeRestrictedUser: YES];
    // }
    // else{
    //     [ALPrivacySettings setIsAgeRestrictedUser: NO];
    // }
}

void SetDoNotSell(bool doNotSell){
    if(doNotSell){
        [ALPrivacySettings setDoNotSell: YES];
    }
    else{
        [ALPrivacySettings setDoNotSell: NO];
    }
}

void OpenMediationDebugger(){
    [[ALSdk shared] showMediationDebugger];
}

void LoadInterstitial(const char* unitId){
    [PluginInstance loadInterstitialWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId]];
}

void ShowInterstitial(const char* unitId, const char* placement){
    if(placement != NULL)
        [PluginInstance showInterstitialWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId] placement:[[NSString alloc] initWithUTF8String: placement]];
    else
        [PluginInstance showInterstitialWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId] placement:NULL];
}

bool IsInterstitialLoaded(const char* unitId){
    [PluginInstance isInterstitialReadyWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId]];
}

void LoadRewarded(const char* unitId){
    [PluginInstance loadRewardedAdWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId]];
}

void ShowRewarded(const char* unitId, const char* placement){
    if(placement != NULL)
        [PluginInstance showRewardedAdWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId] placement:[[NSString alloc] initWithUTF8String: placement]];
    else
        [PluginInstance showRewardedAdWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId] placement:NULL];
}

bool IsRewardedLoaded(const char* unitId){
    [PluginInstance isRewardedAdReadyWithAdUnitIdentifier:[[NSString alloc] initWithUTF8String: unitId]];
}

void LoadBanner(const char* unitId, BannerSize bannerSize){
}
void DestroyBanner(){
}
void ShowBanner(BannerPosition bannerPos, const char* placement){
}
void HideBanner(){
}
void ShowConsentFlow(){
    [PluginInstance showConsentFlow];
}
bool IsUserGdprRegion(){
        return [PluginInstance isUserGdprRegion];
}
bool IsBannerLoaded(){
        return false;
}
bool IsBannerShown(){
        return false;
}

} //namespace dmAppLovinMax
#endif
