package net.remgant.mbta;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.sql.*;
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

    public OnTimeDAOImpl() {
    }

    public OnTimeDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OnTimeDAOImpl(DataSource dataSource) {
          this.jdbcTemplate = new JdbcTemplate(dataSource);
      }

    @Override
    public void addRecord(LocalDate date, Instant timeStamp, String routeId, String nextStop, int stopSequence,
                          LocalTime scheduledTime, LocalTime predictedTime, long delay, long timeTilStop) {
        jdbcTemplate.update("insert into TripResults values(?,?,?,?,?,?,?,?,?)",
                Date.valueOf(date), Timestamp.from(timeStamp),routeId,nextStop,stopSequence, Time.valueOf(scheduledTime),
                Time.valueOf(predictedTime),delay,timeTilStop);
    }


    @Override
    public Map<String, Object> findDataForTrip(LocalDate date, String tripId) {
        List<Map<String,Object>> list = new ArrayList<>();
        jdbcTemplate.query("select * from TripResults where tripDate = ? and routeId = ?",
                new Object[]{Date.valueOf(date),tripId},
                rs -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("timestamp",rs.getTimestamp("tripTS").toInstant().toString());
                    map.put("nextStop",rs.getString("nextStop"));
                    map.put("stopSequence", rs.getInt("stopSequence"));
                    map.put("scheduledTime",rs.getTime("scheduledTiem").toLocalTime().toString());
                    map.put("predictedTime",rs.getTime("predictedTime").toLocalTime().toString());
                    map.put("delay",rs.getLong("delay"));
                    if ((int)map.get("stopSequence") == 1 && list.size() == 1) {
                        list.remove(0);
                    }
                    list.add(map);
                });
        Map<String,Object> map = new HashMap<>();
        map.put("tripDate",date.toString());
        map.put("tripId",tripId);
        map.put("stops",list);

        return map;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
