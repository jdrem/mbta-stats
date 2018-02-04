package net.remgant.mbta;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/16/18.
 */
public class TripReporter implements Runnable {
    Logger log = LoggerFactory.getLogger(TripReporter.class);
    private StopTimesDAO stopTimesDAO;
    private OnTimeDataDAO onTimeDataDAO;
    private String routeId;
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final static Type type = new TypeToken<Map<String, Object>>() {
    }.getType();
    private final static ZoneId zoneId = ZoneId.of("America/New_York");

    private List<String> trips;

    @PostConstruct
    public void init() {
        trips = stopTimesDAO.findTripsForRoute(routeId);
    }

    @Override
    public void run() {
        for (String tripName : trips) {
            String url = "https://api-v3.mbta.com/predictions?filter[trip]={TripID}";
            if (apiKey != null)
                url += "&api_key=" + apiKey;
            String r;
            try {
                r = restTemplate.getForObject(url, String.class, tripName);
            } catch (HttpClientErrorException hcee) {
                log.warn("getting for trip {}: {}",tripName,hcee.getMessage());
                continue;
            }
            int tripId = Integer.parseInt(tripName.substring(tripName.lastIndexOf("-")+1));
            Map<String, Object> map = gson.fromJson(r, type);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            if (data == null || data.size() == 0)
                continue;
            List<String[]> schedule = stopTimesDAO.getSchedule(tripName);
            Map<String, Object> prediction = data.get(0);
            Map<String, Object> attributes = (Map<String, Object>) prediction.get("attributes");
            int stopSequence = ((Number) attributes.get("stop_sequence")).intValue();
            String predictedTimeStr;
            if (stopSequence == 1)
                predictedTimeStr = (String) attributes.get("departure_time");
            else
                predictedTimeStr = (String) attributes.get("arrival_time");
            LocalDateTime localDateTime = LocalDateTime.parse(predictedTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime predictedTime = ZonedDateTime.of(localDateTime, zoneId);
            String sched[] = schedule.get(stopSequence - 1);
            String t[] = sched[0].split(":");
            LocalTime localTime = LocalTime.of(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
            ZonedDateTime scheduledTime = ZonedDateTime.of(LocalDate.now(), localTime, zoneId);
            Duration delay = Duration.between(scheduledTime, predictedTime);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            Duration timeTilStop = Duration.between(now, predictedTime);
            log.info("{} {} {} {} {} {} {} {} {}",
                    tripName,
                    tripId,
                    sched[1],
                    stopSequence,
                    DateTimeFormatter.ISO_LOCAL_TIME.format(now),
                    DateTimeFormatter.ISO_LOCAL_TIME.format(scheduledTime),
                    DateTimeFormatter.ISO_LOCAL_TIME.format(predictedTime),
                    String.format("%d:%02d", delay.get(ChronoUnit.SECONDS) / 60, Math.abs(delay.get(ChronoUnit.SECONDS) % 60)),
                    String.format("%d:%02d", timeTilStop.get(ChronoUnit.SECONDS) / 60, Math.abs(timeTilStop.get(ChronoUnit.SECONDS) % 60))
            );
            onTimeDataDAO.addRecord(LocalDate.now(), Instant.now(), tripId, sched[1], stopSequence,
                    scheduledTime.toLocalTime(), predictedTime.toLocalTime(),
                    delay.get(ChronoUnit.SECONDS), timeTilStop.get(ChronoUnit.SECONDS));
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStopTimesDAO(StopTimesDAO stopTimesDAO) {
        this.stopTimesDAO = stopTimesDAO;
    }

    public void setOnTimeDataDAO(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
