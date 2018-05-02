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
import ru.telematica.casco2go.utils.DateUtils
import java.text.SimpleDateFormat
import android.view.ViewTreeObserver



/**
 * Created by m.sidorov on 01.05.2018.
 */
class FinishTripFragment: BaseFragment() {

    enum class TripStatus {FAILED, LEVEL50, LEVEL65, LEVEL80}

    @BindView(R.id.textTitle)
    lateinit var textTitle: TextView

    @BindView(R.id.textLevel)
    lateinit var textLevel: TextView

    @BindView(R.id.textStartTime)
    lateinit var textStartTime: TextView

    @BindView(R.id.textEndTime)
    lateinit var textEndTime: TextView

    @BindView(R.id.textTripTime)
    lateinit var textTripTime: TextView

    @BindView(R.id.textTravelTime)
    lateinit var textTravelTime: TextView

    @BindView(R.id.textTrafficTime)
    lateinit var textTrafficTime: TextView

    @BindView(R.id.textTotalDiscount)
    lateinit var textTotalDiscount: TextView

    @BindView(R.id.textTotalPrice)
    lateinit var textTotalDiscountPrice: TextView

    @BindView(R.id.nextTextInfo)
    lateinit var nextTextInfo: TextView

    @BindView(R.id.nextButton)
    lateinit var finishButton: TextView

    @BindView(R.id.levelIndicator)
    lateinit var levelIndicator: View

    @BindView(R.id.scopeContainer)
    lateinit var scopeContainer: ViewGroup

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

        updateInfo()
    }

    private fun updateInfo() {
        val tripData = ScoringService.getTripData()

        val tripStatus = when(tripData.scoringData.drivingLevel) {
            in 50..64 -> TripStatus.LEVEL50
            in 65..79 -> TripStatus.LEVEL65
            in 80..100 -> TripStatus.LEVEL80
            else -> TripStatus.FAILED
        }

        val tripFailed = tripData.scoringData.drivingLevel < 65;

        if (tripFailed) {
            textTitle.setTextColor(resources.getColor(R.color.error))
            textLevel.setTextColor(resources.getColor(R.color.error))
            textTotalDiscount.setTextColor(resources.getColor(R.color.error))

            nextTextInfo.setText(R.string.trip_finish_fail_next)
            finishButton.setText(R.string.ok);
            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { finishClick() }
            }
        } else {
            textTitle.setTextColor(resources.getColor(R.color.green))
            textLevel.setTextColor(resources.getColor(R.color.green))
            textTotalDiscount.setTextColor(resources.getColor(R.color.green))

            nextTextInfo.setText(R.string.trip_finish_success_next)
            finishButton.setText(R.string.next);

            finishButton.setOnClickListener {
                Animations.startPressedScaleAnim(finishButton) { nextClick() }
            }
        }

        textTitle.setText(
                when(tripStatus){
                    TripStatus.LEVEL50 -> R.string.trip_finish_success_50
                    TripStatus.LEVEL65 -> R.string.trip_finish_success_65
                    TripStatus.LEVEL80 -> R.string.trip_finish_success_80
                    else -> R.string.trip_finish_fail
                })
        textLevel.setText("${tripData.scoringData.drivingLevel}");
        textStartTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(tripData.startTime))
        textEndTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(tripData.finishTime))
        val min = getString(R.string.minutes)
        textTripTime.setText(formatMinutes(tripData.scoringData.timeTripSec))
        textTravelTime.setText(formatMinutes(tripData.scoringData.timeInTravelSec));
        textTrafficTime.setText(formatMinutes(tripData.scoringData.timeInTrafficSec));
        textTotalDiscount.setText("${tripData.scoringData.getDiscount()}%");
        textTotalDiscountPrice.setText(getString(R.string.total_discount_price, tripData.scoringData.tripCost));

        updateScopeContainerIndicator(tripData.scoringData.drivingLevel)
    }

    private fun updateScopeContainerIndicator(drivingLevel: Int){
        scopeContainer.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scopeContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this)

                val allWidth = scopeContainer.measuredWidth
                levelIndicator.setPadding((allWidth * drivingLevel) / 100, 0, 0,0)
            }
        })
    }

    private fun formatMinutes(timeSec: Int): String{
        if (timeSec < 60) {
            return ""
        }
        return (timeSec / 60).toString() + " " + getString(R.string.minutes)
    }

    private fun nextClick() {
        EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.REWARDS_FRAGMENT))
    }

    private fun finishClick() {
        EventBus.getDefault().post(FirstScreenEvent())
    }

}
