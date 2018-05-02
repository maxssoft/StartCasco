package ru.telematica.casco2go.model.eventbus

import ru.telematica.casco2go.ui.fragments.FragmentTypes

/**
 * Created by m.sidorov on 29.04.2018.
 */
class OpenFragmentEvent(val fragmentType: FragmentTypes,
                        val addToBackStack: Boolean = false,
                        val clearBackStack: Boolean = false) {

}