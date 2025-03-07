package org.MigrationTool.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.MigrationTool.Utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DatabasePool {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePool.class);
    private static final HikariDataSource dataSource;

    static {
        logger.info("Initializing database pool with URL: {}", ConfigLoader.getProperty("database.url"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ConfigLoader.getProperty("database.url"));
        config.setUsername(ConfigLoader.getProperty("database.user"));
        config.setPassword(ConfigLoader.getProperty("database.password"));
        config.setMaximumPoolSize(10);
        config.setDriverClassName("org.h2.Driver");

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
