package net.remgant.mbta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

/**
 * Created by jdr on 1/25/18.
 */
@SpringBootApplication
public class RestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    @Value("${db.url}")
    String dbUrl;
    @Value("${db.user}")
    String dbUser;
    @Value("${db.pwd}")
    String dbPwd;

    @Bean
    public DataSource dataSource() {
       return new SingleConnectionDataSource(dbUrl,dbUser,dbPwd,false);
    }

    @Bean
    public OnTimeDataDAO onTimeDataDAO() {
        return new OnTimeDAOImpl(dataSource());
    }

    @Bean
    public OnTimeDataController onTimeDataController() {
        return new OnTimeDataController(onTimeDataDAO());
    }
}
