package ru.telematica.casco2go.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import org.greenrobot.eventbus.EventBus
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.TripData
import ru.telematica.casco2go.model.eventbus.FirstScreenEvent
import ru.telematica.casco2go.model.eventbus.OpenFragmentEvent
import ru.telematica.casco2go.service.ScoringService
import ru.telematica.casco2go.utils.Animations
import ru.telematica.casco2go.utils.DateUtils
import ru.telematica.casco2go.utils.JsonUtils
import java.text.SimpleDateFormat

/**
 * Created by m.sidorov on 01.05.2018.
 */
class TripStatisticFragment : BaseFragment() {

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
    lateinit var textTotalPrice: TextView

    @BindView(R.id.levelIndicator)
    lateinit var levelIndicator: View

    @BindView(R.id.scopeContainer)
    lateinit var scopeContainer: ViewGroup

    companion object {

        private val TRIP_DATA_PARAM = "trip_data.param"

        @JvmStatic
        fun newInstance(tripData: TripData): TripStatisticFragment {
            val json = JsonUtils.toJson(tripData)
            val bundle = Bundle()
            bundle.putString(TRIP_DATA_PARAM, json)
            val fragment = TripStatisticFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_trip_statistic, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        val tripDataJson = arguments.getString(TRIP_DATA_PARAM, "")
        if (!tripDataJson.isBlank()){
            val tripData = JsonUtils.parseJson(tripDataJson, TripData::class.java)
            updateInfo(tripData)
        }
    }

    private fun updateInfo(tripData: TripData) {
        val tripFailed = tripData.scoringData.drivingLevel < 65;

        if (tripFailed) {
            textLevel.setTextColor(resources.getColor(R.color.error))
            textTotalDiscount.setTextColor(resources.getColor(R.color.error))
        } else {
            textLevel.setTextColor(resources.getColor(R.color.green))
            textTotalDiscount.setTextColor(resources.getColor(R.color.green))
        }

        textLevel.setText("${tripData.scoringData.drivingLevel}");
        textStartTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(tripData.startTime))
        textEndTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(tripData.finishTime))
        val min = getString(R.string.minutes)
        textTripTime.setText(formatMinutes(tripData.scoringData.timeTripSec))
        textTravelTime.setText(formatMinutes(tripData.scoringData.timeInTravelSec));
        textTrafficTime.setText(formatMinutes(tripData.scoringData.timeInTrafficSec));
        textTotalDiscount.setText("${tripData.scoringData.getDiscount()}%");
        textTotalPrice.setText(getString(R.string.total_discount_price, tripData.scoringData.tripCost));

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

}