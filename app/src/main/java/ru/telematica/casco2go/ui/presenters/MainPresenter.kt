package ru.telematica.casco2go.ui.presenters

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.model.eventbus.*
import ru.telematica.casco2go.service.ScoringService
import ru.telematica.casco2go.ui.base.MainActivityView
import ru.telematica.casco2go.ui.fragments.*
import java.security.InvalidParameterException

/**
 * Created by m.sidorov on 29.04.2018.
 */
class MainPresenter {

    constructor(activity: MainActivityView){
        this.activity = activity
    }

    private var activity: MainActivityView? = null

    fun releaseActivity(){
        onStopActivity()
        activity = null
    }

    var currentFragmentType = FragmentTypes.EMPTY_FRAGMENT
        private set

    fun onStartActivity() {
        EventBus.getDefault().register(this)
        restoreFragment()
    }

    fun onStopActivity() {
        EventBus.getDefault().unregister(this)
    }

    private fun restoreFragment() {
        val tripData = ScoringService.getTripData()
        if (tripData.finished) {
            openFragment(OpenFragmentEvent(FragmentTypes.FINISH_TRIP_FRAGMENT))
        } else if (tripData.started) {
            openFragment(OpenFragmentEvent(FragmentTypes.PROCESS_TRIP_FRAGMENT))
        } else {
            openFragment(OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT))
        }
    }

    @Subscribe
    fun onEvent(openFragmentEvent: OpenFragmentEvent) {
        openFragment(openFragmentEvent)
    }

    private fun openFragment(openFragmentEvent: OpenFragmentEvent) {
        if (activity == null) {
            return
        }

        val _activity: MainActivityView = activity!!
        val fragmentManager = _activity.supportFragmentManager
        val fragTransaction = fragmentManager.beginTransaction()
        var fragment: Fragment
        when (openFragmentEvent.fragmentType) {
            FragmentTypes.START_TRIP_FRAGMENT -> {
                fragment = StartTripFragment.newInstance()
                fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            FragmentTypes.PROCESS_TRIP_FRAGMENT -> {
                fragment = ProcessTripFragment.newInstance()
                fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            FragmentTypes.FINISH_TRIP_FRAGMENT -> {
                fragment = FinishTripFragment.newInstance()
                fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            FragmentTypes.REWARDS_FRAGMENT -> {
                fragment = RewardsFragment.newInstance()
                fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            FragmentTypes.HISTORY_FRAGMENT -> {
                fragment = HistoryFragment.newInstance()
                fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            FragmentTypes.SCORING_FRAGMENT -> {
                if (openFragmentEvent.data is ScoringData) {
                    val dataScoring = openFragmentEvent.data as ScoringData
                    fragment = ScoringFragment.newInstance(dataScoring)
                    fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                } else {
                    throw InvalidParameterException("Open fragment [data] may be is " + ScoringData::class.java.simpleName)
                }
            }
            else -> throw IllegalArgumentException(String.format("Unknown fragment type [%s]", openFragmentEvent.fragmentType.name))
        }
        /*
        case Constants.CALL_OR_COPY_FRAGMENT:
            if (CallOrCopyDialogFragment.isShowing)
                return;
            fragment = CallOrCopyDialogFragment.newInstance(openFragmentEvent.getValue());
            ((DialogFragment) fragment).show(fragmentManager, null);
            return;
*/
        // fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        // fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        // fragTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        fragment.retainInstance = true
        // при показе нового фрагмента всегда скрываем клавиатуру
        _activity.hideKeyboard()

        fragTransaction.replace(R.id.fragment_container, fragment, null)

        if (openFragmentEvent.addToBackStack) {
            fragTransaction.addToBackStack(null)
        }
        if (openFragmentEvent.clearBackStack) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        fragTransaction.commit()
        currentFragmentType = openFragmentEvent.fragmentType
    }

    @Subscribe
    fun onEvent(event: ErrorEvent) {
        activity?.showError(event.message, event.error)
    }

    @Subscribe
    fun OnEvent(event: FirstScreenEvent){
        val intent = Intent(activity, ScoringService::class.java)
        intent.action = ScoringService.ACTION_CLEAR_TRIP
        activity?.startService(intent)
    }

    @Subscribe
    fun onEvent(event: StartTripEvent) {
        val intent = Intent(activity, ScoringService::class.java)
        intent.action = ScoringService.ACTION_START_TRIP
        activity?.startService(intent)
    }

    @Subscribe
    fun onEvent(event: FinishTripEvent) {
        val intent = Intent(activity, ScoringService::class.java)
        intent.action = ScoringService.ACTION_STOP_TRIP
        activity?.startService(intent)
    }

}
