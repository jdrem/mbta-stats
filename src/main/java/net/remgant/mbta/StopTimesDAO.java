package net.remgant.mbta;

import java.time.LocalDate;
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

    public List<Object[]> getSchedule(int tripId);

    List<Trip> findTripsForRoute(String routeName, LocalDate date);

    void addTripX(String routeName, String scheduleType, String calendarName, int tripId, String headSign, String dir);

    void addStopToRouteX(int tripId, LocalTime arrivalTime, boolean nextDay, String stopName, int stopSequence);
}
