package ru.telematica.casco2go.ui.base

/**
 * Created by m.sidorov on 29.04.2018.
 */
interface BaseView {
    fun showError(message: String, error: Throwable? = null)
    fun hideKeyboard()
}