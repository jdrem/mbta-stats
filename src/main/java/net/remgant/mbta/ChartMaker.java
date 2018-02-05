package net.remgant.mbta;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
        ChartMaker chartMaker = new ChartMaker();
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new SingleConnectionDataSource(dbUrl, dbUser, dbPwd, false);
        OnTimeDataDAO onTimeDataDAO = new OnTimeDAOImpl(dataSource);
        chartMaker.setOnTimeDataDAO(onTimeDataDAO);
        LocalDate localDate = LocalDate.parse(args[0]);
        int tripId = Integer.parseInt(args[1]);
        byte[] chartAsBytes = chartMaker.createImageForDateAndTrip(localDate, tripId, 800, 600);
        String outFileName = String.format("trip-%s-%03d.png", localDate, tripId);
        Path path = FileSystems.getDefault().getPath(outFileName);
        Files.write(path,chartAsBytes);
    }

    public ChartMaker() {
    }

    public ChartMaker(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
    }

    private OnTimeDataDAO onTimeDataDAO;

    public byte[] createImageForDateAndTrip(LocalDate localDate, int tripId, int width, int height) throws IOException {
        String chartName = String.format("Trip %03d (%s)", tripId, localDate);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        Map<String, Object> data = onTimeDataDAO.findDataForTrip(localDate, tripId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("stops");

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
            ZonedDateTime zdt = ZonedDateTime.of(localDate, e.getKey(), ZoneId.of("America/New_York"));
            Minute minute = new Minute(zdt.getMinute(), zdt.getHour(), zdt.getDayOfMonth(), zdt.getMonthValue(), zdt.getYear());
            timeSeries.add(minute, -5);
            XYTextAnnotation annotation = new XYTextAnnotation(e.getValue(), minute.getFirstMillisecond(), -5);
            annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
            annotation.setPaint(Color.black);
            annotation.setRotationAnchor(TextAnchor.BOTTOM_LEFT);
            annotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
            annotation.setRotationAngle(Math.PI / 2.0);
            plot.addAnnotation(annotation);

        }
        dataset.addSeries(timeSeries);

        Map<String, ZonedDateTime> actualStops = new HashMap<>();
        list.stream().forEach(m -> {
            String stop = m.get("nextStop").toString();
            ZonedDateTime zdt = Instant.parse(m.get("timestamp").toString()).atZone(ZoneId.of("America/New_York"));
            actualStops.put(stop, zdt);
        });

        timeSeries = new TimeSeries("Actual");
        for (Map.Entry<String, ZonedDateTime> e : actualStops.entrySet()) {
            Minute minute = new Minute(e.getValue().getMinute(), e.getValue().getHour(), e.getValue().getDayOfMonth(), e.getValue().getMonthValue(), e.getValue().getYear());
            timeSeries.add(minute, 5);
            XYTextAnnotation annotation = new XYTextAnnotation(e.getKey(), minute.getFirstMillisecond(), 5);
            annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
            annotation.setPaint(Color.black);
            annotation.setRotationAnchor(TextAnchor.BOTTOM_RIGHT);
            annotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            annotation.setRotationAngle(Math.PI / 2.0);
            plot.addAnnotation(annotation);
        }
        dataset.addSeries(timeSeries);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        Rectangle2D.Double shape = new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0);
        for (int i = 0; i < dataset.getSeriesCount(); i++)
            renderer.setSeriesShape(i, shape);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        axis.setVerticalTickLabels(true);

        BufferedImage image = chart.createBufferedImage(width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        outputStream.flush();
        return outputStream.toByteArray();
    }

    public void setOnTimeDataDAO(OnTimeDataDAO onTimeDataDAO) {
        this.onTimeDataDAO = onTimeDataDAO;
    }
}
