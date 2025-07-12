package com.mg.statussaver.utils


import android.app.Activity
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/9214589741" // Replace with your real Ad Unit ID
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    fun loadRewardedAd(context: Context, adUnitId: String, onLoaded: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                onLoaded()
            }
            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                rewardedAd = null
                onFailed(error.message)
            }
        })
    }

    fun showRewardedAd(activity: Activity, onReward: (RewardItem) -> Unit = {}, onClosed: () -> Unit = {}) {
        rewardedAd?.show(activity) { rewardItem ->
            onReward(rewardItem)
        }
        rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onClosed()
            }
        }
    }
}