package ru.telematica.casco2go.repository

import android.content.Context
import ru.telematica.casco2go.model.AuthData
import ru.telematica.casco2go.utils.JsonUtils

/**
 * @author m.sidorov
 */
object ConfigRepository {

    private val identApp = "app.preferences"
    private val identCarPrice = "car_price"
    private val identAuthData = "auth_data"

    private var readed: Boolean = false

    var carPrice: Int = 0
        private set
        get(){
            checkReaded()
            return field
        }

    var authData: AuthData = AuthData()
        private set
        get(){
            checkReaded()
            return field
        }

    @Synchronized
    fun read(context: Context){
        ConfigRepository.carPrice = readCarPrice(context)
        ConfigRepository.authData = readAuthData(context)
        readed = true
    }

    private fun checkReaded(){
        if (!readed){
            throw IllegalStateException(this::class.java.simpleName + ": Attempt to get parameter value with not initialized repository")
        }
    }

    @Synchronized
    fun writeCarPrice(context: Context, carPrice: Int){
        val editor = context.getSharedPreferences(identApp, Context.MODE_PRIVATE).edit()
        editor.putInt(identCarPrice, carPrice)
        editor.commit()
        ConfigRepository.carPrice = carPrice
    }

    @Synchronized
    fun writeAuthData(context: Context, authData: AuthData){
        val editor = context.getSharedPreferences(identApp, Context.MODE_PRIVATE).edit()
        editor.putString(identAuthData, JsonUtils.toJson(authData))
        editor.commit()
        this.authData = authData
    }

    private fun readCarPrice(context: Context): Int{
        return context.getSharedPreferences(identApp, Context.MODE_PRIVATE).getInt(identCarPrice, 0)
    }

    private fun readAuthData(context: Context): AuthData{
        val json = context.getSharedPreferences(identApp, Context.MODE_PRIVATE).getString(identAuthData, "")
        if (json.isBlank()){
            return AuthData()
        }
        try {
            return JsonUtils.parseJson(json, AuthData::class.java)
        } catch (e: Exception){
            return AuthData()
        }
    }

}