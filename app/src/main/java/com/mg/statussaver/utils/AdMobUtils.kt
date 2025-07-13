package com.mg.statussaver.utils


import android.app.Activity
import android.app.Application
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
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.*

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

    fun loadRewardedAd(
        context: Context,
        adUnitId: String,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
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

    fun showRewardedAd(
        activity: Activity,
        onReward: (RewardItem) -> Unit = {},
        onClosed: () -> Unit = {}
    ) {
        rewardedAd?.show(activity) { rewardItem ->
            onReward(rewardItem)
        }
        rewardedAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onClosed()
                }
            }
    }
}


object AppOpenAdManager {
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0

    // Test Ad Unit ID. Replace with your real ID in production.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    /**
     * Loads a new App Open Ad.
     */
    fun loadAd(context: Context) {
        // Agar ad already load ho raha hai ya available hai, to naya load na karein.
        if (isShowingAd || isAdAvailable()) {
            return
        }

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                    // Log ya toast add kar sakte hain for debugging
                    // Log.d("AppOpenAdManager", "Ad was loaded.")
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    appOpenAd = null
                    // Log ya toast add kar sakte hain for debugging
                    // Log.d("AppOpenAdManager", "Ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows the ad if it's available and not already showing.
     * @param activity The activity that will show the ad.
     */
    fun showAdIfAvailable(activity: Activity) {
        // Agar ad show ho raha hai ya available nahi hai, toh kuch na karein.
        if (isShowingAd || !isAdAvailable()) {
            // Naya ad load karne ki koshish karein, shayad pichla fail ho gaya ho.
            loadAd(activity.application)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Ad dismiss hone ke baad, resources ko free karein aur naya ad load karein.
                appOpenAd = null
                isShowingAd = false
                loadAd(activity.application)
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                // Ad show hone mein fail ho gaya, resources free karein aur naya ad load karein.
                appOpenAd = null
                isShowingAd = false
                loadAd(activity.application)
            }

            override fun onAdShowedFullScreenContent() {
                // Ad ab screen par dikh raha hai.
                isShowingAd = true
            }
        }
        appOpenAd?.show(activity)
    }

    /**
     * Checks if an ad is available to show. Ad is considered available if it's not null
     * and was loaded less than 4 hours ago.
     */
    private fun isAdAvailable(): Boolean {
        val wasLoadedInTheLastFourHours = Date().time - loadTime < 4 * 60 * 60 * 1000 // 4 hours
        return appOpenAd != null && wasLoadedInTheLastFourHours
    }

    // onAppBackgrounded() method ki ab zaroorat nahi hai. Hum isse hata sakte hain.
    // fun onAppBackgrounded() { ... }
}
