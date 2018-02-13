package net.remgant.mbta;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jdr on 1/28/18.
 */
@Ignore
public class UpdateInPlaceTest {

    DataSource dataSource;

    @Before
    public void setup() {
        dataSource = new SingleConnectionDataSource("jdbc:mysql://carpenter.home.remgant.net:3306/MBTA?useSSL=false", "root", "kuoluva24", false);
    }

    @Test
    public void test1() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query("select * from TripResultTest", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                String s = resultSet.getString(1);
                int id = 1;
                resultSet.updateInt(1, id);
                resultSet.updateRow();
            }
        });
    }

    @Test
    public void test2() {
        String sql = "select * from TripResults";
        Pattern p = Pattern.compile("CR-.*-(\\d+)");
        PreparedStatementCreator psc = connection ->
                connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.query(psc, resultSet -> {
            String s = resultSet.getString("tripName");
            Matcher m = p.matcher(s);
            if (!m.matches())
                throw new RuntimeException("parse error: "+s);
            int id = Integer.parseInt(m.group(1));
            resultSet.updateInt("tripId", id);
            resultSet.updateRow();
        });

    }
}
