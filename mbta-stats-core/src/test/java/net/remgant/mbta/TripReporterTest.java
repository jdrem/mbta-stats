package net.remgant.mbta;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by jdr on 2/7/18.
 */
public class TripReporterTest {
    @Test
    public void test1() {
        RestTemplate mockRestTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(mockRestTemplate).build();
        server.expect(requestToUriTemplate("https://api-v3.mbta.com/predictions?filter[trip]={TripID}", "CR-Weekday-Fall-17-419"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expected412, MediaType.APPLICATION_JSON));
        TripReporter tripReporter = new TripReporter() {
            @Override
            public void init() {
                this.clock = Clock.fixed(Instant.parse("2018-02-06T10:12:00.00Z"), ZoneId.of("America/New_York"));
                this.restTemplate = mockRestTemplate;
                this.trips = Collections.singletonList(new Trip("CR-Fitchburg", 419, "Weekday", "Fall-17"));
                this.stopTimesDAO = new StopTimesDAO() {
                    @Override
                    public List<String> findStopsForTrip(int routeId) {
                        return null;
                    }

                    @Override
                    public List<String> findAllRoutes() {
                        return null;
                    }

                    @Override
                    public List<Stop> getSchedule(int tripId) {
                        List<Stop> list = new ArrayList<>();
                        // String name, LocalTime arrivalTime, boolean nextDay, int sequence
                        list.add(new Stop("Stop A", LocalTime.of(17, 0), false, 1));
                        list.add(new Stop("Stop B", LocalTime.of(17, 1), false, 2));
                        list.add(new Stop("Stop C", LocalTime.of(17, 2), false, 3));
                        list.add(new Stop("Stop D", LocalTime.of(17, 3), false, 4));
                        list.add(new Stop("Stop E", LocalTime.of(17, 4), false, 5));
                        list.add(new Stop("Stop F", LocalTime.of(17, 5), false, 6));
                        list.add(new Stop("Stop G", LocalTime.of(17, 6), false, 7));
                        list.add(new Stop("Stop H", LocalTime.of(17, 7), false, 8));
                        list.add(new Stop("Stop I", LocalTime.of(17, 8), false, 9));
                        list.add(new Stop("Stop J", LocalTime.of(17, 9), false, 10));
                        list.add(new Stop("Stop K", LocalTime.of(17, 10), false, 11));
                        list.add(new Stop("Stop L", LocalTime.of(17, 11), false, 12));
                        list.add(new Stop("Stop M", LocalTime.of(17, 12), false, 13));
                        list.add(new Stop("Stop N", LocalTime.of(17, 13), false, 14));
                        list.add(new Stop("Stop O", LocalTime.of(17, 14), false, 15));
                        list.add(new Stop("Stop P", LocalTime.of(17, 15), false, 16));
                        list.add(new Stop("Stop Q", LocalTime.of(17, 16), false, 17));
                        list.add(new Stop("Stop R", LocalTime.of(17, 17), false, 18));
                        list.add(new Stop("Stop S", LocalTime.of(17, 18), false, 19));
                        list.add(new Stop("Stop T", LocalTime.of(17, 19), false, 20));
                        return list;
                    }

                    @Override
                    public List<Trip> findTripsForRoute(String routeName, LocalDate date) {
                        return null;
                    }

                    @Override
                    public void addTrip(String routeName, String scheduleType, String calendarName, int tripId, String headSign, String dir) {
                    }

                    @Override
                    public void addStopToRoute(int tripId, LocalTime arrivalTime, boolean nextDay, String stopName, int stopSequence) {
                    }

                    @Override
                    public List<String> findRoutesForDay(LocalDate date) {
                        return null;
                    }
                };
                this.onTimeDataDAO = new OnTimeDataDAO() {
                    @Override
                    public void addRecord(LocalDate date, Instant timeStamp, int tripId, String nextStop, int stopSequence, LocalTime scheduledTime, LocalTime predictedTime, long delay, long timeTilStop) {
                        assertEquals("wrong next stop", "Stop F", nextStop);
                    }

                    @Override
                    public Map<String, Object> findDataForTrip(LocalDate date, int tripId) throws NoDataForTripException {
                        return null;
                    }

                    @Override
                    public List<Map<String, Object>> findCurrentStopData() {
                        return null;
                    }
                };
            }
        };

        tripReporter.init();
        tripReporter.processTripData();
    }


