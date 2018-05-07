package ru.telematica.casco2go.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import org.greenrobot.eventbus.EventBus

import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.eventbus.*
import ru.telematica.casco2go.service.ScoringService
import ru.telematica.casco2go.utils.Animations
import ru.telematica.casco2go.model.TripData


/**
 * Created by m.sidorov on 01.05.2018.
 */
class FinishTripFragment: BaseFragment() {

    enum class TripStatus {FAILED, LEVEL50, LEVEL65, LEVEL80}

    @BindView(R.id.textTitle)
    lateinit var textTitle: TextView

    @BindView(R.id.nextTextInfo)
    lateinit var nextTextInfo: TextView

    @BindView(R.id.nextButton)
    lateinit var finishButton: TextView

    companion object {
        @JvmStatic
        fun newInstance(): FinishTripFragment {
            return FinishTripFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_trip_finish, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        val tripData = ScoringService.getTripData()
        val fragment = TripStatisticFragment.newInstance(tripData.scoringData)
        childFragmentManager
                .beginTransaction()
                .replace(R.id.statistic_container, fragment, "statisticFragment")
                .commit()

        updateInfo(tripData)
    }

    private fun updateInfo(tripData: TripData) {

        val tripStatus = when(tripData.scoringData.drivingLevel) {
            in 50..64 -> TripStatus.LEVEL50
            in 65..79 -> TripStatus.LEVEL65
            in 80..100 -> TripStatus.LEVEL80
            else -> TripStatus.FAILED
        }

        val tripLevelLow = tripData.scoringData.drivingLevel <= 80;

        if (tripLevelLow) {
            textTitle.setTextColor(resources.getColor(R.color.error))

            nextTextInfo.setText(R.string.trip_finish_next_info_80)

            finishButton.setText(R.string.trip_finish_next_80);
            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { finishClick() }
            }
        } else {
            textTitle.setTextColor(resources.getColor(R.color.green))

            nextTextInfo.setText(R.string.trip_finish_next_info_100)

            finishButton.setText(R.string.trip_finish_next_100);
            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { nextClick() }
            }
        }

        textTitle.setText(
                when(tripStatus){
                    TripStatus.LEVEL50 -> R.string.trip_finish_title_50
                    TripStatus.LEVEL65 -> R.string.trip_finish_title_65
                    TripStatus.LEVEL80 -> R.string.trip_finish_title_80
                    else -> {
                        if (tripData.tripTime < ScoringService.MIN_TRIP_TIME) {
                            R.string.error_trip_time
                        } else
                        if (tripData.gpsLevel < 90) {
                            R.string.error_low_gps
                        } else {
                            R.string.trip_finish_title_0
                        }
                    }
                })
    }

    private fun nextClick() {
        EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.REWARDS_FRAGMENT))
    }

    private fun finishClick() {
        EventBus.getDefault().post(FirstScreenEvent())
    }

}
