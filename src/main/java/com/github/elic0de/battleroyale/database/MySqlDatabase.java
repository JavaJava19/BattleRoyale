package com.github.elic0de.battleroyale.database;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlDatabase extends Database {

    /**
     * Name of the Hikari connection pool
     */
    private static final String DATA_POOL_NAME = "BattleRoyaleHikariPool";

    /**
     * The Hikari data source
     */
    private HikariDataSource dataSource;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setConnection() {
        final Settings settings = plugin.getSettings();

        // Create jdbc driver connection url
        final String jdbcUrl = "jdbc:mysql://" + settings.getMySqlHost() + ":" + settings.getMySqlPort() + "/"
                + settings.getMySqlDatabase() + settings.getMySqlConnectionParameters();
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);

        // Authenticate
        dataSource.setUsername(settings.getMySqlUsername());
        dataSource.setPassword(settings.getMySqlPassword());

        // Set connection pool options
        dataSource.setMaximumPoolSize(settings.getMySqlConnectionPoolSize());
        dataSource.setMinimumIdle(settings.getMySqlConnectionPoolIdle());
        dataSource.setMaxLifetime(settings.getMySqlConnectionPoolLifetime());
        dataSource.setKeepaliveTime(settings.getMySqlConnectionPoolKeepAlive());
        dataSource.setConnectionTimeout(settings.getMySqlConnectionPoolTimeout());
        dataSource.setPoolName(DATA_POOL_NAME);

        // Set additional connection pool properties
        dataSource.setDataSourceProperties(new Properties() {{
            put("cachePrepStmts", "true");
            put("prepStmtCacheSize", "250");
            put("prepStmtCacheSqlLimit", "2048");
            put("useServerPrepStmts", "true");
            put("useLocalSessionState", "true");
            put("useLocalTransactionState", "true");
            put("rewriteBatchedStatements", "true");
            put("cacheResultSetMetadata", "true");
            put("cacheServerConfiguration", "true");
            put("elideSetAutoCommits", "true");
            put("maintainTimeStats", "false");
        }});
    }

    public MySqlDatabase(@NotNull BattleRoyale plugin) {
        super(plugin, "mysql_schema.sql");
    }

    @Override
    public void initialize() throws RuntimeException {

    }

    @Override
    public void close() {

    }
}
