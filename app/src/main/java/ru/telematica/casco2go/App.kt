package ru.telematica.casco2go

import android.support.v7.app.AppCompatDelegate
import ru.telematica.casco2go.repository.ConfigRepository
import ru.telematica.casco2go.service.http.HttpService
import ru.telematica.casco2go.service.http.RetrofitProvider
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.core.CrashlyticsCore





/**
 * Created by m.sidorov on 29.04.2018.
 */
class App : android.support.multidex.MultiDexApplication() {

    companion object {

        val HISTORY_PAGE_SIZE = 20

        @JvmStatic
        lateinit var instance: App
            private set

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    lateinit var httpService: HttpService
        private set

    override fun onCreate() {
        super.onCreate()

        initCrashlytics()
        initInstance()
    }

    private fun initCrashlytics() {
        val builder = CrashlyticsCore.Builder()
        //builder.disabled(BuildConfig.DEBUG)
        val core = builder.build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
    }

    private fun initInstance() {
        instance = this
        ConfigRepository.read(this)
        httpService = createHttpService()
    }

    private fun createHttpService(): HttpService {
        return HttpService(RetrofitProvider(this).service)
    }

}