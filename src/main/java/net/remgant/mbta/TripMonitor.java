package net.remgant.mbta;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/15/18.
 */
public class TripMonitor {
    StopTimesDAO stopTimesDAO;
    RestTemplate restTemplate = new RestTemplate();
    Gson gson = new Gson();
    final static Type type = new TypeToken<Map<String, Object>>() {
    }.getType();
    final static ZoneId zoneId = ZoneId.of("America/New_York");

    String apiKey;
    String routeId;

    public void run() {
        List<String> trips = stopTimesDAO.findTripsForRoute(routeId);
        for (String tripId : trips) {
            String url = "https://api-v3.mbta.com/predictions?filter[trip]={TripID}";
            if (apiKey != null)
                url += "&api_key=" + apiKey;
            String r = restTemplate.getForObject(url, String.class, tripId);

            Map<String, Object> map = gson.fromJson(r, type);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            List<String[]> schedule = stopTimesDAO.getSchedule(tripId);
            for (Map<String, Object> d : data) {
                Map<String, Object> attributes = (Map<String, Object>) d.get("attributes");
//                System.out.printf("%s %s %s %s%n",tripId,
//                        attributes.get("stop_sequence"),
//                        attributes.get("arrival_time"),
//                        attributes.get("departure_time"));
                int stopSequence = ((Number) attributes.get("stop_sequence")).intValue();
                String predictedTimeStr;
                if (stopSequence == 1)
                    predictedTimeStr = (String) attributes.get("departure_time");
                else
                    predictedTimeStr = (String) attributes.get("arrival_time");
                LocalDateTime localDateTime = LocalDateTime.parse(predictedTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                ZonedDateTime predictedTime = ZonedDateTime.of(localDateTime, ZoneId.of("America/New_York"));
                String sched[] = schedule.get(stopSequence - 1);
                String t[] = sched[0].split(":");
                LocalTime localTime = LocalTime.of(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
                ZonedDateTime scheduledTime = ZonedDateTime.of(LocalDate.now(), localTime, ZoneId.of("America/New_York"));
                Duration duration = Duration.between(scheduledTime, predictedTime);
                System.out.printf("%s %s %d %s %s %d:%02d%n", tripId, sched[1], stopSequence, scheduledTime, predictedTime,
                        duration.get(ChronoUnit.SECONDS) / 60, Math.abs(duration.get(ChronoUnit.SECONDS) % 60));
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void runx() {
        List<String> trips = stopTimesDAO.findTripsForRoute(routeId);
        for (String tripId : trips) {
            String url = "https://api-v3.mbta.com/predictions?filter[trip]={TripID}";
            if (apiKey != null)
                url += "&api_key=" + apiKey;
            String r = restTemplate.getForObject(url, String.class, tripId);

            Map<String, Object> map = gson.fromJson(r, type);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            if (data == null || data.size() == 0)
                continue;
            List<String[]> schedule = stopTimesDAO.getSchedule(tripId);
            Map<String, Object> prediction = data.get(0);
            Map<String, Object> attributes = (Map<String, Object>) prediction.get("attributes");
            int stopSequence = ((Number) attributes.get("stop_sequence")).intValue();
            String predictedTimeStr;
            if (stopSequence == 1)
                predictedTimeStr = (String) attributes.get("departure_time");
            else
                predictedTimeStr = (String) attributes.get("arrival_time");
            LocalDateTime localDateTime = LocalDateTime.parse(predictedTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime predictedTime = ZonedDateTime.of(localDateTime, ZoneId.of("America/New_York"));
            String sched[] = schedule.get(stopSequence - 1);
            String t[] = sched[0].split(":");
            LocalTime localTime = LocalTime.of(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
            ZonedDateTime scheduledTime = ZonedDateTime.of(LocalDate.now(), localTime, ZoneId.of("America/New_York"));
            Duration delay = Duration.between(scheduledTime, predictedTime);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
            Duration timeTilStop = Duration.between(now, predictedTime);
            System.out.printf("%s %s %d %s %s %s %d:%02d %d:%02d%n",
                    tripId,
                    sched[1],
                    stopSequence,
                    DateTimeFormatter.ISO_LOCAL_TIME.format(now),
                    DateTimeFormatter.ISO_LOCAL_TIME.format(scheduledTime),
                    DateTimeFormatter.ISO_LOCAL_TIME.format(predictedTime),
                    delay.get(ChronoUnit.SECONDS) / 60, Math.abs(delay.get(ChronoUnit.SECONDS) % 60),
                    timeTilStop.get(ChronoUnit.SECONDS) / 60, Math.abs(timeTilStop.get(ChronoUnit.SECONDS) % 60)
                    );
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new SingleConnectionDataSource(dbUrl,dbUser,dbPwd,false);
        StopTimesDAOImpl stopTimesDAO = new StopTimesDAOImpl(dataSource);
        TripMonitor tripMonitor = new TripMonitor();
        tripMonitor.setStopTimesDAO(stopTimesDAO);
        tripMonitor.setRouteId("CR-Fitchburg");
        String apiKey = System.getProperty("api.key");
        if (apiKey != null)
            tripMonitor.setApiKey(apiKey);
        tripMonitor.runx();
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setStopTimesDAO(StopTimesDAO stopTimesDAO) {
        this.stopTimesDAO = stopTimesDAO;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
