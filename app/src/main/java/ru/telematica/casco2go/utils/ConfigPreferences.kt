package ru.telematica.casco2go.utils

import android.content.Context

/**
 * @author m.sidorov
 */
object ConfigPreferences {

    private val identApp = "app.preferences"
    private val identCarPrice = "car_price"

    fun readCarPrice(context: Context): Int{
        return context.getSharedPreferences(identApp, Context.MODE_PRIVATE).getInt(identCarPrice, 0)
    }

    fun writeCarPrice(context: Context, carPrice: Int){
        val editor = context.getSharedPreferences(identApp, Context.MODE_PRIVATE).edit()
        editor.putInt(identCarPrice, carPrice)
        editor.commit()
    }

}