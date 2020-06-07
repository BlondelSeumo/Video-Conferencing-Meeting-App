package aculix.meetly.app

import aculix.meetly.app.di.appModule
import aculix.meetly.app.di.mainModule
import aculix.meetly.app.di.meetingHistoryModule
import aculix.meetly.app.sharedpref.AppPref
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.Kotpref
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class Meetly : Application() {

    companion object {
        var isAdEnabled = false
    }

    override fun onCreate() {
        super.onCreate()
        isAdEnabled = resources.getBoolean(R.bool.enable_ads)

        initializeKotPref()
        setThemeMode()
        initializeKoin()

        if (isAdEnabled) initializeAdmob()
    }

    private fun initializeKotPref() {
        Kotpref.init(this)
    }

    private fun setThemeMode() {
        if (AppPref.isLightThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger()
            androidContext(this@Meetly)
            modules(
                listOf(
                    appModule,
                    mainModule,
                    meetingHistoryModule
                )
            )
        }
    }

    private fun initializeAdmob() {
        MobileAds.initialize(this) {
            val testDeviceIds = listOf("7BD04413716C0B3DD5C73F814E02D21A")
            val configuration =
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
}