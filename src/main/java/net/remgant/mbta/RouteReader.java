package net.remgant.mbta;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jdr on 1/14/18.
 */
public class RouteReader {

    String dataUrl = "https://www.mbta.com/uploadedfiles/MBTA_GTFS.zip";

    public void updateRoutes() throws IOException {
        URL url = new URL(dataUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        InputStream inputStream = (InputStream) urlConnection.getContent();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Pattern tripPtrn = Pattern.compile("\"?CR-(Sunday|Saturday|Weekday)-((?:Fall|Spring)-\\d\\d)-(\\d+)\"?");

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            String s = String.format("Entry: %s len %d added %TD",
                    entry.getName(), entry.getSize(),
                    new Date(entry.getTime()));
            System.out.println(s);
//            if (!(entry.getName().equals("stops.txt") || entry.getName().equals("trips.txt")))
            if (!(entry.getName().equals("stop_times.txt")))
                continue;
            ByteArrayOutputStream output = null;
            byte[] buffer = new byte[2048];

            try {
                output = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            } finally {
                // we must always close the output file
                if (output != null) output.close();
            }
            s = new String(output.toByteArray());
            if (entry.getName().equals("stops.txt")) {
                Scanner scanner = new Scanner(s);
                scanner.useDelimiter(",");
                if (scanner.hasNext()) scanner.nextLine();
                while (scanner.hasNext()) {
                    String id = scanner.next();
                    id = id.substring(1, id.length() - 1);
                    if (Character.isUpperCase(id.charAt(0)))
                        System.out.println(id);
                    scanner.nextLine();
                }

            }
            if (entry.getName().equals("stop_times.txt")) {
                Scanner scanner = new Scanner(s);
                scanner.useDelimiter(",");
                if (scanner.hasNext()) scanner.nextLine();
                while (scanner.hasNext()) {
                    //"CR-Weekday-Fall-17-400","4:50:00","4:50:00","Wachusett",1,"",0,1,1,""
                    s = scanner.next();
                    if (!s.startsWith("\"CR-")) {
                        scanner.nextLine();
                        continue;
                    }
                    String tripName = s.substring(1, s.length() - 1);
                    Matcher matcher = tripPtrn.matcher(tripName);
                    if (!matcher.matches())
                        continue;
                    String scheduleType = matcher.group(1);
                    String calendarName = matcher.group(2);
                    int tripId = Integer.parseInt(matcher.group(3));
                    s = scanner.next();
                    String t[] = s.substring(1, s.length() - 1).split(":");
                    int h = Integer.parseInt(t[0]);
                    boolean nextDay = false;
                    if (h > 23) {
                        h %= 24;
                        nextDay = true;
                    }
                    int m = Integer.parseInt(t[1]);
                    LocalTime arrivalTime = LocalTime.of(h, m);
                    scanner.next();
                    s = scanner.next();
                    String stopName = s.substring(1, s.length() - 1);
                    int stopSequence = scanner.nextInt();
                    System.out.printf("%s %s %b %s %d%n", tripId, arrivalTime, nextDay, stopName, stopSequence);
                    try {
                        stopTimesDAO.addStopToRoute(tripId, arrivalTime, nextDay, stopName, stopSequence);
                    } catch (DuplicateKeyException dke) {

                    }
                    scanner.nextLine();
                }
            } else if (entry.getName().equals("trips.txt")) {
                Scanner scanner = new Scanner(s);
                scanner.useDelimiter(",");
                if (scanner.hasNext()) scanner.nextLine();
                while (scanner.hasNext()) {
                    //"CR-Franklin","CR-Weekday-SouthSide-Fall-17-RegReduced","CR-Weekday-Fall-17-700","South Station","700",1,"","9880005",1,""
                    s = scanner.next();
                    System.out.println(s);

                    if (!s.startsWith("\"CR-")) {
                        scanner.nextLine();
                        continue;
                    }
                    String routeName = s.substring(1, s.length() - 1);
                    scanner.next();
                    s = scanner.next();
                    Matcher m = tripPtrn.matcher(s);
                    if (!m.matches())
                        continue;
                    String scheduleType = m.group(1);
                    String calendarName = m.group(2);
                    int tripId = Integer.parseInt(m.group(3));
                    s = scanner.next();
                    String headSign = s.substring(1, s.length() - 1);
                    scanner.next();
                    s = scanner.next();
                    String dir = s;
                    stopTimesDAO.addTrip(routeName, scheduleType, calendarName, tripId, headSign, dir);
                    scanner.nextLine();

                }
            }
        }
    }

    StopTimesDAO stopTimesDAO;

    public void setStopTimesDAO(StopTimesDAO stopTimesDAO) {
        this.stopTimesDAO = stopTimesDAO;
    }

    public static void main(String args[]) throws IOException {
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new SingleConnectionDataSource(dbUrl, dbUser, dbPwd, false);
        StopTimesDAOImpl dao = new StopTimesDAOImpl();
        dao.setJdbcTemplate(new JdbcTemplate(dataSource));
        RouteReader routeReader = new RouteReader();
        routeReader.setStopTimesDAO(dao);
        routeReader.updateRoutes();
//        new RouteReader().uuu();
    }
}
