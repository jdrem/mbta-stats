package net.remgant.mbta;

/**
 * Created by jdr on 2/4/18.
 */
@SuppressWarnings("UnusedDeclaration")
public class Trip {
    int id;
    String calendar;
    String type;
    String route;

    public Trip() {
    }

    public Trip(String route, int id, String type, String calendar) {
        this.route = route;
        this.id = id;
        this.calendar = calendar;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return String.format("CR-%s-%s-%03d",type,calendar,id);
    }
}
