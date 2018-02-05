package net.remgant.mbta;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
        DataSource dataSource = new DriverManagerDataSource(dbUrl, dbUser, dbPwd);
        StopTimesDAO stopTimesDAO = new StopTimesDAOImpl(dataSource);
        OnTimeDataDAO onTimeDataDAO = new OnTimeDAOImpl(dataSource);
        TripReporter tripReporter = new TripReporter();
        tripReporter.setStopTimesDAO(stopTimesDAO);
        tripReporter.setOnTimeDataDAO(onTimeDataDAO);
        tripReporter.setRouteNames(System.getProperty("route.names"));
        String apiKey = System.getProperty("api.key");
        if (apiKey != null)
            tripReporter.setApiKey(apiKey);
        tripReporter.init();
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.initialize();
        CronTrigger trigger = new CronTrigger("0 */1 4-23 * * *");
        taskScheduler.schedule(tripReporter::processTripData, trigger);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignore) {
        }
    }
}
