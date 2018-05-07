package ru.telematica.casco2go.service.http

import ru.telematica.casco2go.App
import ru.telematica.casco2go.model.AuthData
import ru.telematica.casco2go.model.request.CreateTokenRequest
import ru.telematica.casco2go.model.response.CreateAccessTokenResponse
import ru.telematica.casco2go.repository.ConfigRepository
import ru.telematica.casco2go.utils.isNull
import java.io.IOException
import java.util.*
import android.os.AsyncTask.execute
import okhttp3.*
import ru.telematica.casco2go.model.AuthDataConst


/**
 * Created by m.sidorov on 06.05.2018.
 */
class AuthenticationInterceptor : Authenticator {

    private val ONE_HOUR: Long = 60 * 60 * 1000
    private val AccessCompanyID = 1095
    private val AccessCompanySecret = "0a744d8afe0c912f8c77d710a2cb0e51"

    override fun authenticate(route: Route?, response: Response?): Request? {
        response?.let {
            if (response.code() == 401) {
                val autHeader = requestAuthHeader()
                return response.request().newBuilder().header("Authorization", autHeader).build()
            }
        }

        return null
    }


/*
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val token = getToken()
        val request = chain.request().newBuilder().header("Authorization", token).build()

        return chain.proceed(request)
    }
*/

    @Synchronized
    private fun requestAuthHeader(): String {
        var authData = ConfigRepository.authData
        // Если токен существовал и истек срок действия, то запрашиваем новый
        if (authData.isExpired()){
            authData = createAuthData(App.instance.httpService.createAccessToken(CreateTokenRequest(AccessCompanyID, AccessCompanySecret)))
            //authData = createAuthData(App.instance.httpService.refreshAccessToken(RefreshTokenRequest(AccessCompanyID, AccessCompanySecret, authData.refreshToken)))
        } else {
            authData = createAuthData(App.instance.httpService.createAccessToken(CreateTokenRequest(AccessCompanyID, AccessCompanySecret)))
        }

        return authData.getAuthHeader()
    }

    private fun createAuthData(response: CreateAccessTokenResponse): AuthData {
        val newData = AuthData()
        with(newData) {
            sessionID = response?.session.isNull(0)
            token = response?.accesstoken?.access_token.isNull("")
            userId = response?.accesstoken?.user_id_long.isNull(0)
            refreshToken = response?.accesstoken?.refresh_token.isNull(0)

            var expireTime: Long = (response?.accesstoken?.expires_in.isNull(0) - response?.accesstoken?.created.isNull(0)) * 1000
            if (expireTime < AuthDataConst.MIN_EXPIRE_PERIOD){
                expireTime = AuthDataConst.MIN_EXPIRE_PERIOD
            }
            expiredDate = System.currentTimeMillis() + expireTime
        }
        ConfigRepository.writeAuthData(App.instance, newData)
        return ConfigRepository.authData
    }

}