package ru.telematica.casco2go.model.eventbus

import android.location.Location

/**
 * Created by m.sidorov on 02.05.2018.
 */
class LocationConnectedEvent(val connected: Boolean, val location: Location?) {
}
