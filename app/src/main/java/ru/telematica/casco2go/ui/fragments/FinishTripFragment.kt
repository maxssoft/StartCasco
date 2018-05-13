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
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.model.eventbus.*
import ru.telematica.casco2go.service.ScoringService
import ru.telematica.casco2go.utils.Animations
import ru.telematica.casco2go.model.TripData


/**
 * Created by m.sidorov on 01.05.2018.
 */
class FinishTripFragment: BaseFragment() {

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

        val tripStatus = tripData.scoringData.tripStatus
        val tripLevelLow = tripData.scoringData.drivingLevel <= 80;

        textTitle.setTextColor(resources.getColor(tripData.scoringData.getColorResId()))

        if (tripLevelLow) {

            nextTextInfo.setText(R.string.trip_finish_next_info_80)

            finishButton.setText(R.string.trip_finish_next_80);
            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { finishClick() }
            }
        } else {

            nextTextInfo.setText(R.string.trip_finish_next_info_100)

            finishButton.setText(R.string.trip_finish_next_100);
            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { nextClick() }
            }
        }

        textTitle.setText(
                when(tripStatus){
                    ScoringData.TripStatus.LEVEL50 -> R.string.trip_finish_title_50
                    ScoringData.TripStatus.LEVEL65 -> R.string.trip_finish_title_65
                    ScoringData.TripStatus.LEVEL80 -> R.string.trip_finish_title_80
                    else -> {
                        if (tripData.scoringData.timeTripSec < ScoringService.MIN_TRIP_TIME_SEC) {
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
