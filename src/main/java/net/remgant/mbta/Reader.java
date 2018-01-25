package net.remgant.mbta;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by jdr on 1/14/18.
 */
public class Reader {
    private RestTemplate restTemplate = new RestTemplate();
    private Gson gson = new Gson();
    Type type = new TypeToken<Map<String, Object>>(){}.getType();
    public void run() {
//        String r = restTemplate.getForObject("https://api-v3.mbta.com/predictions?filter[stop]={StopID}",String.class,"Ayer");
//        String r = restTemplate.getForObject("https://api-v3.mbta.com/predictions?filter[route]={RouteID}",String.class,"CR-Fitchburg");
        String r = restTemplate.getForObject("https://api-v3.mbta.com/predictions?filter[trip]={TripID}",String.class,"CR-Weekday-Fall-17-412");

        System.out.println(r);
        Map<String,Object> map = gson.fromJson(r,type);
        System.out.println(map);
    }

    public static void main(String args[]) {
        new Reader().run();
    }
}
