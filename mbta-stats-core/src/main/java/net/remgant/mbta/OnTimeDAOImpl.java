package net.remgant.mbta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jdr on 1/24/18.
 */
public class OnTimeDAOImpl implements OnTimeDataDAO {
    final private static Logger log = LoggerFactory.getLogger(OnTimeDAOImpl.class);
    JdbcTemplate jdbcTemplate;

    @SuppressWarnings("UnusedDeclaration")
    public OnTimeDAOImpl() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public OnTimeDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OnTimeDAOImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addRecord(LocalDate date, Instant timeStamp, int tripId, String nextStop, int stopSequence, LocalTime scheduledTime, LocalTime predictedTime, long delay, long timeTilStop) {
        jdbcTemplate.update("insert into TripResults (tripDate, tripTS, tripId, nextStop, stopSequence, scheduledTime, predictedTime, delay, timeTilNextStop) " +
                        "values(?,?,?,?,?,?,?,?,?)",
                Date.valueOf(date), Timestamp.from(timeStamp), tripId, nextStop, stopSequence, Time.valueOf(scheduledTime),
                Time.valueOf(predictedTime), delay, timeTilStop);
    }

    @Override
    public Map<String, Object> findDataForTrip(LocalDate date, int tripId) throws NoDataForTripException {
        List<Map<String, Object>> list = new ArrayList<>();
        jdbcTemplate.query("select * from TripResults where tripDate = ? and tripId = ?",
                new Object[]{Date.valueOf(date), tripId},
                new int[]{Types.DATE, Types.INTEGER},
                rs -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", rs.getTimestamp("tripTS").toInstant().toString());
                    map.put("nextStop", rs.getString("nextStop"));
                    map.put("stopSequence", rs.getInt("stopSequence"));
                    map.put("scheduledTime", rs.getTime("scheduledTime").toLocalTime().toString());
                    map.put("predictedTime", rs.getTime("predictedTime").toLocalTime().toString());
                    map.put("delay", rs.getLong("delay"));
                    if ((int) map.get("stopSequence") == 1 && list.size() == 1) {
                        list.remove(0);
                    }
                    list.add(map);
                });
        if (list.size() == 0)
            throw new NoDataForTripException(String.format("No data for trip %d on %s", tripId, date));
        Map<String, Object> map = new HashMap<>();
        map.put("tripDate", date.toString());
        map.put("tripId", tripId);
        map.put("stops", list);
        return map;
    }

    @Override
    public List<Map<String, Object>> findCurrentStopData() {
        List<Map<String, Object>> returnList = jdbcTemplate.query("select tr.tripId, routeName, nextStop, stopSequence, scheduledTime, " +
                "predictedTime, delay, timeTilNextStop, tripTS " +
                "from TripResults tr, Trips t, Routes r " +
                "where time_to_sec(timediff(convert_tz(now(),'GMT','America/New_York'),tripTS)) < 5400 " +
                "and not (delay = 0 and timeTilNextStop > 60) and tr.tripId = t.tripId and t.routeId = r.routeId " +
                "order by tripTS desc", (ResultSetExtractor<List<Map<String, Object>>>) rs -> {
            List<Map<String, Object>> list = new ArrayList<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String colName = rsmd.getColumnName(i);
                    switch (rsmd.getColumnType(i)) {
                        case Types.INTEGER:
                            map.put(colName, rs.getInt(i));
                            break;
                        default:
                            map.put(colName, rs.getObject(i).toString());
                            break;
                    }
                }
                list.add(map);
            }
            return list;
        });
        log.debug(returnList.toString());
        Map<Integer, List<Map<String, Object>>> l =
                returnList.stream().collect(Collectors.groupingBy( m -> (int)m.get("tripId")));
        return  l.values().stream().map(v -> v.get(0)).collect(Collectors.toList());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
