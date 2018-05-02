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
import ru.telematica.casco2go.service.ScoringService
import android.content.Intent
import android.net.Uri
import ru.telematica.casco2go.model.eventbus.FirstScreenEvent
import ru.telematica.casco2go.model.eventbus.OpenFragmentEvent
import ru.telematica.casco2go.utils.Animations


/**
 * Created by m.sidorov on 01.05.2018.
 */
class RewardsFragment : BaseFragment() {

    val TELEGRAM_URL = "https://t.me/kasko2go"

    @BindView(R.id.textLevel)
    lateinit var textLevel: TextView

    @BindView(R.id.nextButton)
    lateinit var nextButton: TextView

    @BindView(R.id.anotherTripButton)
    lateinit var anotherTripButton: TextView

    companion object {
        @JvmStatic
        fun newInstance(): RewardsFragment {
            return RewardsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_reward, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)

        updateInfo()
    }

    private fun updateInfo() {
        val tripData = ScoringService.getTripData()
        textLevel.setText("${tripData.scoringData.drivingLevel}")
        nextButton.setOnClickListener {
            Animations.startPressedScaleAnim(nextButton) { nextClick() }
        }
        anotherTripButton.setOnClickListener {
            Animations.startPressedScaleAnim(anotherTripButton) { anotherTripClick() }
        }
    }

    private fun nextClick() {
        val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL))
        if (telegramIntent.resolveActivity(activity.packageManager) != null) {
            startActivity(telegramIntent)
        } else {
            showError(getString(R.string.error_not_telegram))
        }
    }

    private fun anotherTripClick(){
        EventBus.getDefault().post(FirstScreenEvent())
    }



}