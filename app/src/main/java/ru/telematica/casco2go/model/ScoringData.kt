package ru.telematica.casco2go.model

import ru.telematica.casco2go.R
import ru.telematica.casco2go.repository.ConfigRepository
import ru.telematica.casco2go.ui.fragments.FinishTripFragment
import java.util.*

/**
 * Created by m.sidorov on 05.05.2018.
 */
class ScoringData {

    enum class TripStatus {FAILED, LEVEL50, LEVEL65, LEVEL80}

    var startTimeS: String? = null

    var startTime: Date? = null

    var finishTime: Date? = null

    var carPrice: Int = 0

    var timeTripSec: Int = 0

    var timeInTravelSec: Int = 0

    var timeInTrafficSec: Int = 0

    var drivingLevel: Int = 0

    var tripCost: Float = 0.0f

    init {
        carPrice = ConfigRepository.carPrice
    }

    val tripStatus: TripStatus
        get() {
            return when (drivingLevel) {
                in 50..64 -> TripStatus.LEVEL50
                in 65..79 -> TripStatus.LEVEL65
                in 80..100 -> TripStatus.LEVEL80
                else -> TripStatus.FAILED
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

    fun getColorResId(): Int{
        when (tripStatus) {
            TripStatus.FAILED -> return R.color.error
            TripStatus.LEVEL50 -> return R.color.score_orange
            TripStatus.LEVEL65 -> return R.color.score_yellow
            TripStatus.LEVEL80 -> return R.color.score_green
            else -> return R.color.error
        }
    }
}