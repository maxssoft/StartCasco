package ru.telematica.casco2go

import android.support.v7.app.AppCompatDelegate
import ru.telematica.casco2go.service.http.HttpService
import ru.telematica.casco2go.service.http.RetrofitProvider

/**
 * Created by m.sidorov on 29.04.2018.
 */
class App : android.support.multidex.MultiDexApplication() {

    companion object {
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
        initInstance()
    }

    private fun initInstance() {
        instance = this
        httpService = createHttpService()
    }

    private fun createHttpService(): HttpService {
        return HttpService(RetrofitProvider(this).service)
    }

}