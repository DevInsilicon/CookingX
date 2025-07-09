package dev.insilicon.cookingX;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.insilicon.cookingX.Utils.ConfigUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import java.util.logging.Level;

public class DatabaseManager {
    public static DatabaseManager instance;

    // HikariCP source will be null if sqlite functionality is enabled
    private static boolean isExternalSource = false;
    private static HikariDataSource externalSource;
    private static Connection sqliteConnection;

    public DatabaseManager() {

        // im honestly really sorry I had to do this, its just for the compiler. I could have just done it in one line :sob: It makes like 3 warnings if I dont do this
        Object configInstance = CookingX.instance.getConfig().get("storage.method");
        String method = null;
        if (configInstance != null) method = configInstance.toString();

        if (method == null) {
            CookingX.instance.getLogger().severe("No storage method specified in config.yml. Please set 'storage.method' to 'sqlite' or 'mysql'. Defaulting to Sqlite");
            method = "sqlite";
            return;
        }

        switch (method.toLowerCase()) {
            case "sqlite":
                isExternalSource = false;
                CookingX.logger.log(Level.INFO, "Loading Sqlite database.");
                Connection connection = initSqlite();
                if (connection != null) {
                    sqliteConnection = connection;
                    CookingX.logger.info("Sqlite database connection initialized successfully.");
                } else {
                    CookingX.logger.severe("Failed to initialize Sqlite database connection.");
                    CookingX.instance.shutDownPlugin();
                    return;
                }
                break;

            case "mysql":
                isExternalSource = true;

                HikariConfig config = new HikariConfig();

                String host = Optional.ofNullable(ConfigUtil.getValueFromConfig("storage.mysql.host"))
                        .filter(s -> !s.isEmpty())
                        .orElse("localhost");
                String port = Optional.ofNullable(ConfigUtil.getValueFromConfig("storage.mysql.port"))
                        .filter(s -> !s.isEmpty())
                        .orElse("10501");
                String database = Optional.ofNullable(ConfigUtil.getValueFromConfig("storage.mysql.database"))
                        .filter(s -> !s.isEmpty())
                        .orElse("cookingx");
                String username = Optional.ofNullable(ConfigUtil.getValueFromConfig("storage.mysql.username"))
                        .filter(s -> !s.isEmpty())
                        .orElse("admin");
                String password = Optional.ofNullable(ConfigUtil.getValueFromConfig("storage.mysql.password"))
                        .filter(s -> !s.isEmpty())
                        .orElse("admin");
                String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
                config.setJdbcUrl(jdbcUrl);

                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(10);

                externalSource = new HikariDataSource(config);
                break;
        }

        CookingX.logger.info("DatabaseManager initialized with method: " + method + ".");
        if (!isExternalSource) {
            CookingX.logger.info("Using Sqlite database.");
            instance = this;
            CookingX.instance.startSystems();
        } else {
            // Preforming test connection because still has not been verified if the connection is working
            try (Connection conncetion = externalSource.getConnection()) {
                if (conncetion.isValid(2)) {
                    CookingX.logger.info("MySQL database connection initialized successfully.");
                    instance = this;
                    CookingX.instance.startSystems();
                } else {
                    CookingX.logger.severe("Failed to initialize MySQL database connection.");
                    CookingX.instance.shutDownPlugin();
                }
            } catch (Exception e ) {
                CookingX.logger.log(Level.SEVERE, "Failed to connect to MySQL database", e);
                CookingX.instance.shutDownPlugin();
            }
        }

    }

    private Connection initSqlite() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:plugins/CookingX/database.db");
        } catch (Exception e) {
            CookingX.logger.log(Level.SEVERE, "Failed to connect to Sqlite database", e);
            return null;
        }
    }


    public Connection getConnection() {
        if (isExternalSource) {
            try (Connection connection = externalSource.getConnection()) {
                return connection;
            } catch (Exception e ) {
                CookingX.logger.log(Level.SEVERE, "Failed to get MySQL connection", e);
                return null;
            }
        } else {
            try {
                if (sqliteConnection != null && !sqliteConnection.isClosed()) {
                    return sqliteConnection;
                } else {
                    sqliteConnection = initSqlite();
                    return sqliteConnection;
                }
            } catch (Exception e) {
                CookingX.logger.log(Level.SEVERE, "Failed to get Sqlite connection", e);
                return null;
            }
        }
    }

    public void closeDatabase() {
        if (isExternalSource && externalSource != null) {
            externalSource.close();
            CookingX.logger.info("MySQL database connection closed.");
        } else if (sqliteConnection != null) {
            try {
                sqliteConnection.close();
                CookingX.logger.info("Sqlite database connection closed.");
            } catch (Exception e) {
                CookingX.logger.log(Level.SEVERE, "Failed to close Sqlite database connection", e);
            }
        }
    }
}
