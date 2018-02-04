package net.remgant.mbta;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Created by jdr on 1/25/18.
 */
@RestController
public class OnTimeDataController {

    private OnTimeDataDAO onTimeDataDAO;
    private ChartMaker chartMaker;

    public OnTimeDataController(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
        this.chartMaker = new ChartMaker(onTimeDataDAO);
    }

    @RequestMapping(value = "/on_time_data/{year}/{month}/{day}/{tripId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> processOnTimeDataRequest(@PathVariable("year") int year,
                                                        @PathVariable("month") int month,
                                                        @PathVariable("day") int day,
                                                        @PathVariable("tripId") int tripId) {
        return onTimeDataDAO.findDataForTrip(LocalDate.of(year, month, day), tripId);
    }

    @RequestMapping(value = "/on_time_image/{year}-{month}-{day}-{tripId}.png", method = RequestMethod.GET)
    @ResponseBody
    public Object processImageRequest(@PathVariable("year") int year,
                                      @PathVariable("month") int month,
                                      @PathVariable("day") int day,
                                      @PathVariable("tripId") int tripId) {
        try {
            return chartMaker.createImageForDateAndTrip(LocalDate.of(year, month, day), tripId, 800, 600);
        } catch (IOException e) {
            //TODO return custom error page instead?
            throw new RuntimeException(e);
        }
    }
}
