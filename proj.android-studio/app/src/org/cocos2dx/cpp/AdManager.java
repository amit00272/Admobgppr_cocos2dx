package org.cocos2dx.cpp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;


public class AdManager extends AdListener {

    private AdView smallBannerView=null;
    private InterstitialAd interstitialView=null;

    private RelativeLayout relativeLayout;
    private RelativeLayout.LayoutParams layoutParams1;
    private RelativeLayout.LayoutParams layoutParamsTop;
    private RelativeLayout.LayoutParams layoutParamsBottom;
    public int PERSONALISED_ADS = 1;
    public int NONPERSONALISED_ADS = 0;
    public int userchoice = 0;
    private Activity activity;

    private boolean is_OnTOP = false;
    ConsentForm form;

    public String APP_ID = "ca-app-pub-9505496084710920~2673717262" ;
    public String Banner_ID = "ca-app-pub-9505496084710920/3406013301";
    public String Interstitial_id = "ca-app-pub-9505496084710920/2472205582";
    public String Pub_id = "pub-9505496084710920";
    public boolean is_Testing = true;
    public String test_device_id = "1D54CCCC3560BAB0F49863C2CEDDB07F";

    public AdManager(final Activity context){

        activity = context;

        checkForConsent();

        MobileAds.initialize(activity,APP_ID);

    }



    private void loadInterstitial(){


        if(userchoice == NONPERSONALISED_ADS){
            interstitialView.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle()).build());
         return;
        }
        interstitialView.loadAd(new AdRequest.Builder().build());

    }

    public void showInterstitial(){

        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (interstitialView.isLoaded()) {
                    interstitialView.show();
                } else {
                    loadInterstitial();
                }
            }
        });
    }

    public boolean isInterstitialAvailable(){


        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (interstitialView.isLoaded()) {
                    interstitialView.show();
                } else {
                    //Log.d(TAG, "Interstitial ad is not loaded yet");
                }
            }
        });

        return  true;
    }

    public void showBannerOnTop() {
        activity.runOnUiThread(new Runnable() {
            public void run() {

                is_OnTOP = true;
                relativeLayout.removeAllViewsInLayout();
                relativeLayout.addView(smallBannerView, layoutParamsTop);
            }
        });
 }

    public void showBannerOnBottom() {
        activity.runOnUiThread(new Runnable() {
            public void run() {

                is_OnTOP = false;
                relativeLayout.removeAllViewsInLayout();
                relativeLayout.addView(smallBannerView, layoutParamsBottom);
            }
        });
    }

    public void showBanner(){

        activity.runOnUiThread(new Runnable() {
            public void run() {

                if(is_OnTOP){
                   showBannerOnTop();
                }else{
                   showBannerOnBottom();
                }
            }
        });

    }

    public void HideBanner() {
        activity.runOnUiThread(new Runnable() {
            public void run() {

                relativeLayout.removeAllViewsInLayout();

            }
        });
    }

    private void checkForConsent() {

        ConsentInformation consentInformation = ConsentInformation.getInstance(activity);

        if(is_Testing) {
            ConsentInformation.getInstance(activity).addTestDevice(test_device_id);
            ConsentInformation.getInstance(activity).
                    setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);

        }
        String[] publisherIds = {Pub_id};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Log.d(TAG, "Showing Personalized ads");
                        showPersonalizedAds();
                        break;
                    case NON_PERSONALIZED:
                        Log.d(TAG, "Showing Non-Personalized ads");
                        showNonPersonalizedAds();
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Requesting Consent");
                        if (ConsentInformation.getInstance(activity.getApplicationContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            showPersonalizedAds();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });
    }

    private void requestConsent() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            /*
            watch this video how to create privacy policy in mint
            https://www.youtube.com/watch?v=lSWSxyzwV-g&t=140s
            */
            privacyUrl = new URL("https://your.privacy.url/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(activity, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded");
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed");
                        if (userPrefersAdFree) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree");
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again");
                            switch (consentStatus) {
                                case PERSONALIZED:
                                    showPersonalizedAds();break;
                                case NON_PERSONALIZED:
                                    showNonPersonalizedAds();break;
                                case UNKNOWN:
                                    showNonPersonalizedAds();break;
                            }

                        }
                        // Consent form was closed.
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription);
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                //.withAdFreeOption()
                .build();
        form.load();
    }

    /*
    want test your app watch this video https://youtu.be/_JOapnq8hrs?t=654
    */
    private void showPersonalizedAds() {
    /* this line code save consent status if you want to show your ads in next activty just get getConsentStatus and load ads accouding to status e.g.
      MainActivty2    ConsentStatus consentStatus =ConsentInformation.getInstance(this).getConsentStatus();
      if (consentStatus.toString().equals("NON_PERSONALIZED")) loadNonPersonlizedAds(); i hateeeeeeeeeet This new policy
    */

        // if you want to show interstital
       /*
         mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("Add_unit");
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(DEVICE_TEST_ID).build());

       */
        smallBannerView= new AdView(activity);
        smallBannerView.setAdSize(AdSize.SMART_BANNER);
        smallBannerView.setAdUnitId("ca-app-pub-9505496084710920/3406013301");

        interstitialView=new InterstitialAd(activity);
        interstitialView.setAdUnitId("ca-app-pub-9505496084710920/2472205582");
        interstitialView.setAdListener(this);
        relativeLayout=new RelativeLayout(activity);
        layoutParams1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParamsTop=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsTop.addRule(RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.TRUE);
        layoutParamsTop.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParamsBottom=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE);
        layoutParamsBottom.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        relativeLayout.addView(smallBannerView, is_OnTOP ? layoutParamsTop : layoutParamsTop);
        activity.addContentView(relativeLayout, layoutParams1);

        smallBannerView.loadAd(new AdRequest.Builder().build());
        userchoice =PERSONALISED_ADS;
        loadInterstitial();


    }

    private void showNonPersonalizedAds() {


        smallBannerView= new AdView(activity);
        smallBannerView.setAdSize(AdSize.SMART_BANNER);
        smallBannerView.setAdUnitId(Banner_ID);

        interstitialView=new InterstitialAd(activity);
        interstitialView.setAdUnitId(Interstitial_id);
        interstitialView.setAdListener(this);
        relativeLayout=new RelativeLayout(activity);
        layoutParams1=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParamsTop=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsTop.addRule(RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.TRUE);
        layoutParamsTop.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParamsBottom=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE);
        layoutParamsBottom.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        relativeLayout.addView(smallBannerView, is_OnTOP ? layoutParamsTop : layoutParamsTop);
        activity.addContentView(relativeLayout, layoutParams1);

        smallBannerView.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle()).build());
        userchoice = NONPERSONALISED_ADS;
        loadInterstitial();
        /* if you want show interstitial ad also
         mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("Add_unit");
        mInterstitialAd.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle()).addTestDevice(DEVICE_TEST_ID).build());

        */

    }
    public Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        return extras;
    }
    private void showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null");
        }
        if (form != null) {
            Log.d(TAG, "Showing consent form");
            form.show();
        } else {
            Log.d(TAG, "Not Showing consent form");
        }
    }

}
