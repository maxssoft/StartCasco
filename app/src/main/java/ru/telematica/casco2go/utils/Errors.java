package ru.telematica.casco2go.utils;

/**
 * Created by m.sidorov on 23.02.2018.
 */

public class Errors {

    public static String getErrorMessage(Throwable e){
        if (e == null){
            return "Null exception";
        }
        if (e.getMessage() == null){
            return e.getClass().getSimpleName();
        }
        return e.getMessage();
    }


}
