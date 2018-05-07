package ru.telematica.casco2go.model

import ru.telematica.casco2go.repository.ConfigRepository
import ru.telematica.casco2go.ui.fragments.FinishTripFragment
import ru.telematica.casco2go.utils.isNull
import java.util.*

/**
 * Created by m.sidorov on 29.04.2018.
 */
class TripData {

    var startTime: Long = 0
        private set

    var finishTime: Long = 0
        private set

    var started = false
        private set

    var finished = false
        private set

    var scoringData: ScoringData = ScoringData()
        private set

    val tripTime: Long
        get() {
            if (started){
                if (finished)
                    return finishTime - startTime
                else
                    return System.currentTimeMillis() - startTime
            } else
                return 0;
        }

    var gpsLevel: Int = 100

    @Synchronized
    fun start(): TripData {
        started = true
        finished = false;
        startTime = System.currentTimeMillis()
        finishTime = 0
        return this
    }

    @Synchronized
    fun finish(scoringData: ScoringData): TripData {
        finishTime = System.currentTimeMillis()
        finished = true
        this.scoringData = scoringData
        return this
    }

}

