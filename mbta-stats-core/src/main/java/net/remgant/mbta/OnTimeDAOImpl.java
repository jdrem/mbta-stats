package net.remgant.mbta;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/24/18.
 */
public class OnTimeDAOImpl implements OnTimeDataDAO {
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
            throw new NoDataForTripException(String.format("No data for trip %d on %s",tripId,date));
        Map<String, Object> map = new HashMap<>();
        map.put("tripDate", date.toString());
        map.put("tripId", tripId);
        map.put("stops", list);
        return map;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
