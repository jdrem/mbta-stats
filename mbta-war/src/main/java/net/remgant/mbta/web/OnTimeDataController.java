package net.remgant.mbta.web;

import net.remgant.mbta.*;
import org.apache.tomcat.jni.Local;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/25/18.
 */
@RestController
public class OnTimeDataController {

    private StopTimesDAO stopTimesDAO;
    private OnTimeDataDAO onTimeDataDAO;
    private ChartMaker chartMaker;

    public OnTimeDataController(OnTimeDataDAO onTimeDataDAO, StopTimesDAO stopTimesDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
        this.stopTimesDAO = stopTimesDAO;
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
        return chartMaker.createImageForDateAndTrip(LocalDate.of(year, month, day), tripId, 800, 600);
    }

    @RequestMapping(value = "/current_trip_data", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> findCurrentData() {
        return onTimeDataDAO.findCurrentStopData();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data Not Found")
    @ExceptionHandler(NoDataForTripException.class)
    public void noDataForTrip() {
    }
}
