package ru.telematica.casco2go.ui.presenters;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ru.telematica.casco2go.R;

import ru.telematica.casco2go.model.eventbus.OpenFragmentEvent;
import ru.telematica.casco2go.model.eventbus.*;
import ru.telematica.casco2go.model.TripData;

import ru.telematica.casco2go.ui.base.MainActivityView;
import ru.telematica.casco2go.ui.fragments.FragmentTypes;
import ru.telematica.casco2go.ui.fragments.StartTripFragment;
import ru.telematica.casco2go.service.ScoringService;


/**
 * Created by m.sidorov on 29.04.2018.
 */

public class MainPresenter {

    private MainActivityView activity;

    private FragmentTypes currentFragmentType = FragmentTypes.EMPTY_FRAGMENT;

    public void setCurrentFragmentType(FragmentTypes fragmentType) {
        currentFragmentType = fragmentType;
    }

    public FragmentTypes getCurrentFragmentType() {
        return currentFragmentType;
    }


    public MainPresenter(MainActivityView activity){
        this.activity = activity;
    }

    public void onStartActivity(){
        EventBus.getDefault().register(this);
        restoreFragment();
    }

    public void onStopActivity(){
        EventBus.getDefault().unregister(this);
    }

    private void restoreFragment(){
        TripData tripData = ScoringService.getTripData();
        if (tripData.isFinished()){
            openFragment(new OpenFragmentEvent(FragmentTypes.FINISH_TRIP_FRAGMENT, false));
        } else
        if (tripData.isStarted()){
            openFragment(new OpenFragmentEvent(FragmentTypes.PROCESS_TRIP_FRAGMENT, false));
        } else {
            openFragment(new OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT, false));
        }
    }

    @Subscribe
    public void onEvent(OpenFragmentEvent openFragmentEvent) {
        openFragment(openFragmentEvent);
    }

    private void openFragment(OpenFragmentEvent openFragmentEvent) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (openFragmentEvent.fragmentType) {
            case START_TRIP_FRAGMENT:
                fragment = StartTripFragment.newInstance();
                // fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                // fragTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                // fragTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                break;
/*
            case Constants.CALL_OR_COPY_FRAGMENT:
                if (CallOrCopyDialogFragment.isShowing)
                    return;
                fragment = CallOrCopyDialogFragment.newInstance(openFragmentEvent.getValue());
                ((DialogFragment) fragment).show(fragmentManager, null);
                return;
*/
            default:
                throw new IllegalArgumentException(String.format("Unknown fragment type [%s]", openFragmentEvent.fragmentType.name()));
        }

        fragment.setRetainInstance(true);
        // при показе нового фрагмента всегда скрываем клавиатуру
        activity.hideKeyboard();

        if (openFragmentEvent.addToBackStack) {
            fragTransaction.addToBackStack(null);
        }
        if (openFragmentEvent.clearBackStack) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragTransaction.replace(R.id.fragment_container, fragment, "tag");

/*
        // Включаем анимацию сцены с разделяемым элементом
        if (!openFragmentEvent.getSharedTransitionElements().isEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                fragTransaction.setCustomAnimations(0, 0, 0, 0);
                // fragment.setSharedElementEnterTransition(new AutoTransition());
                fragment.setSharedElementEnterTransition(new AnimUtil.OpenFragmentWithSharedViewTransition());
                fragment.setSharedElementReturnTransition(new AnimUtil.OpenFragmentWithSharedViewTransition());

                for (Transitions.SharedElement element : openFragmentEvent.getSharedTransitionElements().values()){
                    // ViewCompat.setTransitionName(view, getString(R.string.shared_transition));
                    // fragTransaction.addSharedElement(view, getString(R.string.shared_transition));
                    fragTransaction.addSharedElement(element.view, element.view.getTransitionName());
                }
            }
        }
*/

        fragTransaction.commit();
        setCurrentFragmentType(openFragmentEvent.fragmentType);
    }

    @Subscribe
    public void onEvent(ErrorEvent event) {
        activity.showError(event.message);
    }

    @Subscribe
    public void onEvent(StartTripEvent event) {
        Intent intent = new Intent(activity, ScoringService.class);
        intent.setAction(ScoringService.ACTION_START_TRIP);
        intent.putExtra(ScoringService.EXTRA_CAR_PRICE, event.carPrice);
        activity.startService(intent);
    }

    @Subscribe
    public void onEvent(FinishTripEvent event) {
        Intent intent = new Intent(activity, ScoringService.class);
        intent.setAction(ScoringService.ACTION_STOP_TRIP);
        activity.startService(intent);
    }

}
