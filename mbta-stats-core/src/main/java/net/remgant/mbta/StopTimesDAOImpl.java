package net.remgant.mbta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/14/18.
 */
public class StopTimesDAOImpl implements StopTimesDAO {
    Logger log = LoggerFactory.getLogger(StopTimesDAOImpl.class);
    private JdbcTemplate jdbcTemplate;

    public StopTimesDAOImpl() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public StopTimesDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public StopTimesDAOImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addStopToRoute(int tripId, LocalTime arrivalTime, boolean nextDay, String stopName, int stopSequence) {
        jdbcTemplate.update("insert into Stops values(?,?,?,?,?)",
                tripId, Time.valueOf(arrivalTime), nextDay, stopName, stopSequence);
    }

    @Override
    public void addTrip(String routeName, String scheduleType, String calendarName, int tripId, String headSign, String dir) {
        int routeId = jdbcTemplate.queryForObject("select routeId from Routes where routeName = ?", new Object[]{routeName}, Integer.class);
        jdbcTemplate.update("insert into Trips values(?,?,?,?,?)", tripId, routeId, scheduleType, headSign, dir);
    }

    @Override
    public List<String> findStopsForTrip(int tripId) {
        List<String> list = new ArrayList<>();
        jdbcTemplate.query("select stopId from Stops where tripId = ? order by stopSequence",
                (RowCallbackHandler) resultSet -> list.add(resultSet.getString(1)),
                tripId);
        return list;
    }

    @Override
    public List<Stop> getSchedule(int tripId) {
        List<Stop> list = new ArrayList<>();
        jdbcTemplate.query("select stopName, arrivalTime, nextDay, stopSequence from Stops where tripId = ? order by stopSequence;",
                (RowCallbackHandler) resultSet -> list.add(new Stop(resultSet.getString(1),
                        resultSet.getTime(2).toLocalTime(),
                        resultSet.getBoolean(3),
                        resultSet.getInt(4))),
                tripId);
        return list;
    }

    @Override
    public List<String> findAllRoutes() {
        List<String> list = new ArrayList<>();
        return jdbcTemplate.queryForList("select routeName from Routes", String.class);
    }

    @Override
    public List<String> findRoutesForDay(LocalDate date) {
        return jdbcTemplate.queryForList("select routeName from Routes where exists (select TripResults.tripId " +
                "from Trips, TripResults WHERE  Routes.routeId = Trips.routeId and Trips.tripId = TripResults.tripId " +
                "and tripDate = ?)", String.class, date);
    }


    @Override
    public List<Trip> findTripsForRoute(String routeName, LocalDate date) {
        Map<String, Object> map;
        try {
            map = jdbcTemplate.queryForMap("select calendarName,exceptionType as calendarType from CalendarExceptions " +
                    "where exceptionDate = ?", Date.valueOf(date));
        } catch (EmptyResultDataAccessException empty) {
            map = new HashMap<>();
            switch (date.getDayOfWeek()) {
                case SUNDAY:
                    map.put("calendarType", "Sunday");
                    break;
                case SATURDAY:
                    map.put("calendarType", "Saturday");
                    break;
                default:
                    map.put("calendarType", "Weekday");
            }
            map.put("calendarName", jdbcTemplate.queryForObject("select calendarName from Calendar where calendarType = ? and" +
                    "? >= startDate and ? <= endDate", new Object[]{map.get("calendarType"), Date.valueOf(date), Date.valueOf(date)}, String.class));
        }
        final String calendarName = map.get("calendarName").toString();
        final String calendarType = map.get("calendarType").toString();
        List<Trip> list = new ArrayList<>();
        log.debug("Getting schedule for {} and {}", routeName, calendarType);
        jdbcTemplate.query("select  tripId from Trips t, Routes r where t.routeId = r.routeId and r.routeName = ? " +
                        "and scheduleType = ?",
                (RowCallbackHandler) resultSet -> list.add(new Trip(routeName, resultSet.getInt(1), calendarType, calendarName)),
                routeName, calendarType);
        return list;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
