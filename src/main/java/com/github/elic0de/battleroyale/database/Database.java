package com.github.elic0de.battleroyale.database;

import com.github.elic0de.battleroyale.BattleRoyale;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Database {

    protected final BattleRoyale plugin;
    private final String schemaFile;
    private boolean loaded;

    protected Database(@NotNull BattleRoyale plugin, @NotNull String schemaFile) {
        this.plugin = plugin;
        this.schemaFile = "database/" + schemaFile;
    }

    @NotNull
    protected final String[] getSchema() {
        try (InputStream schemaStream = Objects.requireNonNull(plugin.getResource(schemaFile))) {
            final String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            return format(schema).split(";");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load database schema", e);
        }
        return new String[0];
    }

    @NotNull
    protected final String format(@NotNull String statement) {
        final Pattern pattern = Pattern.compile("%(\\w+)%");
        final Matcher matcher = pattern.matcher(statement);
        final StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            final Table table = Table.match(matcher.group(1));
            matcher.appendReplacement(sb, plugin.getSettings().getTableName(table));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    public abstract void initialize() throws RuntimeException;

    public abstract void close();

    public boolean hasLoaded() {
        return loaded;
    }

    protected void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public enum Type {
        MYSQL("MySQL"),
        SQLITE("SQLite");
        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @NotNull
        public String getDisplayName() {
            return displayName;
        }
    }


    public enum Table {
        USER_DATA("_users");

        @NotNull
        private final String defaultName;

        Table(@NotNull String defaultName) {
            this.defaultName = defaultName;
        }

        @NotNull
        public static Database.Table match(@NotNull String placeholder) throws IllegalArgumentException {
            return Table.valueOf(placeholder.toUpperCase());
        }

        @NotNull
        public String getDefaultName() {
            return defaultName;
        }
    }
}
