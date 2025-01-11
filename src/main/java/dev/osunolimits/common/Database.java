package dev.osunolimits.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Logger;

public class Database {
    private static Logger log = (Logger) LoggerFactory.getLogger(Database.class);
    public static List<MySQL> runningConnections = new ArrayList<MySQL>();
    private HikariConfig hikariConfig;
    public static HikariDataSource dataSource;
    private int connectionTimeout;
    private int maximumPoolSize;
    public static int currentConnections;

    /**
     * Set the connection timeout value.
     *
     * @param connectionTimeout The connection timeout value in milliseconds.
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        hikariConfig.setConnectionTimeout(this.connectionTimeout);
    }

    /**
     * Get the maximum pool size.
     *
     * @return The maximum pool size.
     */
    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    /**
     * Set the maximum pool size.
     *
     * @param maximumPoolSize The maximum pool size.
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        hikariConfig.setMaximumPoolSize(100);
    }

    /**
     * Set the default connection settings.
     *
     * @param cachePrepStmts        Whether to cache prepared statements.
     * @param prepStmtCacheSize     The size of the prepared statement cache.
     * @param prepStmtCacheSqlLimit The SQL limit for the prepared statement cache.
     * @param autoReconnect         Whether to enable auto-reconnect.
     */
    public void setDefaultSettings(boolean cachePrepStmts, int prepStmtCacheSize, int prepStmtCacheSqlLimit,
            boolean autoReconnect) {
        hikariConfig.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);
        hikariConfig.addDataSourceProperty("autoReconnect", autoReconnect);
    }

    /**
     * Set the default connection settings with default values.
     * The default values are cachePrepStmts=true, prepStmtCacheSize=250,
     * prepStmtCacheSqlLimit=2048, autoReconnect=true.
     */
    public void setDefaultSettings() {
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("autoReconnect", true);
    }

    public void setOptimizedSettings() {
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);  // Use prepared statements cache
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);  // Size of the cache
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);  // Max SQL size in the cache
        hikariConfig.addDataSourceProperty("autoReconnect", true);  // Automatically reconnect
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);  // Use server-side prepared statements
        hikariConfig.addDataSourceProperty("useLocalSessionState", true);  // Reduces traffic by caching session state on the server
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", true);  // Optimize batch inserts/updates
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);  // Cache metadata of result sets for improved performance
        hikariConfig.addDataSourceProperty("cacheStatements", true);  // Enable statement cache (reduces the number of allocations)
        
        // MySQL-specific optimizations (for example)
        hikariConfig.addDataSourceProperty("useSSL", false);  // Disable SSL if not required
        hikariConfig.addDataSourceProperty("requireSSL", false);  // Disable SSL if not required
        hikariConfig.addDataSourceProperty("characterEncoding", "UTF-8");  // Ensure correct encoding
        hikariConfig.addDataSourceProperty("connectionTimeout", 30000);  // 30 seconds connection timeout
        hikariConfig.addDataSourceProperty("idleTimeout", 600000);  // 10 minutes idle timeout
        hikariConfig.addDataSourceProperty("maxLifetime", 1800000);  // 30 minutes max lifetime per connection
        hikariConfig.addDataSourceProperty("minimumIdle", 5);  // Minimum number of idle connections in the pool
        hikariConfig.addDataSourceProperty("maximumPoolSize", 20);  // Maximum pool size, can be adjusted based on load
        hikariConfig.addDataSourceProperty("validationTimeout", 5000);  // Timeout for validation queries (in ms)
        hikariConfig.addDataSourceProperty("leakDetectionThreshold", 5000);  // Leak detection threshold (in ms)
    
        // Additional tuning options (optional)
        hikariConfig.addDataSourceProperty("allowPoolSuspension", true);  // Allow suspending the pool (to avoid connections from leaking)
        hikariConfig.addDataSourceProperty("initializationFailTimeout", 1);  // Fail fast on initialization failure
        hikariConfig.addDataSourceProperty("autoCommit", true);  // Auto commit transactions by default (unless you manage this manually)
    }
    

    /**
     * Constructs a new Database object with default settings.
     */
    public Database() {
        this.hikariConfig = new HikariConfig();
    }

    /**
     * Represents the server timezone for the MySQL connection.
     */
    public enum ServerTimezone {
        UTC("UTC"), GMT("GMT");

        private final String code;

        /**
         * Constructs a new ServerTimezone enum with the specified code.
         *
         * @param code The code representing the server timezone.
         */
        ServerTimezone(String code) {
            this.code = code;
        }

        /**
         * Returns the code representing the server timezone.
         *
         * @return The code representing the server timezone.
         */
        @Override
        public String toString() {
            return code;
        }
    }

    /**
     * Connects to a MySQL database using the specified connection parameters.
     *
     * @param host           The host of the MySQL server.
     * @param user           The username for the database connection.
     * @param password       The password for the database connection.
     * @param database       The name of the database to connect to.
     * @param serverTimezone The server timezone for the MySQL connection.
     */
    public void connectToMySQL(String host, String user, String password, String database,
            ServerTimezone serverTimezone) {
        String url = "jdbc:mysql://" + host + ":3306/" + database + "?serverTimezone=" + serverTimezone;
        hikariConfig
                .setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        dataSource = new HikariDataSource(hikariConfig);

        try (Connection connection = dataSource.getConnection()) {
            log.info("Connected to Database (" + url + ")");
            connection.close();
        } catch (SQLException e) {
            log.error("Error while connecting to MySQL database " + e.getMessage());
        }
    }

    /**
     * Get a connection to the MySQL database.
     *
     * @return A connection to the MySQL database.
     */
    public static MySQL getConnection() {
        MySQL connection = null;
        try {
            connection = new MySQL(dataSource.getConnection());
            Database.currentConnections++;
            return connection;
        } catch (SQLException e) {
            log.error("Error while obtaining a connection from the pool.", e);
            return null;
        }
    }
}
