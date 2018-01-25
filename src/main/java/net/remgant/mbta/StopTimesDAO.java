package net.remgant.mbta;

import java.time.LocalTime;
import java.util.List;

/**
 * Created by jdr on 1/14/18.
 */
public interface StopTimesDAO {
    void addStopToRoute(String tripId, LocalTime arrivalTime, boolean nextDay, String stopId, int stopSequence);
    List<String> findRoute(int routeId);

    void addTrip(String routeId, String tripId, String headSign, String dir);

    List<String> findAllRoutes();

    List<String> findTripsForRoute(String routeId);

    List<String[]>  getSchedule(String tripId);
}