    String expected = "{\"jsonapi\":{\"version\":\"1.0\"},\"data\":[{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-419\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Station\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-419-North Station-1\",\"attributes\":{\"track\":\"8\",\"stop_sequence\":1,\"status\":\"Departed\",\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-06T17:00:00-05:00\",\"arrival_time\":null}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-419\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Shirley\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-419-Shirley-15\",\"attributes\":{\"track\":null,\"stop_sequence\":15,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-06T18:18:59-05:00\",\"arrival_time\":\"2018-02-06T18:18:59-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-419\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Leominster\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-419-North Leominster-16\",\"attributes\":{\"track\":null,\"stop_sequence\":16,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-06T18:27:35-05:00\",\"arrival_time\":\"2018-02-06T18:27:35-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-419\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Fitchburg\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-419-Fitchburg-17\",\"attributes\":{\"track\":null,\"stop_sequence\":17,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-06T18:36:10-05:00\",\"arrival_time\":\"2018-02-06T18:36:10-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-419\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Wachusett\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-419-Wachusett-18\",\"attributes\":{\"track\":null,\"stop_sequence\":18,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":null,\"arrival_time\":\"2018-02-06T18:47:33-05:00\"}}]}";

    String expected401 = "{\"jsonapi\":{\"version\":\"1.0\"},\"data\":[{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-401\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Station\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-401-North Station-1\",\"attributes\":{\"track\":\"10\",\"stop_sequence\":1,\"status\":\"Departed\",\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-07T06:25:00-05:00\",\"arrival_time\":null}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-401\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Shirley\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-401-Shirley-6\",\"attributes\":{\"track\":null,\"stop_sequence\":6,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-07T07:19:06-05:00\",\"arrival_time\":\"2018-02-07T07:19:06-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-401\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Leominster\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-401-North Leominster-7\",\"attributes\":{\"track\":null,\"stop_sequence\":7,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-07T07:28:00-05:00\",\"arrival_time\":\"2018-02-07T07:26:42-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-401\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Fitchburg\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-401-Fitchburg-8\",\"attributes\":{\"track\":null,\"stop_sequence\":8,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":\"2018-02-07T07:34:18-05:00\",\"arrival_time\":\"2018-02-07T07:34:18-05:00\"}},{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-401\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Wachusett\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-401-Wachusett-9\",\"attributes\":{\"track\":null,\"stop_sequence\":9,\"status\":null,\"schedule_relationship\":null,\"direction_id\":0,\"departure_time\":null,\"arrival_time\":\"2018-02-07T07:42:57-05:00\"}}]}";

    String expected406 = "{\"jsonapi\":{\"version\":\"1.0\"},\"data\":[" +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Station\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-North Station-17\",\"attributes\":{\"track\":null,\"stop_sequence\":17,\"status\":\"On time\",\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T08:21:00-05:00\",\"arrival_time\":null}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Kendal Green\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Kendal Green-11\",\"attributes\":{\"track\":null,\"stop_sequence\":11,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T07:51:00-05:00\",\"arrival_time\":\"2018-02-07T07:49:01-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Brandeis/ Roberts\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Brandeis/ Roberts-12\",\"attributes\":{\"track\":null,\"stop_sequence\":12,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T07:54:00-05:00\",\"arrival_time\":\"2018-02-07T07:53:05-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Waltham\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Waltham-13\",\"attributes\":{\"track\":null,\"stop_sequence\":13,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T07:58:00-05:00\",\"arrival_time\":\"2018-02-07T07:56:35-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Waverley\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Waverley-14\",\"attributes\":{\"track\":null,\"stop_sequence\":14,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T08:03:00-05:00\",\"arrival_time\":\"2018-02-07T08:01:34-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Belmont\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Belmont-15\",\"attributes\":{\"track\":null,\"stop_sequence\":15,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T08:06:00-05:00\",\"arrival_time\":\"2018-02-07T08:05:31-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-406\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Porter Square\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-406-Porter Square-16\",\"attributes\":{\"track\":null,\"stop_sequence\":16,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T08:11:00-05:00\",\"arrival_time\":\"2018-02-07T08:10:25-05:00\"}}]}";


