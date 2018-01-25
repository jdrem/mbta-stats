package net.remgant.mbta;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;


import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jdr on 1/14/18.
 */
public class StopTimesDAOImpl implements StopTimesDAO {
    private JdbcTemplate jdbcTemplate;

    public StopTimesDAOImpl() {
    }

    public StopTimesDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public StopTimesDAOImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void init() {

    }

    @Override
    public void addStopToRoute(String tripId, LocalTime arrivalTime, boolean nextDay, String stopId, int stopSequence) {
        jdbcTemplate.update("insert into Stops values (?,?,?,?,?)",
                tripId, arrivalTime.toString(), nextDay, stopId, stopSequence);
    }

    @Override
    public void addTrip(String routeId, String tripId, String headSign, String dir) {
        jdbcTemplate.update("insert into Trips values(?,?,?,?)",
                routeId, tripId, headSign, dir);
    }

    @Override
    public List<String> findRoute(int routeId) {
        List<String> list = new ArrayList<>();
        jdbcTemplate.query("select stopId from Stops where routeId = ?",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet resultSet) throws SQLException {
                        list.add(resultSet.getString(1));
                    }
                },
                routeId);
        return list;
    }

    @Override
    public List<String[]> getSchedule(String tripId) {
        List<String[]> list = new ArrayList<>();
        jdbcTemplate.query("select arrivalTime,stopId from Stops where tripId = ? order by stopSequence;",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet resultSet) throws SQLException {
                        list.add(new String[]{resultSet.getString(1),resultSet.getString(2)});
                    }
                },
                tripId);
        return list;
    }

    @Override
    public List<String> findAllRoutes() {
        List<String> list = new ArrayList<>();

        jdbcTemplate.query("select distinct routeId from Trips", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                list.add(resultSet.getString(1));
            }
        });
        return list;
    }

    @Override
    public List<String> findTripsForRoute(String routeId) {
        List<String> list = new ArrayList<>();
        jdbcTemplate.query("select  tripId from Trips where routeId = ?", new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet resultSet) throws SQLException {
                        list.add(resultSet.getString(1));
                    }
                },
                routeId);
        return list;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
