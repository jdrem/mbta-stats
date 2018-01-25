package net.remgant.mbta;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by jdr on 1/25/18.
 */
@RestController
public class OnTimeDataController {

    OnTimeDataDAO onTimeDataDAO;

    public OnTimeDataController(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
    }

    @RequestMapping(value = "/on_time_data/{year}/{month}/{day}/{tripId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> processOnTimeDataRequest(@PathVariable("year") int year,
                                                       @PathVariable("month") int month,
                                                       @PathVariable("day") int day,
                                                       @PathVariable("tripId") int tripId) {
        return onTimeDataDAO.findDataForTrip(LocalDate.of(year,month,day),String.format("CR-Weekday-Fall-17-%03d",tripId));
    }
}
