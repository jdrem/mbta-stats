package net.remgant.mbta;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/24/18.
 */
public interface OnTimeDataDAO {
    void addRecord(LocalDate date, Instant timeStamp, int tripId, String nextStop, int stopSequence, LocalTime scheduledTime,
                   LocalTime predictedTime, long delay, long timeTilStop);

    Map<String, Object> findDataForTrip(LocalDate date, int tripId);
}
