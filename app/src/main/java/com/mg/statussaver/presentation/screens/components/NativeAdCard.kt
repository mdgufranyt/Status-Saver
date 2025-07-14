package com.mg.statussaver.presentation.screens.components

import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mg.statussaver.R

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/2247696110"
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        onDispose {
            nativeAd?.destroy()
        }
    }

    nativeAd?.let { ad ->
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adView = inflater.inflate(
                    R.layout.native_ad_card,
                    null
                ) as NativeAdView

                // Bind views
                val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                val ctaButton = adView.findViewById<Button>(R.id.ad_call_to_action)

                headlineView.text = ad.headline
                adView.headlineView = headlineView

                adView.mediaView = mediaView

                ctaButton.text = ad.callToAction
                adView.callToActionView = ctaButton

                adView.setNativeAd(ad)
                adView
            }
        )
    }
}