package ru.lanit.at.util;

import java.util.Date;

/**
 * Created by Michael Strizhov on 10.04.2018.
 */
public class Timeout {
    private long endTime;

    public Timeout(int timeoutInSec) {
        endTime = new Date().getTime() + timeoutInSec * 1000;
    }

    public boolean notOver() {
        return (new Date().getTime()) < endTime;
    }

    public boolean over() {
        return !notOver();
    }

}
