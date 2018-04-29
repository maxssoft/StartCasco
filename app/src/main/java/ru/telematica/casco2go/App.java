package ru.telematica.casco2go;

import android.support.v7.app.AppCompatDelegate;

import ru.telematica.casco2go.service.http.HttpService;
import ru.telematica.casco2go.service.http.RetrofitProvider;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class App extends android.support.multidex.MultiDexApplication {

    private static App INSTANCE = null;

    public static App getInstance(){
        return INSTANCE;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private HttpService httpService;
    public HttpService getHttpService(){
        return httpService;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initInstance();
    }

    private void initInstance(){
        httpService = createHttpService();
        INSTANCE = this;
    }

    private HttpService createHttpService(){
        return new HttpService(new RetrofitProvider(this).getService());
    }


}
