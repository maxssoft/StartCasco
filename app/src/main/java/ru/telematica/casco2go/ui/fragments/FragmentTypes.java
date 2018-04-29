package ru.telematica.casco2go.ui.fragments;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public enum FragmentTypes {
    EMPTY_FRAGMENT(0),
    START_TRIP_FRAGMENT(1),
    PROCESS_TRIP_FRAGMENT(2),
    FINISH_TRIP_FRAGMENT(3),
    RESULT_FRAGMENT(4);

    private int intValue;
    FragmentTypes(int value) {
        intValue = value;
    }
}
