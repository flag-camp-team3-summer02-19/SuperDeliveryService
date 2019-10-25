package SuperDelivery.service.idm.database;

import SuperDelivery.service.idm.IDMService;
import SuperDelivery.service.idm.configs.ServiceConfigs;
import SuperDelivery.service.idm.logger.ServiceLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreation {
    public static void CreateTable(boolean drop) {
        try {
            // Step 1 Connect to MySQL.
            Connection conn = IDMService.getCon();

            if (conn == null) {
                ServiceLogger.LOGGER.config("No database connection.");
                return;
            }

            Statement statement = conn.createStatement();
            String sql = null;

            if (drop) {
                // Step 2 Drop tables in case they exist.
                sql = "DROP TABLE IF EXISTS sessions";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS user_address";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS users";
                statement.executeUpdate(sql);

                sql = "DROP TABLE IF EXISTS session_status";
                statement.executeUpdate(sql);
            }

            // Step 3 Create new tables if they do not exist
            sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "userId int AUTO_INCREMENT,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "salt VARCHAR(8) NOT NULL,"
                    + "pword VARCHAR(128) NOT NULL,"
                    + "UNIQUE (email),"
                    + "PRIMARY KEY (userId)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS session_status ("
                    + "statusId int NOT NULL,"
                    + "status VARCHAR(20) NOT NULL,"
                    + "PRIMARY KEY (statusId)"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS sessions ("
                    + "sessionID VARCHAR(128) NOT NULL,"
                    + "email VARCHAR(50) NOT NULL,"
                    + "status int NOT NULL,"
                    + "timeCreated timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "lastUsed timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "exprTime timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,"
                    + "PRIMARY KEY (sessionID),"
                    + "FOREIGN KEY (email) REFERENCES users(email) ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "FOREIGN KEY (status) REFERENCES session_status(statusId) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS user_address ("
                    + "email VARCHAR(50) NOT NULL,"
                    + "street VARCHAR(128) NOT NULL,"
                    + "city VARCHAR(32) NOT NULL,"
                    + "state VARCHAR(8) NOT NULL,"
                    + "zipcode int NOT NULL,"
                    + "PRIMARY KEY (email),"
                    + "FOREIGN KEY (email) REFERENCES users (email) ON UPDATE CASCADE ON DELETE CASCADE"
                    + ")";
            statement.execute(sql);

            // Step 4 Insert data into session_status table
            sql = "INSERT INTO session_status (statusId, status) VALUES (1, 'ACTIVE'), (2, 'CLOSED'), (3, 'EXPIRED'), (4, 'REVOKED');";
            statement.execute(sql);

            ServiceLogger.LOGGER.config("Database table setup successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
