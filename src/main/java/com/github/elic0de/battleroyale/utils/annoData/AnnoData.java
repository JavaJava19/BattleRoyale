package com.github.elic0de.battleroyale.utils.annoData;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.user.GameUserData;
import com.google.gson.internal.Primitives;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

@Getter
public class AnnoData {


    private static final String DATA_POOL_NAME = "MySqlHikariPool";
    private HikariDataSource dataSource;

    public String host = "localhost";

    public String database = "test";
    public String userName = "test";
    public String password = "test";

    public String parameters = "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    public int port = 3306;

    public int maximumPoolSize = 10;
    public int minimumIdle = 10;
    public int maxLifetime = 1800000;
    public int keepAliveTime = 30000;
    public int connectionTimeout = 20000;


    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public AnnoData() {
        final String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + parameters;

        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);

        // Authenticate
        dataSource.setUsername(userName);
        dataSource.setPassword(password);

        // Set connection pool options
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setKeepaliveTime(keepAliveTime);
        dataSource.setConnectionTimeout(connectionTimeout);
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

        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (String statementStr : Arrays.asList("SET DEFAULT_STORAGE_ENGINE = INNODB;", "SET FOREIGN_KEY_CHECKS = 1;")) {
                    statement.execute(statementStr);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create MySQL database tables", e);
        }
    }

    public void createTable(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(clazz.getAnnotation(DatabaseMySQL.class).table()).append(" (");

        Field[] fields = clazz.getDeclaredFields();
        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ColumnIgnore.class)) {
                continue;
            }

            if (field.isAnnotationPresent(Identification.class)) {
                String columnName = field.getName();
                String columnType = getColumnType(field.getType());
                columns.add(columnName + " " + columnType + " NOT NULL UNIQUE PRIMARY KEY");
                continue;
            }

            String columnName = field.getName();
            String columnType = getColumnType(field.getType());
            columns.add(columnName + " " + columnType + " NOT NULL");
        }

        sb.append(String.join(", ", columns)).append(") CHARACTER SET utf8 COLLATE utf8_unicode_ci;");

        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sb.toString());
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create MySQL database tables", e);
        }
    }

    public <T> T insertData(T object) {
        try {
            // todo DRY
            return new DatabaseObjectMap<>(object, getConnection()).insertData().getObject();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T find(@NotNull Class<T> objectClass, String key)  {
        try {
            return new DatabaseObjectMap<>(getDefaults(objectClass), getConnection()).readFromDatabase(getDefaults(objectClass), key).getObject();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void updateData(T object) {
        try {
            new DatabaseObjectMap<>(object, getConnection()).updateData(object).getObject();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getDefaults(@NotNull Class<T> objectClass) throws InvocationTargetException,
            InstantiationException, IllegalAccessException, IllegalArgumentException {
        // Validate that the object type constructor with zero arguments
        final Optional<Constructor<?>> constructors = Arrays.stream(objectClass.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException("Class type must have a zero-argument constructor: " + objectClass.getName());
        }

        // Get the constructor
        final Constructor<?> constructor = constructors.get();
        constructor.setAccessible(true);

        // Instantiate an object of the class type to act as the base
        @SuppressWarnings("unchecked") final T defaults = (T) constructor.newInstance();
        return defaults;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private String getColumnType(Class<?> type) {
        // todo mapping
        if (type == int.class || type == Integer.class) {
            return "INT";
        } else if (type == long.class || type == Long.class || type == BigDecimal.class) {
            return "BIGINT";
        } else if (type == float.class || type == Float.class) {
            return "FLOAT";
        } else if (type == double.class || type == Double.class) {
            return "DOUBLE";
        } else if (type == boolean.class || type == Boolean.class) {
            return "BOOLEAN";
        } else if (type == String.class) {
            return "VARCHAR(255)";
        } else {
            // Handle other types as necessary
            return "VARCHAR(255)";
        }
    }


}

