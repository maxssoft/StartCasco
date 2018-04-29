package ru.telematica.casco2go.model;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class TripData {

    private int carPrice;
    private Calendar startTime;
    private Calendar finishTime;
    private boolean started = false;
    private boolean finished = false;

    public Calendar getStartTime(){
        return startTime;
    }

    public Calendar getFinishTime(){
        return finishTime;
    }

    public int getCarPrice(){
        return carPrice;
    }

    public boolean isStarted(){
        return started;
    }

    public boolean isFinished(){
        return finished;
    }

    // Время поездки в мс
    public long getTripTime(){
        if (!isStarted()) {
            return 0;
        } else
        if (!isFinished()){
            return System.currentTimeMillis() - startTime.getTimeInMillis();
        } else {
            return finishTime.getTimeInMillis() - startTime.getTimeInMillis();
        }
    }

    public int getGpsLevel(){
        return 98;
    }


    public TripData(int carPrice){
        this.carPrice = carPrice;
        this.started = false;
        this.finished = false;
    }

    public TripData start(){
        this.started = true;
        this.startTime = Calendar.getInstance();
        return this;
    }

    public TripData finish(){
        this.finished = true;
        this.finishTime = Calendar.getInstance();
        return this;
    }

}
