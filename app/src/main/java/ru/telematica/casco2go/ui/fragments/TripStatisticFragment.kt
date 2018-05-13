package ru.telematica.casco2go.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.model.TripData
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

        val SCORING_DATA_PARAM = "scoring.data.param"

        @JvmStatic
        fun newInstance(scoringData: ScoringData): TripStatisticFragment {
            val json = JsonUtils.toJson(scoringData)
            val bundle = Bundle()
            bundle.putString(SCORING_DATA_PARAM, json)
            return newInstance(bundle)
        }

        @JvmStatic
        fun newInstance(bundle: Bundle): TripStatisticFragment {
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

        val jsonData = arguments.getString(SCORING_DATA_PARAM, "")
        if (!jsonData.isBlank()){
            val data = JsonUtils.parseJson(jsonData, ScoringData::class.java)
            updateInfo(data)
        }
    }

    private fun updateInfo(data: ScoringData) {
        val tripFailed = data.drivingLevel < 65

        textLevel.setTextColor(data.getColorResId())
        textTotalDiscount.setTextColor(data.getColorResId())

        textLevel.setText("${data.drivingLevel}");
        if (data.startTime != null) {
            textStartTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(data.startTime))
        }
        if (data.finishTime != null) {
            textEndTime.setText(SimpleDateFormat(DateUtils.DATETIME_VIEW_FORMAT).format(data.finishTime))
        }
        val min = getString(R.string.minutes)
        textTripTime.setText(formatMinutes(data.timeTripSec))
        textTravelTime.setText(formatMinutes(data.timeInTravelSec));
        textTrafficTime.setText(formatMinutes(data.timeInTrafficSec));
        textTotalDiscount.setText("${data.getDiscount()}%");
        textTotalPrice.setText(getString(R.string.trip_finish_total_price, data.tripCost));

        updateScopeContainerIndicator(data.drivingLevel)
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
        return (timeSec / 60).toString() + " " + getString(R.string.minutes)
    }

}