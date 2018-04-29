package ru.telematica.casco2go.service.http;

import android.text.TextUtils;
import android.util.Log;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import ru.telematica.casco2go.utils.Errors;

/**
 * Created by m.sidorov on 23.02.2018.
 */

public class HttpService {

    private static final String TAG = HttpService.class.getSimpleName();

    private final TelematicaApi apiService;

    private static final long SESSION_TIMEOUT = 2 * 60 * 60 * 1000;
    private String sessionId = "";
    private long sessionTime = 0;
    public boolean hasSessionId(){
        return TextUtils.isEmpty(sessionId) || System.currentTimeMillis() > sessionTime + SESSION_TIMEOUT;
    }
    public String getSessionId(){
        return sessionId;
    }


    public HttpService(TelematicaApi apiService){
        this.apiService = apiService;
    }

    public static String formatNetworkError(String methodName, Exception e){
        return String.format(String.format("http method [%s] - %s", methodName, Errors.getErrorMessage(e)));
    }

    public static class NetworkException extends RuntimeException {
        public NetworkException(String message){
            super(message);
        }
    }

    public static <T> void checkErrors(Response<T> response){
        if (!response.isSuccessful()){
            throw new NetworkException(String.format("network error [%d:%s]", response.code(), response.message()));
        }
        if (response.body() == null){
            throw new NetworkException("server json body is empty");
        }
    }

/*
    public Single<Track> getRadioActiveTrack(RadioEntity radio){
        return Single.just(radio)
                .subscribeOn(Schedulers.io())
                .map(radio1 -> {
                    if (radio1.isRestream()){
                        return new PlayerTrackInfo();
                    }
                    Response<PlayerTrackInfo> response = apiService.getPlayerInfo(radio1.getExtendedUrl()).execute();
                    checkErrors(response);
                    response.body().response_time = System.currentTimeMillis();
                    return response.body();
                })
                .map(playerTrackInfo -> playerTrackInfo.getTrack());
    }
*/

/*
    public SessionResponse getSession(){
        try {
            Response<SessionResponse> response = apiService.getSession(urls.getSession).execute();
            checkErrors(response);
            sessionId = response.body().session.rsid;
            sessionTime = System.currentTimeMillis();
            return response.body();
        } catch (Exception e){
            Log.e(TAG, formatNetworkError("getSession", e));
            throw new RuntimeException(formatNetworkError("getSession", e));
        }
    }
*/

}
