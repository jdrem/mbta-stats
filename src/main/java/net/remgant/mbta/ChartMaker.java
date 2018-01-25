package net.remgant.mbta;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jdr on 1/25/18.
 */
public class ChartMaker {
    public static void main(String args[]) throws IOException {
        new ChartMaker().run();
    }

    public void run() throws IOException {
        String chartName = "Trip 400";
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        String outFileName = "400.png";
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new SingleConnectionDataSource(dbUrl, dbUser, dbPwd, false);
        OnTimeDAOImpl fixture = new OnTimeDAOImpl(dataSource);
        Map<String, Object> data = fixture.findDataForTrip(LocalDate.of(2018, 1, 25), "CR-Weekday-Fall-17-400");
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("stops");

        TimeSeries timeSeries = new TimeSeries("Delay");
        for (Map<String, Object> o : list) {
            Instant i = Instant.parse(o.get("timestamp").toString());
            ZonedDateTime zdt = i.atZone(ZoneId.of("America/New_York"));
            Minute minute = new Minute(zdt.getMinute(), zdt.getHour(), zdt.getDayOfMonth(), zdt.getMonthValue(), zdt.getYear());
            int delay = Integer.parseInt(o.get("delay").toString());
            timeSeries.add(minute, delay);
        }
        dataset.addSeries(timeSeries);

        Map<LocalTime, String> stopTimes = new HashMap<>();

        list.stream().forEach(m -> {
            LocalTime t = LocalTime.parse(m.get("scheduledTime").toString());
            if (!stopTimes.containsKey(t))
                stopTimes.put(t, m.get("nextStop").toString());
        });

        timeSeries = new TimeSeries("Stops");
        for (Map.Entry<LocalTime, String> e : stopTimes.entrySet()) {
            ZonedDateTime zdt = ZonedDateTime.of(LocalDate.now(), e.getKey(), ZoneId.of("America/New_York"));
            Minute minute = new Minute(zdt.getMinute(), zdt.getHour(), zdt.getDayOfMonth(), zdt.getMonthValue(), zdt.getYear());
            timeSeries.add(minute, 0);
        }
        dataset.addSeries(timeSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                chartName,                  // title
                "Time",                     // x-axis label
                "Delay",                    // y-axis label
                dataset,                    // data
                true,                       // create legend?
                false,                      // generate tooltips?
                false                       // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        Rectangle2D.Double shape = new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0);
        for (int i = 0; i < dataset.getSeriesCount(); i++)
            renderer.setSeriesShape(i, shape);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        axis.setVerticalTickLabels(true);

        BufferedImage image = chart.createBufferedImage(800, 600);
        File imageFile = new File(outFileName);
        ImageIO.write(image, "png", imageFile);
    }
}
