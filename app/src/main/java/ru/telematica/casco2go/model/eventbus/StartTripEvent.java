package ru.telematica.casco2go.model.eventbus;

import java.util.Date;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class StartTripEvent {
    public final int carPrice;

    public StartTripEvent(int carPrice){
        this.carPrice = carPrice;
    }
}
