package org.MigrationTool.Main;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabasePool {
    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DatabasePool.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties properties = new Properties();
            if (input == null) {
                throw new IOException("Cannot find database.properties");
            }
            properties.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("database.url"));
            config.setUsername(properties.getProperty("database.user"));
            config.setPassword(properties.getProperty("database.password"));
            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("database.pool.size", "5")));
            config.setDriverClassName("org.h2.Driver");

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
