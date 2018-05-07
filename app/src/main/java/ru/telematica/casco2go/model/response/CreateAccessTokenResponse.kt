package ru.telematica.casco2go.model.response

/**
 * Created by m.sidorov on 01.05.2018.
 */
class CreateAccessTokenResponse {

    val accesstoken: AccessToken? = null
    val session: Long? = null

    inner class AccessToken {
        val access_token: String? = null
        val refresh_token: Int = 0
        val user_id: Long? = null
        val expires_in: Long = 0
        val created: Long = 0
        val scope: String? = null
        val user_id_long: Long? = null
    }

}