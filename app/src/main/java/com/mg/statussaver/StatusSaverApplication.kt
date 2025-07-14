package com.mg.statussaver

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.mg.statussaver.utils.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StatusSaverApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var currentActivity: Activity? = null
    // Yeh flag track karega ki app background mein gaya tha ya nahi.
    private var appWasInBackground = false

    override fun onCreate() {
        super<Application>.onCreate()

        MobileAds.initialize(this) {}
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        AppOpenAdManager.loadAd(this)
    }

    /**
     * Yeh tab call hoga jab app background se foreground mein aayega.
     */
    override fun onStart(owner: LifecycleOwner) {
        // Agar aapke IDE ne quick fix se super() call add kiya hai, toh usse rehne dein.

        // Check karein ki kya app background se wapis aaya hai.
        if (appWasInBackground) {
            // Humne event ko "consume" kar liya hai, isliye flag ko turant reset karein.
            appWasInBackground = false
            currentActivity?.let {
                AppOpenAdManager.showAdIfAvailable(it)
            }
        }
    }

    /**
     * Yeh tab call hoga jab app foreground se background mein jayega.
     */
    override fun onStop(owner: LifecycleOwner) {
        // Flag set karein ki app ab background mein chala gaya hai.
        appWasInBackground = true
    }

    // --- ActivityLifecycleCallbacks Methods (Inme koi change nahi hai) ---

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}