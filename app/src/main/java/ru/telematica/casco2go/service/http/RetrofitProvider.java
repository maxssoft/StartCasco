package ru.telematica.casco2go.service.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by alexander on 8/28/17.
 */

public class RetrofitProvider {

    private final static String HTTP_CACHE_DIR = "http.cache";
    private final static int CACHE_SIZE_BYTES = 1024 * 1024 * 2;
    private final static String testUrl = "http://35.205.134.193/";
    private final static String releaseUrl = "https://ua.kasko2go.com/";  // debug_
    public final static String baseUrl = testUrl;

    public final static String scroingTcpHost = "35.205.193.18";
    public final static int scroingTcpPort = 50000;

    private Retrofit retrofit;
    private OkHttpClient httpClient;

    private TelematicaApi apiService;

    public TelematicaApi getService() {
        if (retrofit == null) {
            retrofit = buildRetrofit(true);
        }
        if (apiService == null) {
            apiService = retrofit.create(TelematicaApi.class);
        }
        return apiService;
    }

    public RetrofitProvider(Context context) {
        cacheDirectory = new File(context.getCacheDir(), HTTP_CACHE_DIR);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
            cacheDirectory.mkdir();
        }
    }

    private File cacheDirectory;

    private OkHttpClient getOkHttpClient(boolean cacheEnabled) {
        if (httpClient == null) {
            httpClient = buildOkHttpClient(cacheEnabled);
        }
        return httpClient;
    }

    private OkHttpClient buildOkHttpClient(boolean cacheEnabled) {
        SSLContext sc = null;
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());

        } catch (Exception e) {

        }

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        if (cacheEnabled && cacheDirectory != null)
            builder.cache(new Cache(cacheDirectory, CACHE_SIZE_BYTES));

        builder.sslSocketFactory(sc.getSocketFactory());
        builder.followRedirects(false);
        builder.connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);
        builder.authenticator(new AuthenticationInterceptor());

        httpClient = builder.build();
        return httpClient;
    }

    private Retrofit buildRetrofit(boolean cacheEnabled) {
        OkHttpClient httpClient = getOkHttpClient(cacheEnabled);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(createGsonConverter())
                .client(httpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private Converter.Factory createGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeAdapter(OfferFeature.class, new OfferFeatureDeserializer());
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }

}
