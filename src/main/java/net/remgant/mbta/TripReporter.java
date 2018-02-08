package net.remgant.mbta;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jdr on 1/16/18.
 */
@SuppressWarnings("unchecked")
public class TripReporter {
    private final static Logger log = LoggerFactory.getLogger(TripReporter.class);
    private final static ZoneId zoneId = ZoneId.of("America/New_York");
    protected Clock clock = Clock.system(zoneId);
    protected StopTimesDAO stopTimesDAO;
    protected OnTimeDataDAO onTimeDataDAO;
    private String routeNames;
    private String apiKey;

    protected RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final static Type type = new TypeToken<Map<String, Object>>() {
    }.getType();

    protected List<Trip> trips = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void refresh() {
        trips.clear();
        for (String route : routeNames.split(","))
            trips.addAll(stopTimesDAO.findTripsForRoute(route, LocalDate.now(clock)));
    }

    @Scheduled(cron = "0 */1 4-23 * * *")
    public void processTripData() {
        for (Trip trip : trips) {
            String url = "https://api-v3.mbta.com/predictions?filter[trip]={TripID}";
            if (apiKey != null && apiKey.length() > 0)
                url += "&api_key=" + apiKey;
            String r;
            try {
                r = restTemplate.getForObject(url, String.class, trip.toString());
            } catch (HttpClientErrorException hcee) {
                log.warn("getting for trip {}: {}", trip.toString(), hcee.getMessage());
                continue;
            }
            int tripId = trip.getId();
            Map<String, Object> map = gson.fromJson(r, type);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            if (data == null || data.size() == 0)
                continue;
            List<Stop> schedule = stopTimesDAO.getSchedule(tripId);
            log.info("data = {}", data);
            Optional<Map<String, Object>> attrOpt = data.stream().filter(m -> m.containsKey("attributes"))
                    .map(m -> (Map<String, Object>) m.get("attributes"))
                    .filter(m -> !(m.containsKey("status") && m.get("status") != null && m.get("status").equals("Departed")))
                    .sorted((a, b) -> ((Number) a.get("stop_sequence")).intValue() - ((Number) b.get("stop_sequence")).intValue())
                    .findFirst();
            if (!attrOpt.isPresent()) {
                log.warn("no attributes for {}", trip);
                continue;
            }
            Map<String, Object> attributes = attrOpt.get();
            int stopSequence = ((Number) attributes.get("stop_sequence")).intValue();
            String predictedTimeStr;
            log.info("trip: {}, attributes: {}", trip, attributes);
            if (attributes.containsKey("arrival_time") && attributes.get("arrival_time") != null)
                predictedTimeStr = (String) attributes.get("arrival_time");
            else
                predictedTimeStr = (String) attributes.get("departure_time");
            if (predictedTimeStr == null)
                throw new RuntimeException("can't find predicted time");
            LocalDateTime localDateTime = LocalDateTime.parse(predictedTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime predictedTime = ZonedDateTime.of(localDateTime, zoneId);
            Stop stop = schedule.get(stopSequence - 1);
            LocalTime localTime = stop.getArrivalTime();
            ZonedDateTime scheduledTime = ZonedDateTime.of(LocalDate.now(clock), localTime, zoneId);
            Duration delay = Duration.between(scheduledTime, predictedTime);
            ZonedDateTime now = ZonedDateTime.now(clock);
            Duration timeTilStop = Duration.between(now, predictedTime);
            if (log.isDebugEnabled())
                log.debug("{} {} {} {} {} {} {} {}",
                        trip.toString(),
                        stop.getName(),
                        stopSequence,
                        DateTimeFormatter.ISO_LOCAL_TIME.format(now),
                        DateTimeFormatter.ISO_LOCAL_TIME.format(scheduledTime),
                        DateTimeFormatter.ISO_LOCAL_TIME.format(predictedTime),
                        String.format("%d:%02d", delay.get(ChronoUnit.SECONDS) / 60, Math.abs(delay.get(ChronoUnit.SECONDS) % 60)),
                        String.format("%d:%02d", timeTilStop.get(ChronoUnit.SECONDS) / 60, Math.abs(timeTilStop.get(ChronoUnit.SECONDS) % 60))
                );
            onTimeDataDAO.addRecord(LocalDate.now(clock), Instant.now(clock), tripId, stop.getName(), stopSequence,
                    scheduledTime.toLocalTime(), predictedTime.toLocalTime(),
                    delay.get(ChronoUnit.SECONDS), timeTilStop.get(ChronoUnit.SECONDS));
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setRouteNames(String routeNames) {
        this.routeNames = routeNames;
    }

    public void setOnTimeDataDAO(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
    }

    public void setStopTimesDAO(StopTimesDAO stopTimesDAO) {
        this.stopTimesDAO = stopTimesDAO;
    }
}
