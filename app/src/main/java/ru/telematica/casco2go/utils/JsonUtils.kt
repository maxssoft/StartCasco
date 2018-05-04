package ru.telematica.casco2go.utils

import com.google.gson.Gson

/**
 * @author m.sidorov
 */
object JsonUtils {

    fun toJson(objectToSave: Any): String{
        val gson = Gson()
        return gson.toJson(objectToSave)
    }

    fun <T> parseJson(json: String, objectClass: Class<T>): T{
        val gson = Gson()
        return gson.fromJson(json, objectClass)
    }

}