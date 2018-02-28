package net.remgant.mbta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/14/18.
 */
public interface StopTimesDAO {

    List<String> findStopsForTrip(int routeId);

    List<String> findAllRoutes();

    List<String> findRoutesForDay(LocalDate date);

    List<Stop> getSchedule(int tripId);

    List<Trip> findTripsForRoute(String routeName, LocalDate date);

    void addTrip(String routeName, String scheduleType, String calendarName, int tripId, String headSign, String dir);

    void addStopToRoute(int tripId, LocalTime arrivalTime, boolean nextDay, String stopName, int stopSequence);
}
