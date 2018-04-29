package ru.telematica.casco2go.model.eventbus;

import io.reactivex.annotations.Nullable;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class ErrorEvent {

    public final String message;
    public final @Nullable Throwable error;

    public ErrorEvent(String message, Throwable error){
        this.message = message;
        this.error = error;
    }

    public ErrorEvent(String message){
        this.message = message;
        this.error = null;
    }

}
