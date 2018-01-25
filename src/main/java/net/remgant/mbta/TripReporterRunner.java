package net.remgant.mbta;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.sql.DataSource;

/**
 * Created by jdr on 1/24/18.
 */
public class TripReporterRunner {
    public static void main(String args[]) {
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new SingleConnectionDataSource(dbUrl, dbUser, dbPwd, false);
        StopTimesDAOImpl stopTimesDAO = new StopTimesDAOImpl(dataSource);
        dataSource = new SingleConnectionDataSource(dbUrl, dbUser, dbPwd, false);
        OnTimeDataDAO onTimeDataDAO = new OnTimeDAOImpl(dataSource);
        TripReporter tripReporter = new TripReporter();
        tripReporter.setStopTimesDAO(stopTimesDAO);
        tripReporter.setOnTimeDataDAO(onTimeDataDAO);
        tripReporter.setRouteId("CR-Fitchburg");
        String apiKey = System.getProperty("api.key");
        if (apiKey != null)
            tripReporter.setApiKey(apiKey);
        tripReporter.init();

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.initialize();
        CronTrigger trigger = new CronTrigger("0 */1 4-23 * * MON-FRI");
        taskScheduler.schedule(tripReporter, trigger);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {

        }
    }
}
