package ru.telematica.casco2go.service.http;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import ru.telematica.casco2go.model.request.CreateTokenRequest;
import ru.telematica.casco2go.model.request.RefreshTokenRequest;
import ru.telematica.casco2go.model.response.CreateAccessTokenResponse;
import ru.telematica.casco2go.model.response.FinishTripResponse;
import ru.telematica.casco2go.model.response.HistoryResponse;
import ru.telematica.casco2go.model.response.StartTripResponse;
import ru.telematica.casco2go.model.response.StatusResponse;

/**
 * Created by m.sidorov on 22.02.2018.
 */

public interface TelematicaApi {

    @POST("api/users/signupscoringtest?silentMode=true")
    Call<CreateAccessTokenResponse> createToken(@Body CreateTokenRequest request);

    @PUT("api/users")
    Call<CreateAccessTokenResponse> refreshToken(@Body RefreshTokenRequest request);

    @GET("api/Common/GetStatus")
    Call<StatusResponse> getTimeZone(
            @Header("Authorization") String authToken,
            @Query("cityLat") double latitude,
            @Query("cityLon") double longitude);

    @POST("api/Journey/Start?CityAccidentness=1&tariffMode=1&CityWeatherMark=1&")
    Call<StartTripResponse> startTrip(
            @Header("Authorization") String authToken,
            @Query("CityLat") double latitude,
            @Query("CityLon") double longtitude,
            @Query("PlannedDurationSeconds") int maxDurationTrip,
            @Query("TimeZone") String timeZone,
            @Query("SessionID") long sessionId
    );

    @POST("api/Journey/Finish?ReducedDurationSeconds=0&Forced=true")
    Call<FinishTripResponse> finishTrip(@Header("Authorization") String authToken);

    @GET("api/Journey")
    //Call<ResponseBody> loadHistory(
    Call<HistoryResponse> loadHistory(
            @Header("Authorization") String authToken,
            @Query("UTCTo") String utcTo);

}
