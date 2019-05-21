package com.example.loadtest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
class TrackingJdbcConfigurationForStressTest{

    @Bean
    @Qualifier("dataSourceForStressTesting")
    public HikariDataSource trackingDataSourceForStress() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", "jdbc:h2:mem:TRACKING;");
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "");
        HikariDataSource ds = new HikariDataSource(config);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        ds.setLeakDetectionThreshold(60000);
        ds.setConnectionTimeout(30000);
        return ds;
    }
}

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TrackingJdbcConfigurationForStressTest.class)
public class LoadtestApplicationTests {

    int maxPoolSize = 1;
    int numberofUsers = 20;
    int queryExecutionTimeMs = 1;

    @Autowired
    @Qualifier("dataSourceForStressTesting")
    private HikariDataSource trackingDataSourceForStress;

    String sql = "select * from dual";

    @Before
    public void init(){
        trackingDataSourceForStress.setMaximumPoolSize(maxPoolSize);
    }

    @Test
    public void putLoadTest() throws SQLException, ExecutionException, InterruptedException {
        putLoadOnTracking(executeQueryForTestCallable);
    }

    private void putLoadOnTracking(Callable<String> executeQueryForTestCallable) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(numberofUsers);

        List<Callable<String>> callableTasks = new ArrayList<>();

        for (int i = 0; i < numberofUsers; i++) {
            System.out.println(" --------------------- Submitting task  " + i);
            callableTasks.add(executeQueryForTestCallable);
        }

        executorService.invokeAll(callableTasks);
    }

    Callable<String> executeQueryForTestCallable = () -> {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = trackingDataSourceForStress.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(" --------------------- Recieved Result for User " + Thread.currentThread().getId() );
                Thread.sleep(queryExecutionTimeMs);
                return "Success";
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            close(connection, stmt, rs);
        }
        return "failure";
    };

    private void close(Connection connection, Statement stmt, ResultSet rs) throws SQLException {
        rs.close();
        stmt.close();
        connection.close();
    }
}
