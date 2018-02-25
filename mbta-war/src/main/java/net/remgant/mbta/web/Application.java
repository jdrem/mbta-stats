package net.remgant.mbta.web;

import net.remgant.mbta.*;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdr on 2/12/18.
 */
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        initInitialContext();
        SpringApplication.run(Application.class, args);
    }

    private static void initInitialContext() {
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new DriverManagerDataSource(dbUrl, dbUser, dbPwd);
        builder.bind("java:/comp/env/jdbc/MBTADB", dataSource);
        try {
            builder.activate();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public OnTimeDataController onTimeDataController() {
        return new OnTimeDataController(onTimeDataDao());
    }

    @Bean
    public OnTimeDataDAO onTimeDataDao() {
        return new OnTimeDAOImpl(dataSource());
    }

    @Bean
    public StopTimesDAO stopTimesDAO() {
        return new StopTimesDAOImpl(dataSource());
    }


    @Value("${api.key:}")
    private String apiKey;
    @Value("${route.names:}")
    private String routeNames;

    @Bean
    public TripReporter tripReporter() {
        TripReporter tripReporter = new TripReporter();
        tripReporter.setOnTimeDataDAO(onTimeDataDao());
        tripReporter.setStopTimesDAO(stopTimesDAO());
        tripReporter.setApiKey(apiKey);
        tripReporter.setRouteNames(routeNames);
        return tripReporter;
    }

    @Value("${enable.trip.reporter:true}")
    private boolean enableTripReporter;

    @Bean
    public TripReporterWrapper tripReporterWrapper() {
        if (enableTripReporter)
            return new TripReporterWrapper(tripReporter());
        else
            return new TripReporterWrapper();
    }


    public static class TripReporterWrapper {
        TripReporter tripReporter;

        public TripReporterWrapper() {
        }

        TripReporterWrapper(TripReporter tripReporter) {
            this.tripReporter = tripReporter;
        }

        @Scheduled(cron = "0 0 3 * * *")
        public void refresh() {
            if (tripReporter != null)
                tripReporter.refresh();
        }

        @Scheduled(cron = "0 */1 4-23 * * *")
        public void processTripData() {
            if (tripReporter != null)
                tripReporter.processTripData();
        }
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        CorsFilter corsFilter = new CorsFilter();
        registration.setFilter(corsFilter);
        registration.setUrlPatterns(Collections.singleton("/*"));
        return registration;
    }

    @Bean
    public MBeanExporter exporter() {
        final MBeanExporter exporter = new AnnotationMBeanExporter();
        exporter.setAutodetect(true);
        exporter.setExcludedBeans("dataSource");
        return exporter;
    }

    @Bean
    public DataSource dataSource() {
        Context initContext;
        try {
            initContext = new InitialContext();
            return (DataSource) initContext.lookup("java:/comp/env/jdbc/MBTADB");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}