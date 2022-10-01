package com.mytaxi.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.ixidev.gdpr.GDPRChecker;

import java.util.Map;

public class BannerAds {
    public static void showBannerAds(Context context, LinearLayout mAdViewLayout) {

        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }
            }
        });

        if (Constant.isBanner) {
            if (Constant.isAdMobBanner) {
                AdView mAdView = new AdView(context);
                mAdView.setAdSize(AdSize.BANNER);
                mAdView.setAdUnitId(Constant.bannerId);
                AdRequest.Builder builder = new AdRequest.Builder();
                GDPRChecker.Request request = GDPRChecker.getRequest();
                if (request == GDPRChecker.Request.NON_PERSONALIZED) {

                    Bundle extras = new Bundle();
                    extras.putString("npa", "1");
                    builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
                }
                mAdView.loadAd(builder.build());
                mAdViewLayout.addView(mAdView);
                mAdViewLayout.setGravity(Gravity.CENTER);
            } else {
                com.facebook.ads.AdView adView = new com.facebook.ads.AdView(context, Constant.bannerId, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                adView.loadAd();
                mAdViewLayout.addView(adView);
                mAdViewLayout.setGravity(Gravity.CENTER);
            }
        } else {
            mAdViewLayout.setVisibility(View.GONE);
        }
    }
}
