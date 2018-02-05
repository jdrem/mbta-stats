package net.remgant.mbta;

import java.time.LocalTime;

/**
 * Created by jdr on 2/4/18.
 */
public class Stop {
    String name;
    LocalTime arrivalTime;
    boolean nextDay;
    int sequence;

    public Stop(String name, LocalTime arrivalTime, boolean nextDay, int sequence) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.nextDay = nextDay;
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public boolean isNextDay() {
        return nextDay;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "name='" + name + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", nextDay=" + nextDay +
                ", sequence=" + sequence +
                '}';
    }
}