    String expected412 = "{\"jsonapi\":{\"version\":\"1.0\"},\"data\":[" +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"North Station\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-North Station-18\",\"attributes\":{\"track\":null,\"stop_sequence\":18,\"status\":\"On time\",\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T11:07:00-05:00\",\"arrival_time\":null}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Littleton / Rte 495\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Littleton / Rte 495-6\",\"attributes\":{\"track\":null,\"stop_sequence\":6,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:14:07-05:00\",\"arrival_time\":\"2018-02-07T10:14:07-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"South Acton\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-South Acton-7\",\"attributes\":{\"track\":null,\"stop_sequence\":7,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:19:31-05:00\",\"arrival_time\":\"2018-02-07T10:19:31-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"West Concord\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-West Concord-8\",\"attributes\":{\"track\":null,\"stop_sequence\":8,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:24:08-05:00\",\"arrival_time\":\"2018-02-07T10:24:08-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Concord\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Concord-9\",\"attributes\":{\"track\":null,\"stop_sequence\":9,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:27:55-05:00\",\"arrival_time\":\"2018-02-07T10:27:55-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Lincoln\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Lincoln-10\",\"attributes\":{\"track\":null,\"stop_sequence\":10,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:33:00-05:00\",\"arrival_time\":\"2018-02-07T10:33:00-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Hastings\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Hastings-11\",\"attributes\":{\"track\":null,\"stop_sequence\":11,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:37:44-05:00\",\"arrival_time\":\"2018-02-07T10:37:44-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Kendal Green\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Kendal Green-12\",\"attributes\":{\"track\":null,\"stop_sequence\":12,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:39:32-05:00\",\"arrival_time\":\"2018-02-07T10:39:32-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Brandeis/ Roberts\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Brandeis/ Roberts-13\",\"attributes\":{\"track\":null,\"stop_sequence\":13,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:42:40-05:00\",\"arrival_time\":\"2018-02-07T10:42:40-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Waltham\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Waltham-14\",\"attributes\":{\"track\":null,\"stop_sequence\":14,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:46:43-05:00\",\"arrival_time\":\"2018-02-07T10:46:43-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Waverley\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Waverley-15\",\"attributes\":{\"track\":null,\"stop_sequence\":15,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:51:15-05:00\",\"arrival_time\":\"2018-02-07T10:51:15-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Belmont\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Belmont-16\",\"attributes\":{\"track\":null,\"stop_sequence\":16,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:53:55-05:00\",\"arrival_time\":\"2018-02-07T10:53:55-05:00\"}}," +
            "{\"type\":\"prediction\",\"relationships\":{\"trip\":{\"data\":{\"type\":\"trip\",\"id\":\"CR-Weekday-Fall-17-412\"}},\"stop\":{\"data\":{\"type\":\"stop\",\"id\":\"Porter Square\"}},\"route\":{\"data\":{\"type\":\"route\",\"id\":\"CR-Fitchburg\"}}},\"id\":\"prediction-CR-Weekday-Fall-17-412-Porter Square-17\",\"attributes\":{\"track\":null,\"stop_sequence\":17,\"status\":null,\"schedule_relationship\":null,\"direction_id\":1,\"departure_time\":\"2018-02-07T10:57:57-05:00\",\"arrival_time\":\"2018-02-07T10:57:57-05:00\"}}]}";
}