package ru.telematica.casco2go.model

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

    var scoringData: ScoringData
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

    fun getCarPrice(): Int {
        return scoringData?.carPrice.isNull(0)
    }

    constructor(carPrice: Int){
        this.scoringData = ScoringData(carPrice)
    }

    constructor(tripData: TripData) : this(tripData.scoringData.carPrice)

    fun start(): TripData {
        started = true
        finished = false;
        startTime = System.currentTimeMillis()
        finishTime = 0
        scoringData = ScoringData(scoringData.carPrice)
        return this
    }

    fun finish(): TripData {
        finishTime = System.currentTimeMillis()
        finished = true
        return this
    }

}

class ScoringData(val carPrice: Int) {

    var timeTripSec: Int = 0

    var timeInTravelSec: Int = 0

    var timeInTrafficSec: Int = 0

    var drivingLevel: Int = 0

    var tripCost: Float = 0.0f

    fun getDiscount(): Int{
        when(drivingLevel){
            in 50..64 -> return 10
            in 65..79 -> return 30
            in 80..100 -> return 45
            else -> return 0
        }
    }

    fun getOneMinuteCost(): Float{
        when(carPrice){
            in 5000..9999 -> return 0.005f
            in 10000..14999 -> return 0.010f
            in 15000..19999 -> return 0.014f
            in 20000..24999 -> return 0.018f
            in 25000..29999 -> return 0.021f
            in 30000..34999 -> return 0.024f
            in 35000..39999 -> return 0.026f
            in 40000..44999 -> return 0.028f
            in 45000..49999 -> return 0.029f
            in 50000..149999 -> return (carPrice * 0.00006f) / 100f
            in 150000..99999999 -> return (carPrice * 0.00006f) / 100f
            else -> return 0.0f
        }
    }

}