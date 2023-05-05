package com.github.elic0de.battleroyale.utils.annoData;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions.NON_NUMERIC_CONVERSIONS;
import static dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions.convertNumber;

public class DatabaseObjectMap<T> extends LinkedHashMap<String, Object> {

    /*
    * 大まかに修正しないといけない
    * このままだとライブラリとして使えない
    * 型少ししか対応できてない
    * コード汚すぎる
    * */
    private final Class<T> objectClass;

    private final Connection connection;

    @SuppressWarnings("unchecked")
    public DatabaseObjectMap(@NotNull T object, Connection connection) {
        super();

        // Validate that the @DatabaseMySQL annotation is present
        if (!object.getClass().isAnnotationPresent(DatabaseMySQL.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @DatabaseMySQL");
        }
        this.connection = connection;

        // Read the object to the map
        this.objectClass = (Class<T>) object.getClass();
        this.readDefaults(object);
    }

    @SuppressWarnings("unchecked")
    private void readDefaults(@NotNull T object) throws IllegalArgumentException {
        // Validate object
        if (!object.getClass().isAnnotationPresent(DatabaseMySQL.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @YamlFile");
        }

        // Check if this is a rooted map, then begin iterating through the fields
        final Field[] fields = object.getClass().getDeclaredFields();
        int fieldIndex = 0;
        for (final Field field : fields) {
            // Ensure the field is accessible
            field.setAccessible(true);

            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(ColumnIgnore.class)) {
                continue;
            }

            // If the field is annotated with @YamlKey, use the value as the key
            final String key = field.isAnnotationPresent(Column.class)
                    ? field.getAnnotation(Column.class).value()
                    : field.getName();


            // Attempt to read the value from the field and add it to the map
            try {
                final Optional<Object> value = readFieldValue(field, object);
                this.put(key, value.orElse(null));
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to read field " + field.getName() + " from object " +
                        object.getClass().getName() + " to map at YAML path " + field.getName(), e);
            }

            fieldIndex++;
        }
    }


    public DatabaseObjectMap<T> insertData() {
        final String tableName = objectClass.getAnnotation(DatabaseMySQL.class).table();
        String columns = String.join(", ", this.keySet());
        String val = values().stream()
                .map(o -> Objects.toString("'" + o + "'", "'test'"))
                .collect(Collectors.joining(", "));
        String statementString = String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, columns, val);

        try (Connection connection = this.connection) {
            try (PreparedStatement statement = connection.prepareStatement(statementString)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create MySQL database tables", e);
        }
        return this;
    }

    public DatabaseObjectMap<T> updateData(T object) {
        final String tableName = objectClass.getAnnotation(DatabaseMySQL.class).table();
        final Field[] fields = object.getClass().getDeclaredFields();
        final String clauses = entrySet()
                .stream()
                .map(e -> e.getKey() + " = '" + e.getValue() + "'")
                .collect(Collectors.joining(", "));

        String query = "";
        for (final Field field : fields) {
            // Ensure the field is accessible
            field.setAccessible(true);
            if (field.isAnnotationPresent(Identification.class))
                query = field.getName();

        }

        String statementString = String.format("UPDATE %s SET %s WHERE %s;", tableName, clauses, String.format("%s = '%s'", query, get(query)));

        System.out.println(statementString);

        try (Connection connection = this.connection) {
            try (PreparedStatement statement = connection.prepareStatement(statementString)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create MySQL database tables", e);
        }
        return this;
    }

    public DatabaseObjectMap<T> readFromDatabase(@NotNull T object, String key) {
        if (!object.getClass().isAnnotationPresent(DatabaseMySQL.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @DatabaseMySQL");
        }

        final String tableName = object.getClass().getAnnotation(DatabaseMySQL.class).table();
        final Field[] fields = object.getClass().getDeclaredFields();

        String query = "";
        for (final Field field : fields) {
            // Ensure the field is accessible
            field.setAccessible(true);

            // If the field is annotated with @YamlKey, use the value as the key
            final String column = field.isAnnotationPresent(Column.class)
                    ? field.getAnnotation(Column.class).value()
                    : field.getName();

            if (field.isAnnotationPresent(Identification.class))
                query = field.getName();

            // Attempt to read the value from the field and add it to the map
            try {
                final Optional<Object> value = readFieldValue(field, object);
                this.put(column, value.orElse(null));
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to read field " + field.getName() + " from object " +
                        object.getClass().getName() + " to map at column " + field.getName(), e);
            }
        }

        String columns = String.join(", ", this.keySet());

        String statementString = String.format("SELECT %s FROM %s WHERE %s;",  columns, tableName,
                String.format("%s = '%s'", query, key));

        try (Connection connection = this.connection) {
                try (PreparedStatement  statement = connection.prepareStatement(statementString)) {
                    final ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        this.forEach((s, o) -> {
                            try {
                                this.put(s, resultSet.getObject(s));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to create MySQL database tables", e);
        }
        return this;
    }

    private T applyMapTo(@NotNull T defaults) throws IllegalArgumentException {
        // Iterate through each field
        final Field[] fields = defaults.getClass().getDeclaredFields();
        for (final Field field : fields) {
            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(ColumnIgnore.class)) {
                continue;
            }

            // If the field is annotated with @YamlKey, use the value as the key
            final String key = field.isAnnotationPresent(Column.class) ?
                    field.getAnnotation(Column.class).value() : field.getName();
            Optional.ofNullable(this.get(key)).ifPresent(value -> {
                try {
                    writeFieldValue(field, defaults, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Unable to write field " + field.getName() + " from object " +
                            defaults.getClass().getName() + " to YAML path " + field.getName(), e);
                }
            });

        }
        return defaults;
    }

    private <Y> void writeFieldValue(@NotNull Field field, @NotNull T object, @NotNull Y value) throws
            IllegalAccessException, IllegalArgumentException {
        // Set the field to be accessible
        field.setAccessible(true);

        // Convert the value safely
        final Class<?> fieldClass = field.getType();
        Object settableObject = fieldClass.isInstance(value) ? value
                : PrimitiveConversions.isNumber(value.getClass()) && PrimitiveConversions.isNumber(fieldClass)
                ? (Y) convertNumber(value, fieldClass)
                : NON_NUMERIC_CONVERSIONS.containsKey(value.getClass()) && NON_NUMERIC_CONVERSIONS.containsKey(fieldClass)
                ? value
                : null;

        // Handle maps
        if (value instanceof Section) {
            settableObject = ((Section) value).getStringRouteMappedValues(false);
        }

        // Set the field value
        field.set(object, value);

        /*if (settableObject != null) {
        } else {
            throw new IllegalArgumentException("Unable to set field " + field.getName() + " of type " +
                    fieldClass.getName() + " to value " + value);
        }*/
    }

    private Optional<Object> readFieldValue(@NotNull Field field, @NotNull T object) throws IllegalAccessException {
        // Ensure the field is accessible
        field.setAccessible(true);

        // If the object is an enum, return the name of the enum
        if (field.getType().isEnum()) {
            return Optional.ofNullable(field.get(object)).map(Object::toString);
        }

        // Otherwise, return the value of the field
        return Optional.ofNullable(field.get(object));
    }

    public T getObject() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.applyMapTo(AnnoData.getDefaults(objectClass));
    }

    protected Class<T> getObjectClass() {
        return this.objectClass;
    }
}
