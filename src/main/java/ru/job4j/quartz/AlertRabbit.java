package ru.job4j.quartz;

import java.sql.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        try {
            Properties properties = getRabbitProperties();
            Integer interval = Integer.parseInt(properties.getProperty("rabbit.interval"));

            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            data.put("connection", getConnection());
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Properties getRabbitProperties() {
        Properties properties = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream io = classloader.getResourceAsStream("rabbit.properties")) {
            properties.load(io);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private static Connection getConnection() {
        Connection connection = null;

        Properties properties = getRabbitProperties();
        try  {
            Class.forName(properties.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password")
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    private static void saveCurrentDataInDataBase(Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?)")) {
            ps.setTimestamp(1, new Timestamp(Calendar.getInstance().getTime().getTime()));
            ps.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            saveCurrentDataInDataBase(connection);
            store.add(System.currentTimeMillis());
        }
    }
}