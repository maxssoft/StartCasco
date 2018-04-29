package ru.telematica.casco2go.model.eventbus;

import ru.telematica.casco2go.ui.fragments.FragmentTypes;

/**
 * Команда для открытия фрагмента, содержит параметры, необходимые для перехода на фрагмент
 * Created by m.sidorov on 29.04.2018.
 */
public class OpenFragmentEvent {

    public FragmentTypes fragmentType;

    public boolean addToBackStack = true;
    public boolean clearBackStack = false;

    public OpenFragmentEvent(FragmentTypes fragmentType) {
        this.fragmentType = fragmentType;
    }

    public OpenFragmentEvent(FragmentTypes fragmentType, boolean addToBackStack) {
        this.fragmentType = fragmentType;
        this.addToBackStack = false;
    }

}
